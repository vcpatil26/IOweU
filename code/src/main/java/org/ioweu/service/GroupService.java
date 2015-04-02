package org.ioweu.service;

import org.ioweu.api.GroupManager;
import org.ioweu.impl.Group;
import org.ioweu.impl.GroupManagerImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value="moduleService")
public class GroupService {
    public List<Group> getGroups(String userName){
        List<Group> groups = new ArrayList<Group>();
        try {
            GroupManager gm = new GroupManagerImpl();
            groups = gm.getGroups(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }
}
