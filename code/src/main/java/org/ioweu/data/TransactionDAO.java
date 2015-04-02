package org.ioweu.data;

import org.ioweu.impl.*;
import org.ioweu.utils.DBUtils;
import org.ioweu.utils.TransactionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class TransactionDAO {
    private static Logger log = LoggerFactory.getLogger(TransactionDAO.class);

    public void addTransaction (Transaction transaction){

        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "INSERT INTO BILLINGTRANSACTION (date,addedby,gname,amount,description,status) VALUES (?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO SHAREBILL VALUES (?,?,?,?,?)";
        String sql3 = "INSERT INTO CATEGORIZEDTRANSACTION VALUES (?,?,?)";
        String sql4 = "INSERT INTO RECURRINGTRANSACTION VALUES (?,?,?)";

        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement1 = connection.prepareStatement(sql1);

            preparedStatement1.setTimestamp(1, new Timestamp(transaction.getDate().getTime()));
            preparedStatement1.setString(2, transaction.getTransactionOwner());
            preparedStatement1.setString(3, transaction.getGroup().getGroupName());
            preparedStatement1.setDouble(4, transaction.getAmount());
            preparedStatement1.setString(5, transaction.getDescription());
            preparedStatement1.setString(6, transaction.getStatus());
            preparedStatement1.executeUpdate();

            int transactionID = getTransactionID(connection);
            transaction.setTransactionID(transactionID);
            List<User> sharedUsers = transaction.getSharedUsers();
            Map<String,Double> sharedPercentage = transaction.getSharedPercentage();
            preparedStatement2 = connection.prepareStatement(sql2);
            if (sharedUsers != null && sharedUsers.size() != 0){
                for (User user : sharedUsers){
                    double sharedAmount = 0.0;
                    Double percentage = sharedPercentage.get(user.getUserName());
                    preparedStatement2.setInt(1, transaction.getTransactionID());
                    preparedStatement2.setString(2, user.getUserName());
                    if (user.getUserName().equals(transaction.getTransactionOwner())){
                        sharedAmount = getSharedAmount(transaction.getAmount(), percentage, true);
                        preparedStatement2.setDouble(3, sharedAmount);
                        preparedStatement2.setString(4, TransactionConstants.APPROVED_STATUS);
                    }else {
                        sharedAmount = getSharedAmount(transaction.getAmount(), percentage, false);
                        preparedStatement2.setDouble(3, sharedAmount);
                        preparedStatement2.setString(4, TransactionConstants.PENDING_STATUS);
                    }
                    preparedStatement2.setString(5, null);
                    preparedStatement2.executeUpdate();
                }
            }

            Map<String, Double> categories = transaction.getCategories();
            if (categories != null && categories.size() != 0){
                preparedStatement3 = connection.prepareStatement(sql3);
                for (String category : categories.keySet()){
                    preparedStatement3.setInt(1, transaction.getTransactionID());
                    preparedStatement3.setString(2, category);
                    preparedStatement3.setDouble(3, categories.get(category));
                    preparedStatement3.executeUpdate();
                }
            }
            if (transaction.getTransactionType().equals(TransactionConstants.RECURRING_TR)){
                preparedStatement4 = connection.prepareStatement(sql4);

                preparedStatement4.setInt(1, transaction.getTransactionID());
                preparedStatement4.setString(2, ((RecurringTransaction)transaction).getRecurringType());
                preparedStatement4.setString(3, String.valueOf(((RecurringTransaction)transaction).getRecurringTime()));
                preparedStatement4.executeUpdate();
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if (preparedStatement4 != null){
                    preparedStatement4.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRecurringTransaction (RecurringTransaction rctransaction){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql = "INSERT INTO RECURRINGTRANSACTION VALUES (?,?,?)";
        PreparedStatement preparedStatement = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, getTransactionID(connection));
            preparedStatement.setString(2, rctransaction.getRecurringType());
            preparedStatement.setString(3, String.valueOf(rctransaction.getRecurringTime()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement != null){
                    preparedStatement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public double getSharedAmount (double amount, double percentage, boolean isowner) {
        if (isowner){
            return amount * percentage/100;
        } else {
            return -amount * percentage/100;
        }

    }

    public int getTransactionID (Connection connection){
        String sql = "SELECT LAST_INSERT_ID()";
        int transactionID = 0;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null){
                while (resultSet.next()) {
                    transactionID = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement != null){
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return transactionID;
    }

    public void approveTransaction (int transactionID, String username){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "UPDATE SHAREBILL SET status = ? WHERE tid = ? AND uname = ?" ;
        String sql2 = "UPDATE BILLINGTRANSACTION SET status = ? WHERE tid = ? " ;
        String sql3 = "INSERT INTO TRACKBT(uname, tid, description) VALUES (?,?,?)" ;
        String sql4 = "UPDATE TRACKBT SET description = ? WHERE tid = ? AND uname = ?";
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, TransactionConstants.APPROVED_STATUS);
            preparedStatement1.setInt(2, transactionID);
            preparedStatement1.setString(3, username);
            preparedStatement1.executeUpdate();

            if (isTrackBTEntryExists(username, transactionID)){
                preparedStatement4 = connection.prepareStatement(sql4);
                preparedStatement4.setString(1, TransactionConstants.APPROVED_STATUS);
                preparedStatement4.setInt(2, transactionID);
                preparedStatement4.setString(3, username);
                preparedStatement4.executeUpdate();
            }else {
                preparedStatement3 = connection.prepareStatement(sql3);
                preparedStatement3.setString(1, username);
                preparedStatement3.setInt(2, transactionID);
                preparedStatement3.setString(3, TransactionConstants.APPROVED_STATUS);
                preparedStatement3.executeUpdate();
            }
            if (canUpdateBT(transactionID)){
                preparedStatement2 = connection.prepareStatement(sql2);
                preparedStatement2.setString(1, TransactionConstants.APPROVED_STATUS);
                preparedStatement2.setInt(2, transactionID);
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canUpdateBT (int transactionID){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT * FROM SHAREBILL s WHERE s.tid = ? AND s.status != ? ";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setInt(1, transactionID);
            statement.setString(2, TransactionConstants.APPROVED_STATUS);
            ResultSet resultSet = statement.executeQuery();
            boolean found;
            if (resultSet.next()) {
               found = false;
            } else {
               found = true;
            }
            statement.close();
            connection.close();
            return found;
        }  catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void disapproveTransaction (int transactionID, String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "UPDATE SHAREBILL SET status = ? WHERE tid = ? and uname = ? " ;
        String sql2 = "UPDATE BILLINGTRANSACTION SET status = ? WHERE tid = ? " ;
        String sql3 = "INSERT INTO TRACKBT(uname, tid, description) VALUES (?,?,?)" ;
        String sql4 = "UPDATE TRACKBT SET description = ? WHERE tid = ? AND uname = ?";
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement1.setString(1, TransactionConstants.DISAPPROVED_STATUS);
            preparedStatement1.setInt(2, transactionID);
            preparedStatement1.setString(3, userName);
            preparedStatement1.executeUpdate();

            preparedStatement2.setString(1, TransactionConstants.DISAPPROVED_STATUS);
            preparedStatement2.setInt(2, transactionID);
            preparedStatement2.executeUpdate();

            if (isTrackBTEntryExists(userName, transactionID)){
                preparedStatement4 = connection.prepareStatement(sql4);
                preparedStatement4.setString(1, TransactionConstants.DISAPPROVED_STATUS);
                preparedStatement4.setInt(2, transactionID);
                preparedStatement4.setString(3, userName);
                preparedStatement4.executeUpdate();
            }else {
                preparedStatement3 = connection.prepareStatement(sql3);
                preparedStatement3.setString(1, userName);
                preparedStatement3.setInt(2, transactionID);
                preparedStatement3.setString(3, TransactionConstants.DISAPPROVED_STATUS);
                preparedStatement3.executeUpdate();
            }

        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        } finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if (preparedStatement4 != null){
                    preparedStatement4.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isTrackBTEntryExists (String uname, int tid){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT * FROM TRACKBT t WHERE t.tid = ? AND t.uname = ?";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setInt(1, tid);
            statement.setString(2, uname);
            ResultSet resultSet = statement.executeQuery();
            boolean found;
            if (resultSet.next()) {
                found = true;
            } else {
                found = false;
            }
            statement.close();
            connection.close();
            return found;
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void editTransaction(Transaction transaction) {
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "UPDATE BILLINGTRANSACTION SET gname = ?, amount = ?, description = ? WHERE tid = ?";
        String sql2 = "UPDATE SHAREBILL SET amount = ?, status = ? WHERE tid = ? AND uname = ?";
        String sql3 = "UPDATE CATEGORIZEDTRANSACTION SET amount = ? WHERE tid = ? AND cname = ?";

        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement1 = connection.prepareStatement(sql1);

            preparedStatement1.setString(1, transaction.getGroup().getGroupName());
            preparedStatement1.setDouble(2, transaction.getAmount());
            preparedStatement1.setString(3, transaction.getDescription());
            preparedStatement1.setInt(4, transaction.getTransactionID());
            preparedStatement1.executeUpdate();

            List<User> sharedUsers = transaction.getSharedUsers();
            Map<String,Double> sharedPercentage = transaction.getSharedPercentage();
            preparedStatement2 = connection.prepareStatement(sql2);
            if (sharedUsers != null && sharedUsers.size() != 0){
                for (User user : sharedUsers){
                    if (user.getUserName().equals(transaction.getTransactionOwner()) ){
                        Double percentage = sharedPercentage.get(user.getUserName());
                        double sharedAmount = getSharedAmount(transaction.getAmount(), percentage, true);
                        preparedStatement2.setDouble(1, sharedAmount);
                    }else {
                        Double percentage = sharedPercentage.get(user.getUserName());
                        double sharedAmount = getSharedAmount(transaction.getAmount(), percentage, false);
                        preparedStatement2.setDouble(1, sharedAmount);
                    }
                    preparedStatement2.setString(2, TransactionConstants.APPROVED_STATUS);
                    preparedStatement2.setInt(3, transaction.getTransactionID());
                    preparedStatement2.setString(4, user.getUserName());
                    preparedStatement2.executeUpdate();
                }
            }

            Map<String, Double> categories = transaction.getCategories();
            if (categories != null && categories.size() != 0){
                preparedStatement3 = connection.prepareStatement(sql3);
                for (String category : categories.keySet()){
                    preparedStatement3.setDouble(1, categories.get(category));
                    preparedStatement3.setInt(2, getTransactionID(connection));
                    preparedStatement3.setString(3, category);
                    preparedStatement3.executeUpdate();
                }
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void editRecurringTransaction (RecurringTransaction rTransaction){

        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "UPDATE BILLINGTRANSACTION SET gname = ?, amount = ?, description = ? WHERE tid = ?";
        String sql2 = "UPDATE SHAREBILL SET amount = ?, status = ? WHERE tid = ? AND uname = ?";
        String sql3 = "UPDATE CATEGORIZEDTRANSACTION SET amount = ? WHERE tid = ? AND cname = ?";
        String sql4 = "UPDATE RECURRINGTRANSACTION SET isweekly = ?, day = ? WHERE tid = ?";

        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement1 = connection.prepareStatement(sql1);

            preparedStatement1.setString(1, rTransaction.getGroup().getGroupName());
            preparedStatement1.setDouble(2, rTransaction.getAmount());
            preparedStatement1.setString(3, rTransaction.getDescription());
            preparedStatement1.setInt(4, rTransaction.getTransactionID());
            preparedStatement1.executeUpdate();

            preparedStatement4 = connection.prepareStatement(sql4);

            preparedStatement4.setString(1, rTransaction.getRecurringType());
            preparedStatement4.setInt(2, rTransaction.getRecurringTime());
            preparedStatement4.setInt(3, rTransaction.getTransactionID());
            preparedStatement4.executeUpdate();

            List<User> sharedUsers = rTransaction.getSharedUsers();
            Map<String,Double> sharedPercentage = rTransaction.getSharedPercentage();
            preparedStatement2 = connection.prepareStatement(sql2);
            if (sharedUsers != null && sharedUsers.size() != 0){
                for (User user : sharedUsers){
                    if (user.getUserName().equals(rTransaction.getTransactionOwner()) ){
                        Double percentage = sharedPercentage.get(user.getUserName());
                        double sharedAmount = getSharedAmount(rTransaction.getAmount(), percentage, true);
                        preparedStatement2.setDouble(1, sharedAmount);
                    }else {
                        Double percentage = sharedPercentage.get(user.getUserName());
                        double sharedAmount = getSharedAmount(rTransaction.getAmount(), percentage, false);
                        preparedStatement2.setDouble(1, sharedAmount);
                    }

                    preparedStatement2.setString(2, TransactionConstants.APPROVED_STATUS);
                    preparedStatement2.setInt(3, rTransaction.getTransactionID());
                    preparedStatement2.setString(4, user.getUserName());
                    preparedStatement2.executeUpdate();
                }
            }

            Map<String, Double> categories = rTransaction.getCategories();
            if (categories != null && categories.size() != 0){
                preparedStatement3 = connection.prepareStatement(sql3);
                for (String category : categories.keySet()){
                    preparedStatement3.setDouble(1, categories.get(category));
                    preparedStatement3.setInt(2, getTransactionID(connection));
                    preparedStatement3.setString(3, category);
                    preparedStatement3.executeUpdate();
                }
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if (preparedStatement4 != null){
                    preparedStatement4.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteTransaction (int transactionID){

        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "DELETE FROM BILLINGTRANSACTION WHERE tid = ?";
        //rest of the tables should cascade on delete
        /*String sql2 = "DELETE FROM SHAREBILL VALUES WHERE tid = ?";
        String sql3 = "DELETE FROM CATEGORIZEDTRANSACTION WHERE tid = ?";*/

        PreparedStatement preparedStatement1 = null;
        /*PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;*/

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());

            preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, transactionID);
            preparedStatement1.executeUpdate();

            /*preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, transactionID);
            preparedStatement2.executeUpdate();

            preparedStatement3 = connection.prepareStatement(sql3);
            preparedStatement3.setInt(1, transactionID);
            preparedStatement3.executeUpdate();*/

        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        }  finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                /*if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }*/
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteRecurringTransaction (int transactionID){

        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql1 = "DELETE FROM BILLINGTRANSACTION WHERE tid = ?";
        //rest of the tables should cascade on delete
        /*String sql2 = "DELETE FROM SHAREBILL VALUES WHERE tid = ?";
        String sql3 = "DELETE FROM CATEGORIZEDTRANSACTION WHERE tid = ?";
        String sql4 = "DELETE FROM RECURRINGTRANSACTION WHERE tid = ?";*/

        PreparedStatement preparedStatement1 = null;
        /*PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;*/

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());

            preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, transactionID);
            preparedStatement1.executeUpdate();

            /*preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, transactionID);
            preparedStatement2.executeUpdate();

            preparedStatement3 = connection.prepareStatement(sql3);
            preparedStatement3.setInt(1, transactionID);
            preparedStatement3.executeUpdate();

            preparedStatement4 = connection.prepareStatement(sql3);
            preparedStatement4.setInt(1, transactionID);
            preparedStatement4.executeUpdate();*/

        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        } finally {
            try{
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                /*if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if (preparedStatement4 != null){
                    preparedStatement4.close();
                }*/
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public SimplifiedReport generateSimplifiedReport (List<User> userList){
        if(userList.size() < 2)
            return null;

        List<Transaction> allTransactions = getAllTransactions(new Date(System.currentTimeMillis()));
        Hashtable<String, Double> userTotal = new Hashtable<String, Double>();

        List<String> users = new ArrayList<String>();
        for(User u : userList)
        {
            userTotal.put(u.getUserName(), 0.0);
            users.add(u.getUserName());
        }

        for(Transaction t : allTransactions)
        {
            if(!users.contains(t.getTransactionOwner()))
                continue;

            String owner = t.getTransactionOwner();
            for(Map.Entry<String, Double> kv : t.getSharedAmount().entrySet())
            {
                if(kv.getKey().equals(owner))
                    continue;

                if(t.getSharedStatus().get(kv.getKey()).equals(TransactionConstants.SETTLED_STATUS))
                    continue;

                if(!users.contains(kv.getKey()))
                    continue;

                userTotal.put(kv.getKey(), userTotal.get(kv.getKey()) + kv.getValue());
                userTotal.put(owner, userTotal.get(owner) - kv.getValue());
            }
        }

        Hashtable<String, Double> payUsers = new Hashtable<String, Double>();
        Hashtable<String, Double> recUsers = new Hashtable<String, Double>();

        for(Map.Entry<String, Double> kv: userTotal.entrySet())
        {
            if(kv.getValue() < 0)
            {
                payUsers.put(kv.getKey(), -kv.getValue());
            }
            else if(kv.getValue() > 0)
            {
                recUsers.put(kv.getKey(), kv.getValue());
            }
        }

        List<DebtUser> report = new ArrayList<DebtUser>();

        while(recUsers.size() != 0)
        {
            String curPay = null;
            String curRec = null;

            if(payUsers.size() > recUsers.size())
            {
                curPay = findMaxUser(payUsers);
                curRec = matchUsers(payUsers.get(curPay), recUsers);
            }
            else
            {
                curRec = findMaxUser(recUsers);
                curPay = matchUsers(recUsers.get(curRec), payUsers);
            }

            DebtUser du = new DebtUser();
            report.add(du);
            du.setUserName(curPay);
            du.setOwetouser(curRec);

            if(recUsers.get(curRec) > payUsers.get(curPay))
            {
                recUsers.put(curRec, recUsers.get(curRec) - payUsers.get(curPay));
                du.setAmount(payUsers.get(curPay));
                payUsers.remove(curPay);
            }
            else if(recUsers.get(curRec) < payUsers.get(curPay))
            {
                payUsers.put(curPay, payUsers.get(curPay) - recUsers.get(curRec));
                du.setAmount(recUsers.get(curRec));
                recUsers.remove(curRec);
            }
            else
            {
                du.setAmount(recUsers.get(curRec));
                recUsers.remove(curRec);
                payUsers.remove(curPay);
            }
        }

        SimplifiedReport sr = new SimplifiedReport();
        sr.setSettlingUsers(report);
        return sr;
    }

    private String matchUsers(Double amt, Hashtable<String, Double> userAmt)
    {
        String minUser = null;
        Double minValue = Double.MAX_VALUE, curValue;

        String maxUser = null;
        Double maxValue = Double.MIN_VALUE;

        for(Map.Entry<String, Double> kv : userAmt.entrySet())
        {
            curValue = Math.abs(amt - kv.getValue());
            if(minValue > curValue)
            {
                minUser = kv.getKey();
                minValue = curValue;
            }

            if(maxValue < kv.getValue())
            {
                maxUser = kv.getKey();
                maxValue = kv.getValue();
            }
        }

        if(maxValue < amt)
        {
            return maxUser;
        }
        else
        {
            return minUser;
        }
    }

    private String findMaxUser(Hashtable<String, Double> userAmt)
    {
        String maxUser = null;
        Double maxValue = Double.MIN_VALUE;

        for(Map.Entry<String, Double> kv : userAmt.entrySet())
        {
            if(maxValue < kv.getValue())
            {
                maxUser = kv.getKey();
                maxValue = kv.getValue();
            }
        }

        return maxUser;
    }

    public List<Transaction> getAllTransactions (java.util.Date date){

            UserDAO userDAO = new UserDAO();
            Dictionary<String, Group> groups = new Hashtable<String, Group>();
            Dictionary<String, User> users = new Hashtable<String, User>();
            List<Transaction> transactionList = new ArrayList<Transaction>();
            Transaction transaction = null;
            String connectionURL =  DBUtils.getJDBCUrl();
            Connection connection = null;

            String sql1 = "SELECT tid, date, addedby, gname, amount, description, status FROM BILLINGTRANSACTION WHERE date < ? AND tid NOT IN (SELECT tid FROM RECURRINGTRANSACTION)";
            String sql2 = "SELECT uname, amount, status, msg FROM SHAREBILL WHERE tid = ?";
            String sql3 = "SELECT cname, amount FROM CATEGORIZEDTRANSACTION WHERE tid = ?";

            PreparedStatement preparedStatement1 = null;
            PreparedStatement preparedStatement2 = null;
            PreparedStatement preparedStatement3 = null;

            try {
                connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
                preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement2 = connection.prepareStatement(sql2);
                preparedStatement3 = connection.prepareStatement(sql3);

                preparedStatement1.setTimestamp(1, new Timestamp(date.getTime()));
                ResultSet billingTransaction = preparedStatement1.executeQuery();

                while(billingTransaction.next())
                {
                    transaction = new BillingTransaction();
                    transaction.setTransactionType(TransactionConstants.NORMAL_TR);
                    transaction.setTransactionID(billingTransaction.getInt(1));
                    transaction.setDate(billingTransaction.getTimestamp(2));
                    transaction.setTransactionOwner(billingTransaction.getString(3));
                    String gname = billingTransaction.getString(4);
                    Group group = null;
                    if(gname != null && (group = groups.get(gname)) == null)
                    {
                        group = getGroup(gname);
                        groups.put(gname, group);
                    }
                    transaction.setGroup(group);
                    transaction.setAmount(billingTransaction.getDouble(5));
                    transaction.setDescription(billingTransaction.getString(6));
                    transaction.setStatus(billingTransaction.getString(7));

                    preparedStatement2.setInt(1, transaction.getTransactionID());
                    ResultSet sharedBy = preparedStatement2.executeQuery();

                    List<User> sharedUsers = new ArrayList<User>();
                    Map<String, Double> sharedAmount = new Hashtable<String, Double>();
                    Map<String, SharedStatus> sharedStatus = new Hashtable<String, SharedStatus>();
                    while(sharedBy.next())
                    {
                        User sUser = null;
                        String uname = sharedBy.getString(1);
                        if(uname != null && (sUser = users.get(uname)) == null)
                        {
                            if(userDAO.isRegisteredUser(uname))
                            {
                                sUser = userDAO.getRegisteredtUser(uname);
                            }
                            else
                            {
                                sUser = userDAO.getPrivatetUser(uname);
                            }
                        }
                        if(sUser != null)
                        {
                            sharedUsers.add(sUser);
                            sharedAmount.put(uname, sharedBy.getDouble(2));
                            SharedStatus ss = new SharedStatus();
                            ss.setSharedStatus(sharedBy.getString(3));
                            ss.setStatusMsg(sharedBy.getString(4));
                            sharedStatus.put(uname, ss);
                        }
                    }
                    transaction.setSharedUsers(sharedUsers);
                    transaction.setSharedStatus(sharedStatus);
                    transaction.setSharedAmount(sharedAmount);

                    preparedStatement3.setInt(1, transaction.getTransactionID());
                    ResultSet catTrxn = preparedStatement3.executeQuery();

                    Map<String, Double> categories = new Hashtable<String, Double>();
                    while(catTrxn.next())
                    {
                        categories.put(catTrxn.getString(1), catTrxn.getDouble(2));
                    }
                    transaction.setCategories(categories);
                    transactionList.add(transaction);
                }

            } catch (SQLException e) {
                String error = "Error persisting the transaction.";
                log.error(error, e);
            }  finally {
                try{
                    if (preparedStatement1 != null){
                        preparedStatement1.close();
                    }
                    if (preparedStatement2 != null){
                        preparedStatement2.close();
                    }
                    if (preparedStatement3 != null){
                        preparedStatement3.close();
                    }
                    if  (connection != null){
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return transactionList;
    }

    public void settleTransaction(List<User> userList, Date date){

        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;

        String sql = "UPDATE SHAREBILL SET status = ? WHERE tid = ? AND uname = ?";
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, TransactionConstants.SETTLED_STATUS);

            List<Transaction> allTransactions = getAllTransactions(date);

            List<String> users = new ArrayList<String>();
            for(User u : userList)
            {
                users.add(u.getUserName());
            }

            for(Transaction t : allTransactions)
            {
                if(!users.contains(t.getTransactionOwner()))
                    continue;

                String owner = t.getTransactionOwner();
                for(Map.Entry<String, Double> kv : t.getSharedAmount().entrySet())
                {
                    if(kv.getKey().equals(owner))
                        continue;

                    if(t.getSharedStatus().get(kv.getKey()).equals(TransactionConstants.SETTLED_STATUS))
                        continue;

                    if(!users.contains(kv.getKey()))
                        continue;

                    preparedStatement.setInt(2, t.getTransactionID());
                    preparedStatement.setString(3, kv.getKey());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            String error = "Error persisting the transaction.";
            log.error(error, e);
        } finally {
            try{
                if (preparedStatement != null){
                    preparedStatement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Transaction> getPendingTransactions(String user){
        List<Transaction> transactions = new ArrayList<Transaction>();
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT bt.tid, bt.date, bt.addedby, bt.gname, bt.description, bt.amount, s.amount, s.status, s.msg FROM " +
                "BILLINGTRANSACTION bt, SHAREBILL s WHERE bt.tid = s.tid AND s.uname = ?  AND s.status != ?";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setString(1, user);
            statement.setString(2, TransactionConstants.APPROVED_STATUS);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int transactionID = resultSet.getInt(1);
                Timestamp date = resultSet.getTimestamp(2);
                String transactionOwner = resultSet.getString(3);
                String group = resultSet.getString(4);
                String description = resultSet.getString(5);
                Double transactionAmount = resultSet.getDouble(6);
                Double sharedAmount = resultSet.getDouble(7);
                String status = resultSet.getString(8);
                String msg = resultSet.getString(9);

                Transaction transaction = new BillingTransaction();
                transaction.setTransactionID(transactionID);
                transaction.setDate(getDate(date));
                transaction.setTransactionOwner(transactionOwner);
                transaction.setAmount(transactionAmount);
                Map<String, Double> sharedAmountMap = new HashMap<String, Double>();
                sharedAmountMap.put(user, sharedAmount);
                transaction.setSharedAmount(sharedAmountMap);
                transaction.setDescription(description);
                transaction.setGroup(getGroup(group));
                transaction.setStatus(status);
                transaction.setStatusMsg(msg);
                transactions.add(transaction);
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }

    public Transaction getTransaction(int transactionID){
        Transaction transaction = new BillingTransaction();
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT bt.date, bt.addedby, bt.gname, bt.description, bt.amount, bt.status, s.uname, s.amount, s.status, s.msg FROM " +
                "BILLINGTRANSACTION bt, SHAREBILL s WHERE bt.tid =  ? AND bt.tid = s.tid ";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setInt(1, transactionID);
            ResultSet resultSet = statement.executeQuery();
            Map<String, Double> sharedAmountMap = new HashMap<String, Double>();
            Map<String, SharedStatus> sharedStatusMap = new HashMap<String, SharedStatus>();
            while (resultSet.next()) {
                Timestamp date = resultSet.getTimestamp(1);
                String transactionOwner = resultSet.getString(2);
                String group = resultSet.getString(3);
                String description = resultSet.getString(4);
                Double transactionAmount = resultSet.getDouble(5);
                String btStatus = resultSet.getString(6);
                String sharedUser = resultSet.getString(7);
                Double sharedAmount = resultSet.getDouble(8);
                String sharedStatus = resultSet.getString(9);
                String sharedStatusMsg = resultSet.getString(10);

                if (transaction.getTransactionID() != 0 && transaction.getTransactionID() == transactionID ){
                    sharedAmountMap.put(sharedUser, sharedAmount);
                    SharedStatus shStatus = new SharedStatus();
                    shStatus.setStatusMsg(sharedStatusMsg);
                    shStatus.setSharedStatus(sharedStatus);
                    sharedStatusMap.put(sharedUser, shStatus);
                }else {
                    transaction.setTransactionID(transactionID);
                    transaction.setDate(getDate(date));
                    transaction.setTransactionOwner(transactionOwner);
                    transaction.setAmount(transactionAmount);
                    transaction.setDescription(description);
                    transaction.setGroup(getGroup(group));
                    transaction.setStatus(btStatus);
                    sharedAmountMap.put(sharedUser, sharedAmount);
                    SharedStatus shStatus = new SharedStatus();
                    shStatus.setStatusMsg(sharedStatusMsg);
                    shStatus.setSharedStatus(sharedStatus);
                    sharedStatusMap.put(sharedUser, shStatus);
                }
            }
            transaction.setSharedAmount(sharedAmountMap);
            transaction.setSharedStatus(sharedStatusMap);
            statement.close();
            connection.close();
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return transaction;
    }

    public List<RecentActivity> getChangedTransactions (String userName){
        List<RecentActivity> recentActivityList = new ArrayList<RecentActivity>();
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query = "SELECT t.uname, t.tid, t.description, b.description FROM TRACKBT t, BILLINGTRANSACTION b WHERE t.tid IN " +
                "(SELECT distinct b.tid from BILLINGTRANSACTION b, SHAREBILL s where b.tid = s.tid and s.uname = ?) " +
                "AND t.tid = b.tid";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String uname = resultSet.getString(1);
                int transactionID = resultSet.getInt(2);
                String status = resultSet.getString(3);
                String desc = resultSet.getString(4);
                RecentActivity recentActivity = new RecentActivity();
                recentActivity.setUsername(uname);
                recentActivity.setLoggedinUser(userName);
                recentActivity.setTrStatus(status);
                recentActivity.setTrDescription(desc);
                recentActivity.setTid(transactionID);
                recentActivityList.add(recentActivity);
            }
            statement.close();
            connection.close();
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return recentActivityList;
    }



    private Group getGroup (String groupName){
        GroupDAO groupDAO = new GroupDAO();
        List<String> usersPerGroup = groupDAO.getUsersPerGroup(groupName);
        Group group = new Group();
        group.setGroupName(groupName);
        group.setMembersList(usersPerGroup);
        return group;

    }

    private java.util.Date getDate (Timestamp timestamp){
        return new Date(timestamp.getTime());
    }

    public List<User> getUsersAssociate (String username){
        List<User> usersList = new ArrayList<User>();
        UserDAO userDAO = new UserDAO();
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query = "SELECT distinct s2.uname FROM SHAREBILL s1, SHAREBILL s2 WHERE s1.tid = s2.tid and s1.uname = ?";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String uname = resultSet.getString(1);
                if (!uname.equals(username)){
                    if (userDAO.isRegisteredUser(uname)){
                        usersList.add(userDAO.getRegisteredtUser(uname));
                    } else {
                        usersList.add(userDAO.getPrivatetUser(uname));
                    }
                }
            }
            statement.close();
            connection.close();
        }  catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            try{
                if (statement != null){
                    statement.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return usersList;
    }

    public List<DebtUser> getUsersInDebt(String uname){

        List<DebtUser> debtList = new ArrayList<DebtUser>();
        List<Transaction> allTransactions = getAllTransactions(new Date(System.currentTimeMillis()));
        Hashtable<String, Double> userTotal = new Hashtable<String, Double>();
        Double amt;
        for(Transaction t : allTransactions)
        {
            if(uname.equals(t.getTransactionOwner()))
            {
                for(Map.Entry<String, Double> kv : t.getSharedAmount().entrySet())
                {
                    if(t.getSharedStatus().get(kv.getKey()).equals(TransactionConstants.SETTLED_STATUS))
                        continue;

                    if(uname.equals(kv.getKey()))
                        continue;

                    amt = userTotal.get(kv.getKey());
                    if(amt == null)
                        amt = 0.0;
                    amt += -kv.getValue();
                    userTotal.put(kv.getKey(), amt);

                }
            }
            else if(!uname.equals(t.getTransactionOwner()))
            {
                for(Map.Entry<String, Double> kv : t.getSharedAmount().entrySet())
                {
                    if(t.getSharedStatus().get(kv.getKey()).equals(TransactionConstants.SETTLED_STATUS))
                        continue;

                    if(uname.equals(kv.getKey()))
                    {
                        amt = userTotal.get(t.getTransactionOwner());
                        if(amt == null)
                            amt = 0.0;
                        amt += kv.getValue();
                        userTotal.put(t.getTransactionOwner(), amt);
                    }
                }
            }

        }

        List<DebtUser> report = new ArrayList<DebtUser>();

        for(Map.Entry<String, Double> kv : userTotal.entrySet())
        {
            DebtUser du = new DebtUser();
            if(kv.getValue() < 0)
            {
                du.setUserName(uname);
                du.setOwetouser(kv.getKey());
                du.setAmount(-kv.getValue());
            }
            else if(kv.getValue() > 0)
            {
                du.setUserName(kv.getKey());
                du.setOwetouser(uname);
                du.setAmount(kv.getValue());
            }
            report.add(du);
        }
        return report;
    }
}
