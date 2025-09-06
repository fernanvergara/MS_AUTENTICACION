package co.com.sti.api.config;

import co.com.sti.api.Handler;
import co.com.sti.api.RouterRest;
import co.com.sti.api.mapper.UserDTOMapper;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import({RouterRest.class, Handler.class, CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.TestConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TaskPath taskPath;

    @Configuration
    static class TestConfig {
        @Bean
        IRegisterUserUseCase registerUserUseCase() {
            return Mockito.mock(IRegisterUserUseCase.class);
        }

        @Bean
        ISearchUserUseCase searchUserUseCase() {
            ISearchUserUseCase mockSearch = Mockito.mock(ISearchUserUseCase.class);
            // Corrige el problema: simula que el UseCase devuelve un Mono vacío para evitar el NPE.
            when(mockSearch.getUserbyIdentification(Mockito.anyString())).thenReturn(Mono.empty());
            return mockSearch;
        }

        @Bean
        UserDTOMapper userDTOMapper() {
            return Mockito.mock(UserDTOMapper.class);
        }

        @Bean
        Validator validator() {
            return Mockito.mock(Validator.class);
        }

        @Bean
        TaskPath taskPath() {
            // Se configura el mock del TaskPath en el momento de la creación del bean
            TaskPath taskPathMock = Mockito.mock(TaskPath.class);
            when(taskPathMock.getTasks()).thenReturn("/api/v1/usuarios");
            when(taskPathMock.getTasksById()).thenReturn("/api/v1/usuarios/{identification}");
            return taskPathMock;
        }
    }

    @Test
    @DisplayName("corsConfigurationShouldAllowOrigins() should return expected headers")
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri(taskPath.getTasks())
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