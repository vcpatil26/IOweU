package org.ioweu.utils;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOweUUtils {
    protected static Logger log = LoggerFactory.getLogger(IOweUUtils.class);
    public static  String hashPwd (String pwd) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            return new String(messageDigest.digest(pwd.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding password string when creating digest", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encoding password string when creating digest", e);
        }
    }

    public static Response reportInternalServerError(String resourceMethod, Throwable t) {
        log.error("Resource Method : " + resourceMethod + " : Internal Server Error ", t);
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        builder.entity(t.getMessage());
        return builder.build();
    }
}
