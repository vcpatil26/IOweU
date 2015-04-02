package org.ioweu.utils;

import java.net.URL;
import java.util.Properties;

public class DBUtils {
    private static Properties properties = new Properties();

    public static String getJDBCUrl(){
        loadProperties();
        return properties.getProperty(IOweUConstants.JDBC_URL);
    }

    public static String getJDBCUser () {
        loadProperties();
        return properties.getProperty(IOweUConstants.JDBC_USER);
    }

    public static String getJDBCDriver() {
        loadProperties();
        return properties.getProperty(IOweUConstants.JDBC_DRIVER);
    }

    public static String getJDBCPWD (){
        loadProperties();
        return properties.getProperty(IOweUConstants.JDBC_PWD);
    }

    private static void loadProperties() {
        URL url = getPropertyFileURL();
        try {
            properties.load(url.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static URL getPropertyFileURL() {
        return DBUtils.class.getClassLoader().getResource(IOweUConstants.IOWU_PROPERTIES);
    }
}
