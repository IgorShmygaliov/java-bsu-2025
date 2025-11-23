package observer;


import model.Transaction;


public interface TransactionListener {
    void onTransaction(Transaction tx);
}