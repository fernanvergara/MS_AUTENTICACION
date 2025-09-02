package co.com.sti.usecase.searchuser;

import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;


public class SearchUserUseCase implements ISearchUserUseCase {

    private final UserRepository userRepository;

    public SearchUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> getUserbyIdentification(String identification) {
        return userRepository.findUserByNumberIdentity(identification);
    }
}
