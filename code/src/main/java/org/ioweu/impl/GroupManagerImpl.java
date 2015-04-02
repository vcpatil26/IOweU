package org.ioweu.impl;

import org.ioweu.api.GroupManager;
import org.ioweu.data.GroupDAO;

import java.util.List;

public class GroupManagerImpl implements GroupManager {
    private GroupDAO groupDAO;

    public GroupManagerImpl() {
        groupDAO = new GroupDAO();
    }

    public void addGroup(Group group) throws Exception{
        if (!groupDAO.isGroupNameExists(group.getGroupName())){
            groupDAO.addGroup(group);
        } else {
            throw new Exception("Group already available");
        }

    }

    public boolean isGroupNameValid(String groupName) throws Exception{
        return groupDAO.isGroupNameExists(groupName);
    }

    @Override
    public List<Group> getGroups(String user) throws Exception {
        return groupDAO.getGroups(user);
    }

    @Override
    public List<String> getUsersPerGroup(String groupName) throws Exception {
        return groupDAO.getUsersPerGroup(groupName);
    }
}
