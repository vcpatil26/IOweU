package org.ioweu.controller;

import org.ioweu.api.TransactionManager;
import org.ioweu.impl.RecentActivity;
import org.ioweu.impl.TransactionManagerImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RecentActivityController {

    @ResponseBody
    @RequestMapping(value = "/getRecentActivity.jsp", method = RequestMethod.GET)
    public List<String> getRecentActivity (Principal principal){
        List<String> recentActMsgs = new ArrayList<String>();
        TransactionManager tm = new TransactionManagerImpl();
        List<RecentActivity> transactions = tm.getChangedTransactions(principal.getName());
        for (RecentActivity rca : transactions){
            String msg = rca.getUsername() + " " + rca.getTrStatus() + " the transaction " + rca.getTid() + " on "
                    + rca.getTrDescription();
            recentActMsgs.add(msg);
        }
        return recentActMsgs;
    }


}
