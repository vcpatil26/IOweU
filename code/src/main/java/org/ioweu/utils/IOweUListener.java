package org.ioweu.utils;

import org.ioweu.api.GroupManager;
import org.ioweu.api.UserManager;
import org.ioweu.impl.GroupManagerImpl;
import org.ioweu.impl.UserManagerImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class IOweUListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        UserManager um = new UserManagerImpl();
        GroupManager gm = new GroupManagerImpl();

        servletContext.setAttribute(IOweUConstants.USER_MANAGER, um);
        servletContext.setAttribute(IOweUConstants.GROUP_MANAGER, gm);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
