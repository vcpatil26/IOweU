package org.ioweu.impl;

public class RecentActivity {
    private String loggedinUser;
    private String username;
    private int tid;
    private String trDescription;
    private String trStatus;

    public String getLoggedinUser() {
        return loggedinUser;
    }

    public void setLoggedinUser(String loggedinUser) {
        this.loggedinUser = loggedinUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getTrDescription() {
        return trDescription;
    }

    public void setTrDescription(String trDescription) {
        this.trDescription = trDescription;
    }

    public String getTrStatus() {
        return trStatus;
    }

    public void setTrStatus(String trStatus) {
        this.trStatus = trStatus;
    }
}
