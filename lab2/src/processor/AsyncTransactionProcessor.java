package processor;

import model.Transaction;
import observer.EventBus;
import strategy.TransactionStrategyFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTransactionProcessor {
    private final ExecutorService pool = Executors.newFixedThreadPool(8);
    private final TransactionStrategyFactory factory;

    public AsyncTransactionProcessor(TransactionStrategyFactory factory) {
        this.factory = factory;
    }

    public CompletableFuture<Void> submit(Transaction tx) {
        return CompletableFuture.runAsync(() -> {
            factory.get(tx).execute(tx);
            EventBus.get().publish(tx);
        }, pool);
    }

    public void shutdown() {
        pool.shutdown();
    }
}
