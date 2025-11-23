package observer;

import model.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus instance = new EventBus();
    private final List<Consumer<Transaction>> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus get() {
        return instance;
    }

    public void subscribe(Consumer<Transaction> listener) {
        listeners.add(listener);
    }

    public void publish(Transaction tx) {
        for (Consumer<Transaction> listener : listeners) {
            listener.accept(tx);
        }
    }
}
