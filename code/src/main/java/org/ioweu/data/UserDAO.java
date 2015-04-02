package org.ioweu.data;

import org.ioweu.impl.PrivateUser;
import org.ioweu.impl.RegisteredUser;
import org.ioweu.impl.User;
import org.ioweu.utils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static Logger log = LoggerFactory.getLogger(UserDAO.class);
    public void insertUser (User user){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        String queryString1 = "INSERT INTO USER(uname, name, isregistered) VALUES ( ?,?, ?)";
        String queryString2 = "INSERT INTO REGISTEREDUSER(uname, email, pwdhash) VALUES (?,?,?)";
        String name = user.getFirstName() + " " + user.getLastName();
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement1 = connection.prepareStatement(queryString1);
            statement1.setString(1, user.getUserName());
            statement1.setString(2, name);
            statement1.setBoolean(3, user.isRegistered());
            statement1.executeUpdate();
            if (user.isRegistered()){
                statement2 = connection.prepareStatement(queryString2);
                statement2.setString(1, user.getUserName());
                statement2.setString(2, user.getEmail());
                statement2.setString(3, user.getPassword());
                statement2.executeUpdate();
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            try{
                if (statement1 != null){
                    statement1.close();
                }
                if (statement2 != null){
                    statement2.close();
                }
                if  (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean isUserNameAvailable (String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement("SELECT * FROM REGISTEREDUSER WHERE uname=?");
            statement.setString(1, userName);

            ResultSet resultSet = statement.executeQuery();
            boolean found = false;
            if (resultSet.next()){
                found = true;
            }
            statement.close();
            connection.close();

            return found;

        }  catch (SQLException e) {
            e.printStackTrace();
        }   finally {
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

    public boolean isEmailAvailable (String emailAddress){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement("SELECT * FROM REGISTEREDUSER WHERE email=?");
            statement.setString(1, emailAddress);

            ResultSet resultSet = statement.executeQuery();
            boolean found = false;
            if (resultSet.next()){
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

    public String getPassword (String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        String password = null;

        String sql = "SELECT pwdhash FROM REGISTEREDUSER WHERE uname = ? " ;
        PreparedStatement preparedStatement = null;
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null){
                while (resultSet.next()) {
                    password = resultSet.getString(1);
                }
                resultSet.close();
            }
        } catch (SQLException e) {
            String error = "Error retrieving data..";
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
        return password;
    }

    public List<String> getPrivateUserList(String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query = "SELECT uname FROM USER WHERE uname LIKE ?  AND isregistered=0";
        List<String> userList = new ArrayList<String>();
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, userName + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String priUser = resultSet.getString(1);
                userList.add(priUser);
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
        return userList;
    }

    public User getRegisteredtUser(String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        User user = null;
        String query = "SELECT r.email, r.pwdhash, u.name, u.isregistered FROM REGISTEREDUSER r, USER u WHERE r.uname = u.uname AND r.uname = ? ";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String email = resultSet.getString(1);
                String pwd = resultSet.getString(2);
                String name = resultSet.getString(3);
                String[] split = name.split(" ");
                String isregistered = resultSet.getString(4);
                if (isregistered.equals("1")){
                    user = new RegisteredUser();
                    user.setRegistered(true);
                    user.setUserName(userName);
                    user.setEmail(email);
                    user.setPassword(pwd);
                    if (split[0] != null){
                        user.setFirstName(split[0]);
                    }
                    if (split[1] != null){
                        user.setLastName(split[1]);
                    }

                }else {
                    user = new PrivateUser();
                    user.setUserName(userName);
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
        return user;
    }

    public User getPrivatetUser(String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        User user = null;
        String query = "SELECT u.name FROM USER u WHERE u.uname = ? ";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                String[] split = name.split(" ");
                user = new PrivateUser();
                user.setUserName(userName);
                if (split.length > 0 && split[0] != null){
                    user.setFirstName(split[0]);
                }
                if (split.length >1 && split[1] != null){
                    user.setLastName(split[1]);
                }
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
        return user;
    }

    public boolean isRegisteredUser(String username){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query = "SELECT u.uname FROM REGISTEREDUSER u WHERE u.uname = ? ";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            boolean found = false;
            if (resultSet.next()){
                found = true;
            }
            statement.close();
            connection.close();
            return found;
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
        return false;
    }


}
