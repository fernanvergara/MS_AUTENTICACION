package co.com.sti.r2dbc;

import co.com.sti.model.user.User;
import co.com.sti.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @InjectMocks
    private MyReactiveRepositoryAdapter adapter;

    @Mock
    private MyReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private User testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setup() {
        // Initializes test data before each test.
        testUser = User.builder()
                .name("testName")
                .numberIdentity("1234567890")
                .email("test@example.com")
                .build();
        testUserEntity = UserEntity.builder()
                .id(1L)
                .name("testName")
                .numberIdentity("1234567890")
                .email("test@example.com")
                .build();

    }

    @Test
    @DisplayName("should save a user and complete successfully")
    void testSaveUser() {
        when(mapper.map(any(User.class), eq(UserEntity.class))).thenReturn(testUserEntity);
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(testUserEntity));

        Mono<Void> result = adapter.saveUser(testUser);
        StepVerifier.create(result)
                .verifyComplete();
        verify(repository).save(testUserEntity);
    }

    @Test
    @DisplayName("should find a user by number identity")
    void testFindUserByNumberIdentity() {
        when(repository.findByNumberIdentity(testUser.getNumberIdentity())).thenReturn(Mono.just(testUserEntity));
        when(mapper.map(any(UserEntity.class), eq(User.class))).thenReturn(testUser);

        Mono<User> result = adapter.findUserByNumberIdentity(testUser.getNumberIdentity());
        StepVerifier.create(result)
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    @DisplayName("should find a user by email")
    void testFindUserByEmail() {
        when(repository.findByEmail(testUser.getEmail())).thenReturn(Mono.just(testUserEntity));
        when(mapper.map(any(UserEntity.class), eq(User.class))).thenReturn(testUser);

        Mono<User> result = adapter.findUserByEmail(testUser.getEmail());
        StepVerifier.create(result)
                .expectNext(testUser)
                .verifyComplete();
    }}
