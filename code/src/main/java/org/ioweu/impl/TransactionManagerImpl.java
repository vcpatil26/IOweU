package org.ioweu.impl;

import org.ioweu.api.TransactionManager;
import org.ioweu.data.TransactionDAO;

import java.util.Date;
import java.util.List;

public class TransactionManagerImpl implements TransactionManager {
    private TransactionDAO transactionDAO;

    public TransactionManagerImpl() {
        transactionDAO = new TransactionDAO();
    }

    public void createTransation(Transaction transaction) {
        transactionDAO.addTransaction(transaction);
    }

    public void createRecurringTransation(RecurringTransaction transaction) {
        transactionDAO.addRecurringTransaction(transaction);
    }

    public void approveTransaction(int transactionID, String userName) {
        transactionDAO.approveTransaction(transactionID, userName);
    }

    public void disapproveTransaction(int transactionID, String userName) {
        transactionDAO.disapproveTransaction(transactionID, userName);
    }

    public void editTransaction(Transaction transaction) {
        transactionDAO.editTransaction(transaction);
    }

    public void editRecurringTransaction(RecurringTransaction recurringTransaction) {
        transactionDAO.editRecurringTransaction(recurringTransaction);
    }

    public void deleteTransaction(int transactionID) {
        transactionDAO.deleteTransaction(transactionID);
    }

    public void deleteRecurringTransaction(int transactionID) {
        transactionDAO.deleteRecurringTransaction(transactionID);
    }

    public SimplifiedReport generateSimplifiedReport(List<User> userList) {
        return transactionDAO.generateSimplifiedReport(userList);
    }

    public List<Transaction> getAllTransactions(Date date) {
        return transactionDAO.getAllTransactions(date);
    }

    public void settleTransaction(List<User> users, Date date) {
        transactionDAO.settleTransaction(users, date);
    }

    @Override
    public List<Transaction> getPendingTransactions(String user) {
         return transactionDAO.getPendingTransactions(user);
    }

    @Override
    public Transaction getTransaction(int transactionID) {
        return transactionDAO.getTransaction(transactionID);
    }

    @Override
    public List<RecentActivity> getChangedTransactions(String userName) {
        return transactionDAO.getChangedTransactions(userName);
    }

    @Override
    public List<User> getAssociateUsers(String userName) {
        return transactionDAO.getUsersAssociate(userName);
    }

    @Override
    public List<DebtUser> getUsersInDebt(String uname) {
        return transactionDAO.getUsersInDebt(uname);
    }
}
