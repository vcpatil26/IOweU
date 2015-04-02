package org.ioweu.controller;

import org.ioweu.api.TransactionManager;
import org.ioweu.impl.Transaction;
import org.ioweu.impl.TransactionManagerImpl;
import org.ioweu.utils.IOweUConstants;
import org.ioweu.utils.TransactionConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController {
    @ResponseBody
    @RequestMapping(value = "/getPendingTransactions.jsp", method = RequestMethod.GET)
    public List<Transaction> getPendingTransactions (Principal principal){
        System.out.println("*************** get pending transactions **********");
        TransactionManager tm = new TransactionManagerImpl();
        return tm.getPendingTransactions(principal.getName());
    }

    @ResponseBody
    @RequestMapping(value = "/getTransactionPage.jsp", method = RequestMethod.GET)
    public ModelAndView getTransactionPage (ServletRequest request){
        System.out.println("*************** get transaction info **********");
        return new ModelAndView(IOweUConstants.TRANSACTION_INFO);
    }

    @ResponseBody
    @RequestMapping(value = "/getTransaction.jsp", method = RequestMethod.GET)
    public Transaction getTransaction (ServletRequest request){
        System.out.println("*************** get transaction info **********");
        TransactionManager tm = new TransactionManagerImpl();
        String transactionID = request.getParameter("id");
        return tm.getTransaction(Integer.parseInt(transactionID));
    }

    @ResponseBody
    @RequestMapping(value = "/approveTransaction.jsp", method = RequestMethod.POST)
    public boolean approveTransaction (ServletRequest request, Principal principal){
        try{
            System.out.println("*************** Approve Transaction **********");
            TransactionManager tm = new TransactionManagerImpl();
            Map<Integer, String> statusMap = new HashMap<Integer, String>();
            Map parameterMap = request.getParameterMap();
            for (Object key : parameterMap.keySet()) {
                if (key.toString().contains("status_")) {
                    if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                        String[] split = key.toString().split("_");
                        String transactionID = split[1];
                        String incomingStatus = ((String[]) parameterMap.get(key))[0];
                        String actualStatus;
                        if (incomingStatus.equals("Approve")){
                            actualStatus = TransactionConstants.APPROVED_STATUS;
                        } else if (incomingStatus.equals("Disapprove")) {
                            actualStatus = TransactionConstants.DISAPPROVED_STATUS;
                        } else {
                            actualStatus = TransactionConstants.PENDING_STATUS;
                        }
                        statusMap.put(Integer.parseInt(transactionID), actualStatus);
                    }
                }
            }

            for (int transactionID : statusMap.keySet()){
                if (statusMap.get(transactionID).equals(TransactionConstants.APPROVED_STATUS)){
                    tm.approveTransaction(transactionID, principal.getName());
                }else if (statusMap.get(transactionID).equals(TransactionConstants.DISAPPROVED_STATUS)){
                    tm.disapproveTransaction(transactionID, principal.getName());
                }
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
