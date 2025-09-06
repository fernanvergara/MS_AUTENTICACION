package co.com.sti.api.security;

import co.com.sti.api.exceptions.ForbiddenException;
import co.com.sti.api.exceptions.UnauthorizedException;
import co.com.sti.api.security.domain.UserPrincipal;
import co.com.sti.model.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class JwtValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Mono<UserPrincipal> validateToken(String token) {
        return Mono.fromCallable(() -> {
                    Claims claims = Jwts.parser()
                            .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                            .build()
                            .parseSignedClaims(token) //aqui lanza error
                            .getPayload();
                    // 1. Extraer el email y el id de rol del token
                    String email = claims.get("email", String.class);
                    log.info("email en el token: {}", email);
                    Integer idRole = claims.get("idRole", Integer.class);
                    log.info("idRole en el token: {}", idRole);

                    // 2. AÑADIDO: Validar que las claims necesarias existen
                    if (email == null || idRole == null) {
                        throw new ForbiddenException("El token no contiene las claims de email y/o idRole.");
                    }

                    // 3. Convertir el id de rol en una lista de autoridades
                    List<String> roles = Collections.singletonList("ROLE_" + Role.getById(idRole).getName());

                    // Devolver un UserPrincipal para Spring Security
                    return new UserPrincipal(email, roles);

                })
                .onErrorResume(err -> { //revisar aqui
                    return switch (err) {
                        case ExpiredJwtException expiredJwtException ->
                                Mono.error(new UnauthorizedException("Token expirado"));
                        case JwtException jwtException ->
                                Mono.error(new ForbiddenException("Token inválido o malformado"));
                        case IllegalArgumentException illegalArgumentException ->
                                Mono.error(new ForbiddenException("Rol no existe o no válido"));
                        case null, default ->
                            // Manejar cualquier otra excepción no controlada
                                Mono.error(new ForbiddenException("Error desconocido en la validación del token"));
                    };
                });
    }

}
