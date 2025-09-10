package co.com.sti.usecase.resgisteruser;

import co.com.sti.model.user.User;
import reactor.core.publisher.Mono;

public interface IRegisterUserUseCase {
    Mono<User> registerUser(User user);
}
