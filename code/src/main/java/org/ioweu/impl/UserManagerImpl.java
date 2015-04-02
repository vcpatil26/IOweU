package org.ioweu.impl;

import org.ioweu.api.UserManager;
import org.ioweu.data.UserDAO;

import java.util.List;

public class UserManagerImpl implements UserManager {
    private UserDAO userDAO;

    public UserManagerImpl(){
        userDAO = new UserDAO();
    }

    public void addUser(User user) throws Exception {
        if (userDAO == null){
            userDAO = new UserDAO();
        }
        if (user instanceof RegisteredUser){
            if (!userDAO.isUserNameAvailable(user.getUserName()) && !userDAO.isEmailAvailable(user.getEmail())){
                userDAO.insertUser(user);
            }else {
                throw new Exception("Error while adding the user");
            }
        }else if (user instanceof PrivateUser){
            if (!userDAO.isUserNameAvailable(user.getUserName())){
                userDAO.insertUser(user);
            }else {
                throw new Exception("Error while adding the user");
            }
        }

    }

    public boolean isRegistered(String username, String password) throws Exception{
        return false;
    }

    public boolean isUserNameAvailable(String userName) throws Exception {
        if (userDAO == null){
            userDAO = new UserDAO();
        }
        return userDAO.isUserNameAvailable(userName);
    }

    public boolean isEmailAvailable(String emailAddress) throws Exception{
        if (userDAO == null){
            userDAO = new UserDAO();
        }
        return userDAO.isEmailAvailable(emailAddress);
    }

    public String getPassword(String username) throws Exception {
        return userDAO.getPassword(username);
    }

    @Override
    public List<String> getPrivateUserList(String userName) {
        return userDAO.getPrivateUserList(userName);
    }

    @Override
    public User getRegisteredUser(String userName) {
        return userDAO.getRegisteredtUser(userName);
    }

    @Override
    public User getPrivateUser(String userName) {
        return userDAO.getPrivatetUser(userName);
    }

    @Override
    public boolean isRegisteredUser(String userName) {
        return userDAO.isRegisteredUser(userName);
    }


}
