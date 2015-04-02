package org.ioweu.controller;

import org.ioweu.api.GroupManager;
import org.ioweu.api.TransactionManager;
import org.ioweu.api.UserManager;
import org.ioweu.impl.*;
import org.ioweu.service.GroupService;
import org.ioweu.utils.IOweUConstants;
import org.ioweu.utils.TransactionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

@Controller
public class DashboardController {
    @Autowired
    private GroupService groupService;

    @ResponseBody
    @RequestMapping(value = "/getLoggedUser.jsp", method = RequestMethod.GET)
    public AuthorizedUser getUserName (Principal principal){
        System.out.println("*************** Logged user : " + principal.getName());
        AuthorizedUser authorizedUser = new AuthorizedUser();
        if (principal.getName() != null){
            authorizedUser.setAuthorized(true);
            authorizedUser.setAuthUser(principal.getName());
            authorizedUser.setMessage("User authorized");
        } else {
            authorizedUser.setAuthorized(false);
            authorizedUser.setAuthUser(null);
            authorizedUser.setMessage("User is not authorized");
        }
        return authorizedUser;
    }

    @ResponseBody
    @RequestMapping(value = "/addGroup.jsp", method = RequestMethod.POST)
    public ModelAndView addGroup(HttpServletRequest request, Principal principal) {
        System.out.println("***************** add group ************");
        try{
            String groupName = request.getParameter("groupName");
            List<String> userList = new ArrayList<String>();
            String groupOwner = principal.getName();
            userList.add(groupOwner);
            Map parameterMap = request.getParameterMap();
            for (Object key : parameterMap.keySet()){
                if (key.toString().contains("userName"))
                    if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                        String name = ((String [])parameterMap.get(key))[0];
                        userList.add(name);
                    }
            }
            GroupManager gm = new GroupManagerImpl();
            Group group = new Group();
            group.setGroupName(groupName);
            group.setMembersList(userList);
            gm.addGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(IOweUConstants.DASHBOARD);

    }

