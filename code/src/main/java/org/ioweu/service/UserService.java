package org.ioweu.service;

import org.ioweu.api.UserManager;
import org.ioweu.impl.UserManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Service(value="userService")
public class UserService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        String password = null;
        System.out.println("user service");
        try {
            UserManager um = new UserManagerImpl();
            boolean nameAvailable = um.isUserNameAvailable(userName);
            if (nameAvailable){
                password = um.getPassword(userName);
            }
            System.out.println("user service " + userName);
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
            List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
            authorityList.add(authority);
            return new User(userName, password, authorityList);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
