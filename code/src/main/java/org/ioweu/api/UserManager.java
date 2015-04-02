package org.ioweu.api;

import org.ioweu.impl.User;

import java.util.List;

public interface UserManager {
    public void addUser(User user) throws Exception;
//    public void addPrivateUser (PrivateUser pUser) throws Exception;
    public boolean isUserNameAvailable(String userName) throws Exception;
    public boolean isEmailAvailable(String emailAddress) throws Exception;
    public String getPassword(String username) throws Exception;
    public List<String> getPrivateUserList(String userName);
    public User getRegisteredUser(String userName);
    public User getPrivateUser(String userName);
    public boolean isRegisteredUser(String userName);

}
