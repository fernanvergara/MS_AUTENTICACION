package co.com.sti.usecase.searchuser;

import co.com.sti.model.user.User;
import reactor.core.publisher.Mono;

public interface ISearchUserUseCase {
    Mono<User> getUserbyIdentification(String identification);
}
