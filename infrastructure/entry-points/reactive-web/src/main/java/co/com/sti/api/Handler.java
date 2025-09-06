package co.com.sti.api;

import co.com.sti.api.dto.CreateUserDTO;
import co.com.sti.api.dto.LoginDto;
import co.com.sti.usecase.authentication.IAuthenticationUseCase;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.api.mapper.UserDTOMapper;
import co.com.sti.usecase.resgisteruser.IRegisterUserUseCase;
import co.com.sti.usecase.searchuser.ISearchUserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final IRegisterUserUseCase registerUserUseCase;
    private final ISearchUserUseCase searchUserUseCase;
    private final IAuthenticationUseCase authenticationUseCase;
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public Mono<ServerResponse> registerUserEntryPoint(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserDTO.class)
                .flatMap(dto -> {
                    Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        return Mono.error(new InvalidUserDataException(errorMessage));
                    }
                    return Mono.just(dto);
                })
                .map(userDTOMapper::toModel)
                .doOnNext(model -> model.setPassword(passwordEncoder.encode(model.getNumberIdentity())))
                .flatMap(registerUserUseCase::registerUser)
                .flatMap(savedUser -> {
                        log.info("Usuario registrado correctamente: {}", savedUser);
                        Map<String, String> successMessage = new HashMap<>();
                        successMessage.put("message", "Usuario registrado exitosamente");
                        // Se devuelve una respuesta exitosa con el mensaje JSON
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(successMessage);
                        }
                );
    }

    public Mono<ServerResponse> getUserByIdentificationEntryPoint(ServerRequest serverRequest) {
        String identification = serverRequest.pathVariable("identification");
        return searchUserUseCase.getUserbyIdentification(identification)
                .flatMap(foundedUser -> {
                    // Si se encuentra el usuario, devuelve un 200 OK con los datos
                    log.info("Usuario encontrado: {}", foundedUser.getName()+" "+foundedUser.getLastName());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(foundedUser);
                })
                .switchIfEmpty(
                        // Si el Mono está vacío (no se encontró el usuario), devuelve un 200 OK sin cuerpo
                        Mono.defer(() -> {
                            log.warn("Usuario con identificación {} no encontrado. Devolviendo 200 OK con cuerpo vacío.", identification);
                            return ServerResponse.ok().build();
                        })
                );
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginDto.class)
                .flatMap(loginDto -> authenticationUseCase.authenticate(loginDto.getEmail(), loginDto.getPassword())
                        .doOnSuccess(authResponseDto -> {
                            log.info("Usuario autenticado: {}", authResponseDto.getEmail());
                            log.info("Nombres: {}", authResponseDto.getFirstName()+" "+authResponseDto.getLastName());
                            log.info("Role: {}", authResponseDto.getRole());
                            log.info("token: {}", authResponseDto.getToken());
                        })
                        .flatMap(authResponseDto -> ServerResponse.ok().bodyValue(authResponseDto)));
    }
}
