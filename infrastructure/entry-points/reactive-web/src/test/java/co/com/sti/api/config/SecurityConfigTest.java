package co.com.sti.api.config;


import co.com.sti.api.exceptions.UnauthorizedException;
import co.com.sti.api.security.JwtValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private ApplicationContext applicationContext;

    private ReactiveAuthenticationManager authenticationManager;
    private ServerSecurityContextRepository securityContextRepository;

    @BeforeEach
    void setUp() {
        authenticationManager = securityConfig.authenticationManager();
        securityContextRepository = securityConfig.securityContextRepository();
    }

    @Test
    @DisplayName("should provide a BCryptPasswordEncoder bean")
    void passwordEncoderBean_ShouldProvideBCryptPasswordEncoder() {
        assertNotNull(securityConfig.passwordEncoder());
    }

    @Test
    @DisplayName("authenticationManager should authenticate successfully with a valid token")
    void authenticationManager_ValidToken_AuthenticatesSuccessfully() {
        String testToken = "valid-token";
        Authentication expectedAuth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                testToken,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtValidator.validateToken(testToken)).thenReturn(Mono.just(expectedAuth));

        Authentication inputToken = new UsernamePasswordAuthenticationToken(null, testToken);
        Mono<Authentication> result = authenticationManager.authenticate(inputToken);

        StepVerifier.create(result)
                .expectNextMatches(authentication -> {
                    assertEquals(expectedAuth.getPrincipal(), authentication.getPrincipal());
                    assertEquals(expectedAuth.getCredentials(), authentication.getCredentials());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("authenticationManager should throw UnauthorizedException for an invalid token")
    void authenticationManager_InvalidToken_ThrowsUnauthorizedException() {
        when(jwtValidator.validateToken(anyString())).thenReturn(Mono.error(new UnauthorizedException("Invalid token")));

        Authentication token = new UsernamePasswordAuthenticationToken("invalid-token", "invalid-token");
        Mono<Authentication> result = authenticationManager.authenticate(token);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UnauthorizedException &&
                                throwable.getMessage().equals("Invalid token")
                )
                .verify();
    }

    @Test
    @DisplayName("securityContextRepository should load a valid context from a valid Authorization header")
    void securityContextRepository_ValidTokenInHeader_LoadsContext() {
        String testToken = "valid-token";
        Authentication expectedAuth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                testToken,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        ReactiveAuthenticationManager managerMock = mock(ReactiveAuthenticationManager.class);
        when(managerMock.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(expectedAuth));

        when(applicationContext.getBean(eq("authenticationManager")))
                .thenReturn(managerMock);

        ServerWebExchange mockedExchange = mock(ServerWebExchange.class);
        when(mockedExchange.getRequest())
                .thenReturn(MockServerHttpRequest.get("/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + testToken)
                        .build());
        when(mockedExchange.getApplicationContext())
                .thenReturn(applicationContext);

        Mono<SecurityContext> result = securityContextRepository.load(mockedExchange);

        StepVerifier.create(result)
                .expectNextMatches(context -> {
                    assertNotNull(context.getAuthentication());
                    return context.getAuthentication().equals(expectedAuth);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("securityContextRepository should return empty for no Authorization header")
    void securityContextRepository_NoAuthHeader_ReturnsEmpty() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        Mono<SecurityContext> result = securityContextRepository.load(exchange);

        StepVerifier.create(result)
                .verifyComplete();
    }

}
