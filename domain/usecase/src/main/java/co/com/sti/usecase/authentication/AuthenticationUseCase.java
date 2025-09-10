package co.com.sti.usecase.authentication;

import co.com.sti.usecase.authentication.dto.AuthResponseDto;
import co.com.sti.model.role.Role;
import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.authentication.jwt.IJwtUtilsAuth;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.exceptios.UserNotExistsException;
import reactor.core.publisher.Mono;

public class AuthenticationUseCase implements IAuthenticationUseCase {

    private final UserRepository userRepository;
    private final IJwtUtilsAuth jwtUtilsAuth;

    public AuthenticationUseCase(UserRepository userRepository, IJwtUtilsAuth jwtUtilsAuth) {
        this.userRepository = userRepository;
        this.jwtUtilsAuth = jwtUtilsAuth;
    }

    @Override
    public Mono<AuthResponseDto> authenticate(String email, String password) {
        return userRepository.findUserByEmail(email)
                .flatMap(user -> {
                    // Aquí se valida la contraseña
                    if (jwtUtilsAuth.passwordMatch(password, user.getPassword())) {
                        String token = jwtUtilsAuth.generate(user.getEmail(), user.getIdRole());
                        return Mono.just(AuthResponseDto.builder()
                                .firstName(user.getName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .role(Role.getById(user.getIdRole()).getName())
                                .token(token)
                                .build());
                    } else {
                        return Mono.error(new InvalidUserDataException("No se puede iniciar sesión, Credenciales inválidas"));
                    }
                })
                .switchIfEmpty(Mono.error(new UserNotExistsException("No se puede iniciar sesión, Usuario no encontrado")));
    }

}
