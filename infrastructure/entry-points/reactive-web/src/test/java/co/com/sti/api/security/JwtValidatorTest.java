package co.com.sti.api.security;

import co.com.sti.api.exceptions.ForbiddenException;
import co.com.sti.api.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtValidatorTest {

    private JwtValidator jwtValidator;
    private static final String JWT_SECRET = "thisisthejwtsecretkeyforsecurityandtestingpurposes";
    private Key key;

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator();
        ReflectionTestUtils.setField(jwtValidator, "jwtSecret", JWT_SECRET);
        key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    // -- PRUEBAS DE ESCENARIOS DE ÉXITO --

    @Test
    @DisplayName("should validate a valid token and return a UserPrincipal")
    void validateToken_validToken_returnsUserPrincipal() {
        String token = Jwts.builder()
                .claim("email", "test@example.com")
                .claim("idRole", 1) // Usamos un ID de rol válido (ADMIN)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, TimeUnit.HOURS.toChronoUnit())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        StepVerifier.create(jwtValidator.validateToken(token))
                .assertNext(userPrincipal -> {
                    assertEquals("test@example.com", userPrincipal.getEmail());
                    assertEquals(1, userPrincipal.getAuthorities().size());
                    assertTrue(userPrincipal.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN")));
                })
                .verifyComplete();
    }

    // -- PRUEBAS DE ESCENARIOS DE FALLA (ERRORES) --

    @Test
    @DisplayName("should return Mono.error(UnauthorizedException) for an expired token")
    void validateToken_expiredToken_throwsUnauthorizedException() {
        String token = Jwts.builder()
                .claim("email", "test@example.com")
                .claim("idRole", 1)
                // Token expirado 1 hora en el pasado
                .issuedAt(Date.from(Instant.now().minus(2, TimeUnit.HOURS.toChronoUnit())))
                .expiration(Date.from(Instant.now().minus(1, TimeUnit.HOURS.toChronoUnit())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        StepVerifier.create(jwtValidator.validateToken(token))
                .verifyErrorSatisfies(throwable ->
                        assertTrue(throwable instanceof UnauthorizedException)
                );
    }

    @Test
    @DisplayName("should return Mono.error(ForbiddenException) for an invalid signature")
    void validateToken_invalidSignature_throwsForbiddenException() {
        Key invalidKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String token = Jwts.builder()
                .claim("email", "test@example.com")
                .claim("idRole", 1)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, TimeUnit.HOURS.toChronoUnit())))
                .signWith(invalidKey, SignatureAlgorithm.HS256)
                .compact();

        StepVerifier.create(jwtValidator.validateToken(token))
                .verifyErrorSatisfies(throwable ->
                        assertTrue(throwable instanceof ForbiddenException)
                );
    }

    @Test
    @DisplayName("should return Mono.error(ForbiddenException) for a malformed token")
    void validateToken_malformedToken_throwsForbiddenException() {
        String malformedToken = "invalid.token.format";

        StepVerifier.create(jwtValidator.validateToken(malformedToken))
                .verifyErrorSatisfies(throwable ->
                        assertTrue(throwable instanceof ForbiddenException)
                );
    }

    @Test
    @DisplayName("should return Mono.error(ForbiddenException) when a required claim is missing")
    void validateToken_missingClaim_throwsForbiddenException() {
        // Token sin el claim 'email'
        String token = Jwts.builder()
                .claim("idRole", 1)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, TimeUnit.HOURS.toChronoUnit())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        StepVerifier.create(jwtValidator.validateToken(token))
                .verifyErrorSatisfies(throwable ->
                        assertTrue(throwable instanceof ForbiddenException)
                );
    }

    @Test
    @DisplayName("should return Mono.error(ForbiddenException) when idRole is invalid")
    void validateToken_invalidIdRole_throwsForbiddenException() {
        // Token con un id de rol que no existe, forzando un IllegalArgumentException.
        String token = Jwts.builder()
                .claim("email", "test@example.com")
                .claim("idRole", 999) // ID de rol que no existe en tu enum
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, TimeUnit.HOURS.toChronoUnit())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        StepVerifier.create(jwtValidator.validateToken(token))
                .verifyErrorSatisfies(throwable ->
                        assertTrue(throwable instanceof ForbiddenException)
                );
    }

}
