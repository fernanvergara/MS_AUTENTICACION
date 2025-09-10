package co.com.sti.api.config;

import co.com.sti.api.Handler;
import co.com.sti.api.RouterRest;
import co.com.sti.api.dto.CreateUserDTO;
import co.com.sti.api.mapper.UserDTOMapper;
import co.com.sti.model.user.User;
import co.com.sti.usecase.authentication.IAuthenticationUseCase;
import co.com.sti.usecase.resgisteruser.IRegisterUserUseCase;
import co.com.sti.usecase.searchuser.ISearchUserUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import({RouterRest.class, Handler.class, CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.TestConfig.class, ConfigTest.SecurityTestConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IRegisterUserUseCase registerUserUseCase;

    @Autowired
    private TaskPath taskPath;

    @Configuration
    static class SecurityTestConfig {
        @Bean
        public SecurityWebFilterChain securityTestFilterChain(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        IRegisterUserUseCase registerUserUseCase() {
            return Mockito.mock(IRegisterUserUseCase.class);
        }

        @Bean
        ISearchUserUseCase searchUserUseCase() {
            ISearchUserUseCase mockSearch = Mockito.mock(ISearchUserUseCase.class);
            when(mockSearch.getUserbyIdentification(Mockito.anyString())).thenReturn(Mono.empty());
            return mockSearch;
        }

        @Bean
        IAuthenticationUseCase authenticationUseCase() {
            return Mockito.mock(IAuthenticationUseCase.class);
        }

        @Bean
        UserDTOMapper userDTOMapper() {
            UserDTOMapper mockMapper = Mockito.mock(UserDTOMapper.class);
            when(mockMapper.toModel(Mockito.any())).thenReturn(User.builder().build());
            return mockMapper;
        }

        @Bean
        CreateUserDTO createUserDTO() { return Mockito.mock(CreateUserDTO.class); }

        @Bean
        Validator validator() {
            return Mockito.mock(Validator.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() { return Mockito.mock(PasswordEncoder.class); }

        @Bean
        TaskPath taskPath() {
            // Se configura el mock del TaskPath en el momento de la creación del bean
            TaskPath taskPathMock = Mockito.mock(TaskPath.class);
            when(taskPathMock.getTasks()).thenReturn("/api/v1/usuarios");
            when(taskPathMock.getTasksById()).thenReturn("/api/v1/usuarios/{identification}");
            when(taskPathMock.getTaskAuth()).thenReturn("/api/v1/login");
            return taskPathMock;
        }
    }

    @Test
    @DisplayName("corsConfigurationShouldAllowOrigins() should return expected headers")
    void corsConfigurationShouldAllowOrigins() {
        CreateUserDTO userDto = new CreateUserDTO(
                "Nombre", "Apellido", "email@valido.com", "1234567890",
                LocalDate.of(1990, 1, 1), "3101234567", "Dirección de prueba",
                1, new BigDecimal("1500000.0")
        );
        when(registerUserUseCase.registerUser(Mockito.any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(taskPath.getTasks())
                .bodyValue(userDto)
//                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("securityHeadersConfig should return expected headers for GET request")
    void securityHeadersConfigShouldApplyToGetRequest() {
        webTestClient.get()
                .uri(taskPath.getTasksById(),"123456")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}