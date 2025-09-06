package co.com.sti.api;

import co.com.sti.api.config.TaskPath;
import co.com.sti.api.dto.CreateUserDTO;
import co.com.sti.api.exceptions.GlobalExceptionHandler;
import co.com.sti.api.mapper.UserDTOMapper;
import co.com.sti.model.user.User;
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
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({Handler.class, RouterRestTest.TestRouter.class, GlobalExceptionHandler.class, RouterRest.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IRegisterUserUseCase registerUserUseCase;

    @Autowired
    private ISearchUserUseCase searchUserUseCase;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

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
        UserDTOMapper userDTOMapper() {
            return mock(UserDTOMapper.class);
        }

        @Bean
        Validator validator() {
            return mock(Validator.class);
        }

        @Bean
        TaskPath taskPath() {
            // Se configura el mock del TaskPath en el momento de la creación del bean
            TaskPath taskPathMock = mock(TaskPath.class);
            when(taskPathMock.getTasks()).thenReturn("/api/v1/usuarios");
            when(taskPathMock.getTasksById()).thenReturn("/api/v1/usuarios/{identification}");
            return taskPathMock;
        }
    }

    @Test
    @DisplayName("should return 200 OK for a valid POST request to /api/v1/usuarios")
    void testListenPOSTUseCase() {
        // Simula el comportamiento del UseCase y el Mapper
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());
        when(registerUserUseCase.registerUser(any(User.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"test@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("should return 409 Conflict for an existing email")
    void testListenPOST_withExistingEmail() {
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(new User());
        when(registerUserUseCase.registerUser(any(User.class)))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists")));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\", \"lastName\":\"test\", \"email\":\"existing@test.com\", \"numberIdentity\":\"1234567890\", \"birthDate\":\"1990-01-01\", \"phoneNumber\":\"1234567890\", \"address\":\"Test Address 123\", \"idRole\":1, \"salary\":1000000}")
                .exchange()
                .expectStatus().is4xxClientError();
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
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

}
