package co.com.sti.config;

import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.resgisteruser.ResgisterUserUseCase;
import co.com.sti.usecase.searchuser.SearchUserUseCase;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.sti.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    public SearchUserUseCase searchUserUseCase(UserRepository userRepository) {
        return new SearchUserUseCase(userRepository);
    }

    @Bean
    public ResgisterUserUseCase resgisterUserUseCase(UserRepository userRepository, TransactionExecutor transactionExecutor) {
        return new ResgisterUserUseCase(userRepository,  transactionExecutor);
    }
}
