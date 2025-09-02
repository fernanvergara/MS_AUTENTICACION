package co.com.sti.usecase.searchuser;

import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchUserUseCase useCase;

    @Test
    @DisplayName("should return a user when identification exists")
    void testGetUserByIdentification_userFound() {
        String identification = "123456789";
        User mockUser = User.builder()
                .name("Test User")
                .numberIdentity(identification)
                .email("test.user@example.com")
                .salary(new BigDecimal("10000000"))
                .build();

        when(userRepository.findUserByNumberIdentity(identification)).thenReturn(Mono.just(mockUser));

        Mono<User> result = useCase.getUserbyIdentification(identification);

        StepVerifier.create(result)
                .expectNext(mockUser)
                .verifyComplete();

        verify(userRepository, times(1)).findUserByNumberIdentity(identification);
    }

    @Test
    @DisplayName("should return an empty mono when identification does not exist")
    void testGetUserByIdentification_userNotFound() {
        String identification = "987654321";

        when(userRepository.findUserByNumberIdentity(anyString())).thenReturn(Mono.empty());

        Mono<User> result = useCase.getUserbyIdentification(identification);

        StepVerifier.create(result)
                .expectNextCount(0) // Expect 0 elements
                .verifyComplete(); // The Mono should complete successfully

        verify(userRepository, times(1)).findUserByNumberIdentity(identification);
    }

}
