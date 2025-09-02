package co.com.sti.usecase.transaction;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface TransactionExecutor {
    <T> Mono<T> executeInTransaction(Supplier<Mono<T>> supplier);
}
