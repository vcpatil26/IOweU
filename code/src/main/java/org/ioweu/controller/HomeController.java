package org.ioweu.controller;

import org.ioweu.api.UserManager;
import org.ioweu.impl.RegisteredUser;
import org.ioweu.impl.User;
import org.ioweu.impl.UserManagerImpl;
import org.ioweu.service.UserService;
import org.ioweu.utils.IOweUConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
    @RequestMapping(value = "/signUpRequest", method = RequestMethod.GET)
    public ModelAndView signRequest() {
        return new ModelAndView("addUser");
    }

    @ResponseBody
    @RequestMapping(value = "/signUp.jsp", method = RequestMethod.POST)
    public boolean signUp(HttpServletRequest request) {
        try{
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String userName = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String registered = request.getParameter("registered");
            System.out.println("************** adding user *******************");
            UserManager um = new UserManagerImpl();
            User user = new RegisteredUser();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setUserName(userName);
            user.setPassword(password);
            user.setRegistered(Boolean.parseBoolean(registered));
            um.addUser(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
