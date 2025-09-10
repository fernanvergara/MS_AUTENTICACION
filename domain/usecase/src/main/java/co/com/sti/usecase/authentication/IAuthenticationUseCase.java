package co.com.sti.usecase.authentication;

import co.com.sti.usecase.authentication.dto.AuthResponseDto;
import reactor.core.publisher.Mono;

public interface IAuthenticationUseCase {
    Mono<AuthResponseDto> authenticate(String email, String password);
}
