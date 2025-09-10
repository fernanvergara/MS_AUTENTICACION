package co.com.sti.config;

import co.com.sti.model.user.gateways.UserRepository;
import co.com.sti.usecase.authentication.jwt.IJwtUtilsAuth;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@Profile("test")
public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    System.out.println("Found UseCase bean: " + beanName);
                    useCaseBeanFound = true;
                    // Optional: You can also assert that a specific bean is present.
                    assertNotNull(context.getBean(beanName), "Bean " + beanName + " should not be null");
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary // Prevents Spring from getting confused with multiple beans of the same type in other test files.
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        // Provides a mock bean for TransactionExecutor, another dependency.
        @Bean
        @Primary
        public TransactionExecutor transactionExecutor() {
            return mock(TransactionExecutor.class);
        }

        @Bean
        @Primary
        public IJwtUtilsAuth jwtUtilsAuth() {
            return mock(IJwtUtilsAuth.class);
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}