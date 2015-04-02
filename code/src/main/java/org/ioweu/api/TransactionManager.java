package org.ioweu.api;


import org.ioweu.impl.*;

import java.util.Date;
import java.util.List;

public interface TransactionManager {
    public void createTransation(Transaction transaction);
    public void createRecurringTransation(RecurringTransaction transaction);
    public void approveTransaction(int transactionID, String userName);
    public void disapproveTransaction(int transactionID, String userName);
    public void editTransaction(Transaction transaction);
    public void editRecurringTransaction (RecurringTransaction recurringTransaction);
    public void deleteTransaction(int transactionID);
    public void deleteRecurringTransaction(int transactionID);
    public SimplifiedReport generateSimplifiedReport(List<User> userList);
    public List<Transaction> getAllTransactions(Date date);
    public void settleTransaction(List<User> users, Date date);
    public List<Transaction> getPendingTransactions(String user);
    public Transaction getTransaction(int transactionID);
    public List<RecentActivity> getChangedTransactions (String userName);
    public List<User> getAssociateUsers (String userName);
    public List<DebtUser> getUsersInDebt(String uname);
}
