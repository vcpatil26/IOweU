package org.ioweu.api;

import org.ioweu.impl.Group;

import java.util.List;

public interface GroupManager {
    public void addGroup (Group group) throws Exception;
    public boolean isGroupNameValid(String groupName) throws Exception;
    public List<Group> getGroups (String user) throws Exception;
    public List<String> getUsersPerGroup (String groupName) throws Exception;
}
