package co.com.sti.api.config;

import co.com.sti.api.security.JwtValidator;
import co.com.sti.model.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtValidator jwtValidator;

    public SecurityConfig(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager, ServerSecurityContextRepository securityContextRepository) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String jsonError = "{\"error\":\"Autenticación requerida\",\"message\":\"Token de autenticación no válido o no proporcionado.\"}";
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));
                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String jsonError = "{\"error\":\"Permiso Denegado\",\"message\":\"No tienes permisos para acceder a este recurso.\"}";
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));
                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        })
                )
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "api/doc/**").permitAll()
                        .pathMatchers( "/api/v1/usuarios").hasAnyRole(Role.ADMIN.getName(), Role.ADVISOR.getName())
                        .pathMatchers( "/api/v1/usuarios/**").hasAnyRole(Role.ADMIN.getName(), Role.ADVISOR.getName(), Role.CLIENT.getName())
                        .pathMatchers("/api/v1/login").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            String token = authentication.getCredentials().toString();
            // Lógica para validar el token y devolver un Mono con la autenticación
            return jwtValidator.validateToken(token)
                    .map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        };
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository() {
            @Override
            public Mono<SecurityContext> load(org.springframework.web.server.ServerWebExchange exchange) {
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    String authToken = token.substring(7);
                    return authenticationManager()
                            .authenticate(new UsernamePasswordAuthenticationToken(authToken, authToken))
                            .map(org.springframework.security.core.context.SecurityContextImpl::new)
                            .cast(SecurityContext.class)
                            .onErrorResume(throwable -> Mono.empty());
                }
                return Mono.empty();
            }
        };
    }

}
