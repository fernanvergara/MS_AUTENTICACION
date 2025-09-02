package co.com.sti.model.user.gateways;

import co.com.sti.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Void> saveUser(User user);
    Mono<User> findUserByNumberIdentity(String numberIdentity);
    Mono<User> findUserByEmail(String email);
}