    @ResponseBody
    @RequestMapping(value = "/addPrivateUser.jsp", method = RequestMethod.POST)
    public ModelAndView addPrivateUser(@RequestParam(value="firstName", required=true) String firstName,
                                       @RequestParam(value="lastName", required=true) String lastName,
                                       @RequestParam(value="userName", required=true) String userName,
                                       @RequestParam(value ="registered", required = true) boolean registered,
                                       Principal principal) {
        try{
            System.out.println("***************adding private user *****************");
            UserManager userManager = new UserManagerImpl();
            User privateUser = new PrivateUser();
            privateUser.setUserName(principal.getName() + "_" + userName);
            privateUser.setRegistered(registered);
            privateUser.setFirstName(firstName);
            privateUser.setLastName(lastName);
            userManager.addUser(privateUser);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(IOweUConstants.DASHBOARD);
    }

    @ResponseBody
    @RequestMapping(value = "/getGroups.jsp", method = RequestMethod.GET)
    public List<Group> getGroups(Principal principal) {
        System.out.println("*************** get groups *****************");
         return groupService.getGroups(principal.getName());
    }

    @ResponseBody
    @RequestMapping(value = "/getUsersPerGroup.jsp", method = RequestMethod.GET)
    public List<String> getUsersPerGroup(@RequestParam(value="groupName", required=true) String groupName) {
        System.out.println("*************** get users per group *****************");
        List<String> usersPerGroup = new ArrayList<String>();
        try{
            GroupManager gm = new GroupManagerImpl();
            usersPerGroup = gm.getUsersPerGroup(groupName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usersPerGroup;
    }

    @ResponseBody
    @RequestMapping(value = "/getPrivateUsers.jsp", method = RequestMethod.GET)
    public List<String> getUsers(Principal principal) {
        System.out.println("*************** get Private Users *****************");
        List<String> priUserNames = new ArrayList<String>();
        try{
            UserManager um = new UserManagerImpl();
            List<String> privateUsers = um.getPrivateUserList(principal.getName());
            for (String pUser : privateUsers){
                String[] splits = pUser.split("_");
                String priUserName = splits[splits.length - 1];
                priUserNames.add(priUserName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priUserNames;
    }

    @ResponseBody
    @RequestMapping(value = "/addTransaction.jsp", method = RequestMethod.POST)
    public boolean addTransaction(HttpServletRequest request, Principal principal) {
        try {
            Transaction transaction = null;
            HashMap<String, Double> sharedPercentages = new HashMap<String, Double>();
            Map<String, Double> newSharedMap = new HashMap<String, Double>();
            System.out.println("*********** Adding Transaction ***********");
            GroupManager gm = new GroupManagerImpl();
            UserManager um = new UserManagerImpl();
            String category = request.getParameter("category");
            String description = request.getParameter("description");
            String totalAmount = request.getParameter("totalAmount");
            String groupName = request.getParameter("group");
            String recurringTr = request.getParameter("recurringTr");

            List<String> usersPerGroup = gm.getUsersPerGroup(groupName);
            Group group = new Group();
            group.setGroupName(groupName);
            group.setMembersList(usersPerGroup);
            List<User> users = new ArrayList<User>();
            users.add(um.getRegisteredUser(principal.getName()));
            Map<String, Double> categoryMap = new HashMap<String, Double>();
            if (category != null && !category.equals("None")){
                categoryMap.put(category, Double.parseDouble(totalAmount));
            }

            Map<Integer, String> userMap = new HashMap<Integer, String>();
            Map<Integer, String> percentageMap = new HashMap<Integer, String>();

            Map parameterMap = request.getParameterMap();
            for (Object key : parameterMap.keySet()) {
                String username;
                String percentage;
                if (key.toString().contains("userName")) {
                    if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                        username = ((String[]) parameterMap.get(key))[0];
                        if (um.isRegisteredUser(username)){
                            users.add(um.getRegisteredUser(username));
                            String substring = key.toString().substring("username".length());
                            userMap.put(Integer.parseInt(substring), username);
                        }  else {
                            users.add(um.getPrivateUser(principal.getName() + "_" + username));
                            String substring = key.toString().substring("username".length());
                            userMap.put(Integer.parseInt(substring), principal.getName() + "_" + username);
                        }
                    }
                } else if (key.toString().contains("percentage")) {
                    if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                        percentage = ((String[]) parameterMap.get(key))[0];
                        String[] split = key.toString().split("_");
                        String substring = split[0].substring("user".length());
                        percentageMap.put(Integer.parseInt(substring), percentage);
                    }
                }
            }
            for (int userNo : userMap.keySet()) {
                for (int user : percentageMap.keySet()) {
                    if (userNo == user) {
                        sharedPercentages.put(userMap.get(user), Double.parseDouble(percentageMap.get(user)));
                    }
                }
            }

            Double accumilatePercentage = 0.00;
            Double ownerPercentage = 0.00;

            for (String user : sharedPercentages.keySet()){
                Double aDouble = sharedPercentages.get(user);
                accumilatePercentage += aDouble;
            }

            ownerPercentage = 100 - accumilatePercentage;
            sharedPercentages.put(principal.getName(), ownerPercentage);

            String shareEqually = request.getParameter("shareEqually");

            if (recurringTr != null && recurringTr.equals("on")){
                transaction = new RecurringTransaction();
                transaction.setTransactionType(TransactionConstants.RECURRING_TR);
            }  else {
                transaction = new BillingTransaction();
                transaction.setTransactionType(TransactionConstants.NORMAL_TR);
            }

            if (shareEqually != null && shareEqually.equals("on")){
                int size = sharedPercentages.size();
                Double newPercentage = new Double(100 / size) ;
                for (String user : sharedPercentages.keySet()){
                    newSharedMap.put(user, newPercentage);
                }
                transaction.setSharedPercentage(newSharedMap);
            } else {
                transaction.setSharedPercentage(sharedPercentages);
            }
            transaction.setAmount(Double.valueOf(totalAmount));
            transaction.setDescription(description);
            transaction.setGroup(group);
            transaction.setSharedUsers(users);
            transaction.setTransactionOwner(principal.getName());
            transaction.setCategories(categoryMap);
            transaction.setDate(getCurrentDate());
            transaction.setStatus(TransactionConstants.PENDING_STATUS);

            TransactionManager tm = new TransactionManagerImpl();
            tm.createTransation(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Date getCurrentDate (){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    @ResponseBody
    @RequestMapping(value = "/getUserOwe.jsp", method = RequestMethod.GET)
    public List<DebtUser> getUserOweReport(Principal principal){
        System.out.println("************* Debt Report ************");
        List<DebtUser> userOweDebts = new ArrayList<DebtUser>();
        TransactionManager tm = new TransactionManagerImpl();
        List<DebtUser> usersInDebt = tm.getUsersInDebt(principal.getName());
        for (DebtUser debtUser : usersInDebt){
            if (!debtUser.getUserName().equals(principal.getName())){
                userOweDebts.add(debtUser);
            }
        }
        return userOweDebts;
    }

    @ResponseBody
    @RequestMapping(value = "/getOthersOweReport.jsp", method = RequestMethod.GET)
    public List<DebtUser> getOthersOweReport(Principal principal){
        System.out.println("************* Debt Report ************");
        List<DebtUser> othersOweDebts = new ArrayList<DebtUser>();
        TransactionManager tm = new TransactionManagerImpl();
        List<DebtUser> usersInDebt = tm.getUsersInDebt(principal.getName());
        for (DebtUser debtUser : usersInDebt){
            if (debtUser.getUserName().equals(principal.getName())){
                othersOweDebts.add(debtUser);
            }
        }
        return othersOweDebts;
    }


    @ResponseBody
    @RequestMapping(value = "/generateReport.jsp", method = RequestMethod.GET)
    public List<DebtUser> generatetReport(HttpServletRequest request, Principal principal){
        System.out.println("************* Generate Report ************");
        List<User> userList = new ArrayList<User>();
        TransactionManager tm = new TransactionManagerImpl();
        UserManager um = new UserManagerImpl();
        Map parameterMap = request.getParameterMap();
        for (Object key : parameterMap.keySet()) {
            if (key.toString().contains("username")) {
                if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                    String username = ((String[]) parameterMap.get(key))[0];
                    if (um.isRegisteredUser(username)){
                        userList.add(um.getRegisteredUser(username));
                    }else {
                        userList.add(um.getPrivateUser(username));
                    }
                }
            }
        }
        SimplifiedReport simplifiedReport = tm.generateSimplifiedReport(userList);
        return simplifiedReport.getSettlingUsers();
    }


    @ResponseBody
    @RequestMapping(value = "/settleUp.jsp", method = RequestMethod.POST)
    public boolean settleUp(HttpServletRequest request){
        System.out.println("************* Settle up ************");
        try {
            List<User> userList = new ArrayList<User>();
            TransactionManager tm = new TransactionManagerImpl();
            UserManager um = new UserManagerImpl();
            Map parameterMap = request.getParameterMap();
            for (Object key : parameterMap.keySet()) {
                if (key.toString().contains("username")) {
                    if (parameterMap.get(key) instanceof String[] && ((String[]) parameterMap.get(key)).length > 0) {
                        String username = ((String[]) parameterMap.get(key))[0];
                        if (um.isRegisteredUser(username)){
                            userList.add(um.getRegisteredUser(username));
                        }else {
                            userList.add(um.getPrivateUser(username));
                        }
                    }
                }
            }
            tm.settleTransaction(userList, getCurrentDate());
            return true;
        } catch (Exception e){
            return false;
        }

    }

}
