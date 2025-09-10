package co.com.sti.usecase.resgisteruser;

import co.com.sti.model.user.User;
import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.exceptios.UserAlreadyExistsException;
import co.com.sti.usecase.transaction.TransactionExecutor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public class ResgisterUserUseCase implements IRegisterUserUseCase{

    private final UserRepository userRepository;
    private final TransactionExecutor transactionExecutor;

    public ResgisterUserUseCase(UserRepository userRepository, TransactionExecutor transactionExecutor) {
        this.userRepository = userRepository;
        this.transactionExecutor = transactionExecutor;
    }

    public Mono<User> registerUser(User user){
        return  transactionExecutor.executeInTransaction(() -> {
                if (user.getSalary().compareTo(BigDecimal.ZERO) < 0 || user.getSalary().compareTo(new BigDecimal(15000000)) > 0) {
                    return Mono.error(new InvalidUserDataException("El salario no esta dentro del rango mayor que 0 y menor que 15'000.000 COP"));
                }

                return userRepository.findUserByNumberIdentity(user.getNumberIdentity())
                    .flatMap(existingUser -> Mono.<User>error(new UserAlreadyExistsException("Ya existe un usuario con este número de identidad")))
                    .switchIfEmpty(Mono.defer(() ->
                            userRepository.findUserByEmail(user.getEmail())
                                    .flatMap(existingUser -> Mono.<User>error(new UserAlreadyExistsException("Ya existe un usuario con este email.")))
                                    .switchIfEmpty(Mono.defer(() -> userRepository.saveUser(user)))
                    ));
                }
            );
    }
}
