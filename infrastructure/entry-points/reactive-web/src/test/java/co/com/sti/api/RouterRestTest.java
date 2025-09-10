package co.com.sti.api;

import co.com.sti.api.config.SecurityConfig;
import co.com.sti.api.config.TaskPath;
import co.com.sti.api.dto.CreateUserDTO;
import co.com.sti.api.dto.LoginDto;
import co.com.sti.api.exceptions.GlobalExceptionHandler;
import co.com.sti.api.mapper.UserDTOMapper;
import co.com.sti.api.security.JwtValidator;
import co.com.sti.model.user.User;
import co.com.sti.usecase.authentication.IAuthenticationUseCase;
import co.com.sti.usecase.authentication.dto.AuthResponseDto;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.resgisteruser.IRegisterUserUseCase;
import co.com.sti.usecase.exceptios.UserAlreadyExistsException;
import co.com.sti.usecase.searchuser.ISearchUserUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import({Handler.class, RouterRest.class, GlobalExceptionHandler.class, RouterRestTest.TestRouter.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IRegisterUserUseCase registerUserUseCase;

    @Autowired
    private ISearchUserUseCase searchUserUseCase;

    @Autowired
    private IAuthenticationUseCase authenticationUseCase;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Configuration
    static class TestRouter {
        @Bean
        IRegisterUserUseCase registerUserUseCase() {
            return mock(IRegisterUserUseCase.class);
        }

        @Bean
        ISearchUserUseCase searchUserUseCase() {
            return mock(ISearchUserUseCase.class);
        }

        @Bean
        IAuthenticationUseCase authenticationUseCase() {
            return mock(IAuthenticationUseCase.class);
        }

        @Bean
        UserDTOMapper userDTOMapper() {
            return mock(UserDTOMapper.class);
        }

        @Bean
        Validator validator() {
            return mock(Validator.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        // 4. Mockeamos los beans de seguridad necesarios
        @Bean
        ReactiveAuthenticationManager authenticationManager() {
            ReactiveAuthenticationManager manager = mock(ReactiveAuthenticationManager.class);
            when(manager.authenticate(any())).thenReturn(
                    Mono.just(new UsernamePasswordAuthenticationToken(
                            "mockUser", null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    ))
            );
            return manager;
        }

        @Bean
        ServerSecurityContextRepository securityContextRepository() {
            ServerSecurityContextRepository repository = mock(ServerSecurityContextRepository.class);
            when(repository.load(any())).thenReturn(
                    Mono.just(new org.springframework.security.core.context.SecurityContextImpl(
                            new UsernamePasswordAuthenticationToken("mockUser", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    ))
            );
            return repository;
        }

        @Bean
        JwtValidator jwtValidator() {
            return mock(JwtValidator.class);
        }

        @Bean
        TaskPath taskPath() {
            TaskPath taskPathMock = mock(TaskPath.class);
            when(taskPathMock.getTasks()).thenReturn("/api/v1/usuarios");
            when(taskPathMock.getTasksById()).thenReturn("/api/v1/usuarios/{identification}");
            when(taskPathMock.getTaskAuth()).thenReturn("/api/v1/login");
            return taskPathMock;
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 200 OK for a valid POST request to /api/v1/usuarios")
    void testListenPOSTUseCase() {
        // Simula el comportamiento del UseCase y el Mapper
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());
        when(registerUserUseCase.registerUser(any(User.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("should return 409 Conflict for an existing email")
    void testListenPOST_withExistingEmail() {
        when(validator.validate(any(CreateUserDTO.class))).thenReturn(Set.of());
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());
        when(registerUserUseCase.registerUser(any(User.class)))
                .thenReturn(Mono.error(new UserAlreadyExistsException("Email already exists")));


        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"existing@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("should return 400 Bad Request for an invalid salary")
    void testListenPOST_withInvalidSalary() {
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<CreateUserDTO>> violations = (Set<ConstraintViolation<CreateUserDTO>>) mock(Set.class);
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(CreateUserDTO.class))).thenReturn(violations);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":2, \"salary\":2000000}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("should return 400 Bad Request for missing required data (e.g., name)")
    void testListenPOST_withMissingData() {
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<CreateUserDTO>> violations = (Set<ConstraintViolation<CreateUserDTO>>) mock(Set.class);
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(CreateUserDTO.class))).thenReturn(violations);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                // 'name' field is missing.
                .bodyValue("{\"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("should return 409 Conflict for an existing identification number")
    void testListenPOST_withExistingIdentification() {
        // Mocks the use case to throw an exception, simulating that the ID already exists.
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());
        when(registerUserUseCase.registerUser(any(User.class)))
                .thenReturn(Mono.error(new UserAlreadyExistsException("Identification number already exists")));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("should return  500 INTERNAL_SERVER_ERROR for incorrect data type (e.g., idRole is a string)")
    void testListenPOST_withInvalidDataType() {
        // The validator mock will simulate a validation error for the CreateUserDTO.
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<CreateUserDTO>> violations = (Set<ConstraintViolation<CreateUserDTO>>) mock(Set.class);
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(CreateUserDTO.class))).thenReturn(violations);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                // 'idRole' is a string instead of an integer.
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":\"invalid\", \"salary\":1000000}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("should return 200 OK with user data for a valid GET request to /api/v1/usuarios/{identification}")
    void testListenGETUseCaseFound() throws JsonProcessingException {
        String identification = "1234567890";
        User foundUser = new User(); // Mock a found user
        foundUser.setNumberIdentity(identification);
        String expectedJson = objectMapper.writeValueAsString(foundUser);

        when(searchUserUseCase.getUserbyIdentification(identification))
                .thenReturn(Mono.just(foundUser));

        webTestClient.get()
                .uri("/api/v1/usuarios/{identification}", identification)
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(expectedJson);
    }

    @Test
    @DisplayName("should return 200 OK with empty body for a GET request to /api/v1/usuarios/{identification} when user not found")
    void testListenGETUseCaseNotFound() {
        String identification = "9999999999";

        when(searchUserUseCase.getUserbyIdentification(identification))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/usuarios/{identification}", identification)
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("should return 200 OK and an auth token for a valid login")
    void testLogin_Success() {
        LoginDto loginDto = new LoginDto("test@test.com", "password123");
        AuthResponseDto authResponseDto = new AuthResponseDto("test@test.com", "John", "Doe", "USER", "fake_token");

        when(authenticationUseCase.authenticate(loginDto.getEmail(), loginDto.getPassword()))
                .thenReturn(Mono.just(authResponseDto));

        webTestClient.post()
                .uri("/api/v1/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("fake_token");
    }

    @Test
    @DisplayName("should return 401 Unauthorized for an invalid login")
    void testLogin_InvalidCredentials() {
        LoginDto loginDto = new LoginDto("invalid@test.com", "wrong_password");

        when(authenticationUseCase.authenticate(loginDto.getEmail(), loginDto.getPassword()))
                .thenReturn(Mono.error(new InvalidUserDataException("Invalid credentials")));

        webTestClient.post()
                .uri("/api/v1/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

}
