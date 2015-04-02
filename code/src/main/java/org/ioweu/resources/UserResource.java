package org.ioweu.resources;

import org.ioweu.api.UserManager;
import org.ioweu.impl.User;
import org.ioweu.impl.UserManagerImpl;
import org.ioweu.utils.IOweUConstants;
import org.ioweu.utils.IOweUUtils;
import org.ioweu.utils.ResourcePathConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ResourcePathConstants.USER_OPERATIONS)
public class UserResource {
    @Context
    ServletContext context;

    @POST
    @Path(ResourcePathConstants.ADD_USER)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addUser(User user){
        UserManager um  = (UserManagerImpl)context.getAttribute(IOweUConstants.USER_MANAGER);
        try {
            um.addUser(user);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("User added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return IOweUUtils.reportInternalServerError(ResourcePathConstants.ADD_USER, e);
        }
    }

    @GET
    @Path(ResourcePathConstants.IS_USERNAME_VALID)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isUserNameAvailable (@QueryParam("username") String userName){
        UserManager um  = (UserManagerImpl)context.getAttribute(IOweUConstants.USER_MANAGER);
        boolean state;
        try {
            state = um.isUserNameAvailable(userName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("False");
                return builder.build();
            }
        }  catch (Throwable e) {
            return IOweUUtils.reportInternalServerError(ResourcePathConstants.IS_USERNAME_VALID, e);
        }
    }

    @GET
    @Path(ResourcePathConstants.IS_EMAIL_VALID)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isEmailAddressAvailable (@QueryParam("email") String email){
        UserManager um  = (UserManagerImpl)context.getAttribute(IOweUConstants.USER_MANAGER);
        boolean state;
        try {
            state = um.isEmailAvailable(email);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("False");
                return builder.build();
            }
        }  catch (Throwable e) {
            return IOweUUtils.reportInternalServerError(ResourcePathConstants.IS_EMAIL_VALID, e);
        }
    }

}
