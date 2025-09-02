package co.com.sti.usecase.resgisteruser;

import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.exceptios.UserAlreadyExistsException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResgisterUserUseCaseTest {

    @InjectMocks
    private ResgisterUserUseCase useCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionExecutor transactionExecutor;

    private User testUser;
    private User existingUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .name("John Doe")
                .numberIdentity("123456789")
                .email("john.doe@example.com")
                .salary(new BigDecimal(700000))
                .build();

        existingUser = User.builder()
                .name("Jane Smith")
                .numberIdentity("987654321")
                .email("jane.smith@example.com")
                .build();

        when(transactionExecutor.executeInTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
    }

    @Test
    @DisplayName("should register a new user successfully when no existing user is found")
    void testRegisterUser_success() {
        when(userRepository.findUserByNumberIdentity(anyString())).thenReturn(Mono.empty());
        when(userRepository.findUserByEmail(anyString())).thenReturn(Mono.empty());
        when(userRepository.saveUser(any(User.class))).thenReturn(Mono.empty());

        Mono<Void> result = useCase.registerUser(testUser);

        StepVerifier.create(result)
                .verifyComplete();

        verify(userRepository).findUserByNumberIdentity(testUser.getNumberIdentity());
        verify(userRepository).findUserByEmail(testUser.getEmail());
        verify(userRepository).saveUser(testUser);
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when user with same identity number exists")
    void testRegisterUser_identityNumberExists() {
        when(userRepository.findUserByNumberIdentity(anyString())).thenReturn(Mono.just(existingUser));

        Mono<Void> result = useCase.registerUser(testUser);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().equals("Ya existe un usuario con este número de identidad")
                )
                .verify();

        verify(userRepository, times(1)).findUserByNumberIdentity(testUser.getNumberIdentity());
        verify(userRepository, never()).findUserByEmail(anyString());
        verify(userRepository, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when user with same email exists")
    void testRegisterUser_emailExists() {
        when(userRepository.findUserByNumberIdentity(anyString())).thenReturn(Mono.empty());
        when(userRepository.findUserByEmail(anyString())).thenReturn(Mono.just(existingUser));

        Mono<Void> result = useCase.registerUser(testUser);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().equals("Ya existe un usuario con este email.")
                )
                .verify();

        verify(userRepository, times(1)).findUserByNumberIdentity(testUser.getNumberIdentity());
        verify(userRepository, times(1)).findUserByEmail(testUser.getEmail());
        verify(userRepository, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when user's salary exceeds the maximum limit")
    void testRegisterUser_salaryExceedsLimit() {
        User userWithHighSalary = User.builder()
                .name("High Salary User")
                .numberIdentity("999999999")
                .email("high.salary@example.com")
                .salary(new BigDecimal("20000000"))
                .build();

        Mono<Void> result = useCase.registerUser(userWithHighSalary);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidUserDataException &&
                                throwable.getMessage().equals("El salario no esta dentro del rango mayor que 0 y menor que 15'000.000 COP")
                )
                .verify();

        verify(userRepository, times(0)).findUserByNumberIdentity(userWithHighSalary.getNumberIdentity());
        verify(userRepository, times(0)).findUserByEmail(userWithHighSalary.getEmail());
        verify(userRepository, never()).saveUser(any(User.class));
    }
}
