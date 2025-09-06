package co.com.sti.usecase.authentication;

import co.com.sti.model.role.Role;
import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.authentication.dto.AuthResponseDto;
import co.com.sti.usecase.authentication.jwt.IJwtUtilsAuth;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.exceptios.UserNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

    @InjectMocks
    private AuthenticationUseCase useCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IJwtUtilsAuth jwtUtilsAuth;

    private User testUser;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String testToken = "test-token-jwt";
    private final int testRoleId = 1;

    @BeforeEach
    void setUp() {
        // Configuración de un usuario de prueba para los escenarios exitosos y de fallo de contraseña
        testUser = User.builder()
                .name("Test")
                .lastName("User")
                .email(testEmail)
                .password("hashedPassword")
                .idRole(testRoleId)
                .build();
    }

    @Test
    @DisplayName("should authenticate a user successfully and return an AuthResponseDto")
    void authenticate_SuccessfulAuthentication_ReturnsAuthResponseDto() {
        // 1. Configuración de los mocks para simular una autenticación exitosa.
        // Se simula que el usuario es encontrado por su email
        when(userRepository.findUserByEmail(testEmail)).thenReturn(Mono.just(testUser));
        // Se simula que la contraseña coincide
        when(jwtUtilsAuth.passwordMatch(testPassword, testUser.getPassword())).thenReturn(true);
        // Se simula la generación de un token
        when(jwtUtilsAuth.generate(anyString(), anyInt())).thenReturn(testToken);

        // 2. Ejecución del método a probar
        Mono<AuthResponseDto> result = useCase.authenticate(testEmail, testPassword);

        // 3. Verificación de los resultados
        StepVerifier.create(result)
                .assertNext(authResponseDto -> {
                    // Se verifican los campos del DTO de respuesta
                    assertEquals("Test", authResponseDto.getFirstName());
                    assertEquals("User", authResponseDto.getLastName());
                    assertEquals(testEmail, authResponseDto.getEmail());
                    assertEquals(Role.getById(testUser.getIdRole()).getName(), authResponseDto.getRole());
                    assertEquals(testToken, authResponseDto.getToken());
                })
                .verifyComplete();

        // Se verifica que se llamaron a los métodos del repositorio y del JWTUtils
        verify(userRepository).findUserByEmail(testEmail);
        verify(jwtUtilsAuth).passwordMatch(testPassword, testUser.getPassword());
        verify(jwtUtilsAuth).generate(testUser.getEmail(), testUser.getIdRole());
    }

    @Test
    @DisplayName("should throw UserNotExistsException when user is not found by email")
    void authenticate_UserNotFound_ThrowsUserNotExistsException() {
        // 1. Configuración del mock para simular que no se encuentra el usuario
        when(userRepository.findUserByEmail(testEmail)).thenReturn(Mono.empty());

        // 2. Ejecución del método a probar
        Mono<AuthResponseDto> result = useCase.authenticate(testEmail, testPassword);

        // 3. Verificación de la excepción esperada
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotExistsException &&
                                throwable.getMessage().equals("No se puede iniciar sesión, Usuario no encontrado")
                )
                .verify();

        // Se verifica que solo se llamó al método de búsqueda de usuario
        verify(userRepository).findUserByEmail(testEmail);
    }

    @Test
    @DisplayName("should throw InvalidUserDataException when password does not match")
    void authenticate_PasswordMismatch_ThrowsInvalidUserDataException() {
        // 1. Configuración de los mocks para simular una contraseña incorrecta
        // Se simula que el usuario es encontrado
        when(userRepository.findUserByEmail(testEmail)).thenReturn(Mono.just(testUser));
        // Se simula que la contraseña NO coincide
        when(jwtUtilsAuth.passwordMatch(testPassword, testUser.getPassword())).thenReturn(false);

        // 2. Ejecución del método a probar
        Mono<AuthResponseDto> result = useCase.authenticate(testEmail, testPassword);

        // 3. Verificación de la excepción esperada
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidUserDataException &&
                                throwable.getMessage().equals("No se puede iniciar sesión, Credenciales inválidas")
                )
                .verify();

        // Se verifica que se llamó a los métodos correctos, pero no al de generación de token
        verify(userRepository).findUserByEmail(testEmail);
        verify(jwtUtilsAuth).passwordMatch(testPassword, testUser.getPassword());
    }
}
