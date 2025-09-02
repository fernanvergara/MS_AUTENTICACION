package co.com.sti.r2dbc;

import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.r2dbc.entity.UserEntity;
import co.com.sti.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Long,
        MyReactiveRepository
        > implements UserRepository {
    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    public Mono<Void> saveUser(User user) {
        UserEntity userEntity = mapper.map(user, UserEntity.class);
        return repository.save(userEntity)
                .doOnNext(u -> log.info("Nuevo usuario registrado con ID:'{}'", u.getId()))
                .then();
    }

    @Override
    public Mono<User> findUserByNumberIdentity(String numberIdentity) {
        return repository.findByNumberIdentity(numberIdentity)
                .doOnNext(u -> {
                    if(u != null) {
                        log.warn("Existe un usuario registrado con el Número de identificación dado, ID:'{}'", u.getId());
                    }else{
                        log.info("No existe usuario registrado con el Número de identificación: '{}'", numberIdentity);
                    }
                })
                .map(u -> mapper.map(u, User.class));
    }

    @Override
    public Mono<User> findUserByEmail(String email) {
        return repository.findByEmail(email)
                .doOnNext(u -> {
                    if(u != null) {
                        log.warn("Existe un usuario registrado con el Email dado, ID:'{}'", u.getId());
                    }else{
                        log.info("No existe usuario registrado con el Email: '{}'", email);
                    }
                })
                .map(u -> mapper.map(u, User.class));
    }
}
