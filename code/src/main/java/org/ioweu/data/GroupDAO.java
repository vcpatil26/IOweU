package org.ioweu.data;

import org.ioweu.impl.Group;
import org.ioweu.utils.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
    public void addGroup (Group group){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        String queryString1 = "INSERT INTO USERGROUP(gname) VALUES (?)";
        String queryString2 = "INSERT INTO GROUPCONSISTUSER(gname, uname) VALUES (?,?)";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement1 = connection.prepareStatement(queryString1);
            statement1.setString(1, group.getGroupName());
            statement1.executeUpdate();
            List<String> membersList = group.getMembersList();
            for (String user : membersList){
                statement2 = connection.prepareStatement(queryString2);
                statement2.setString(1, group.getGroupName());
                statement2.setString(2, user);
                statement2.executeUpdate();
            }

        } catch (SQLException e) {
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

    public boolean isGroupNameExists(String groupName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query = "SELECT gname FROM USERGROUP WHERE gname=?";
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            boolean found = false;
            if (resultSet.next()) {
                found = true;
            }
            statement.close();
            connection.close();
            return found;
        } catch (SQLException e) {
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

    public List<Group> getGroups (String userName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT gname FROM GROUPCONSISTUSER WHERE uname=?";
        List<Group> groupList = new ArrayList<Group>();
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String groupName = resultSet.getString(1);
                List<String> usersPerGroup = getUsersPerGroup(groupName);
                Group group = new Group();
                group.setGroupName(groupName);
                group.setMembersList(usersPerGroup);
                groupList.add(group);
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
        return groupList;
    }

    public List<String> getUsersPerGroup (String groupName){
        String connectionURL =  DBUtils.getJDBCUrl();
        Connection connection = null;
        PreparedStatement statement = null;
        String query1 = "SELECT uname FROM GROUPCONSISTUSER WHERE gname=?";
        List<String> userList = new ArrayList<String>();
        try {
            connection = DriverManager.getConnection(connectionURL, DBUtils.getJDBCUser(), DBUtils.getJDBCPWD());
            statement = connection.prepareStatement(query1);
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String userName = resultSet.getString(1);
                userList.add(userName);
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
}
