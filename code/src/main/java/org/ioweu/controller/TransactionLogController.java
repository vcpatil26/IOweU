package org.ioweu.controller;

import org.ioweu.api.TransactionManager;
import org.ioweu.impl.Transaction;
import org.ioweu.impl.TransactionManagerImpl;
import org.ioweu.impl.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class TransactionLogController {

    @ResponseBody
    @RequestMapping(value = "/getAllTransactions.jsp", method = RequestMethod.GET)
    public List<Transaction> getAllTransactions (Principal principal){
        System.out.println("************** ger all transaction ********* ");
        List<Transaction> filteredTransactions = new ArrayList<Transaction>();
        TransactionManager tm = new TransactionManagerImpl();
        Calendar c = Calendar.getInstance();
        Date currentTime = c.getTime();
        List<Transaction> allTransactions = tm.getAllTransactions(currentTime);
        for (Transaction transaction : allTransactions){
            if (transaction.getTransactionOwner().equals(principal.getName())) {
                filteredTransactions.add(transaction);
            }

            List<User> sharedUsers = transaction.getSharedUsers();
            for (User user : sharedUsers){
                if (user.getUserName().equals(principal.getName()) && !user.getUserName().equals(transaction.getTransactionOwner())){
                    filteredTransactions.add(transaction);
                }
            }
        }
        return filteredTransactions;
    }
}
