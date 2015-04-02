package org.ioweu.resources;

import org.ioweu.api.GroupManager;
import org.ioweu.impl.Group;
import org.ioweu.impl.GroupManagerImpl;
import org.ioweu.utils.IOweUConstants;
import org.ioweu.utils.IOweUUtils;
import org.ioweu.utils.ResourcePathConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ResourcePathConstants.GROUP_OPERATIONS)
public class GroupResource {
    @Context
    ServletContext context;

    @POST
    @Path(ResourcePathConstants.ADD_GROUP)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addUser(Group group){
        GroupManager gm  = (GroupManagerImpl)context.getAttribute(IOweUConstants.GROUP_MANAGER);
        try {
            gm.addGroup(group);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Group added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return IOweUUtils.reportInternalServerError(ResourcePathConstants.ADD_GROUP, e);
        }
    }

    @GET
    @Path(ResourcePathConstants.IS_GROUPNAME_VALID)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response isGroupNameAvailable (@QueryParam("groupName") String groupName){
        GroupManager gm  = (GroupManagerImpl)context.getAttribute(IOweUConstants.GROUP_MANAGER);
        boolean state;
        try {
            state = gm.isGroupNameValid(groupName);
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
            return IOweUUtils.reportInternalServerError(ResourcePathConstants.IS_GROUPNAME_VALID, e);
        }
    }
}
