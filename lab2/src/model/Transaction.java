package model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public class Transaction {
    private UUID id;
    private Instant timestamp;
    private TransactionType type;
    private BigDecimal amount;
    private UUID fromId;
    private UUID toId;


    public Transaction(TransactionType type, BigDecimal amount, UUID fromId, UUID toId) {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.type = type;
        this.amount = amount;
        this.fromId = fromId;
        this.toId = toId;
    }


    public UUID getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getFromId() { return fromId; }
    public UUID getToId() { return toId; }
}