package org.ioweu.impl;

import org.ioweu.utils.TransactionConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public abstract class Transaction {
    private int transactionID;
    private String description;
    private double amount;
    private Group group;
    private List<User> sharedUsers;
    private Map<String, Double> sharedPercentage;
    private Map<String, Double> sharedAmount;
    private Map<String, SharedStatus> sharedStatus;
    private Date date;
    private Map<String, Double> categories;
    private String transactionType = TransactionConstants.NORMAL_TR;
    private String transactionOwner;
    private String status = TransactionConstants.PENDING_STATUS;
    private String statusMsg;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<User> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(List<User> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    public Map<String, Double> getSharedPercentage() {
        return sharedPercentage;
    }

    public void setSharedPercentage(Map<String, Double> sharedPercentage) {
        this.sharedPercentage = sharedPercentage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, Double> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Double> categories) {
        this.categories = categories;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionOwner() {
        return transactionOwner;
    }

    public void setTransactionOwner(String transactionOwner) {
        this.transactionOwner = transactionOwner;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public Map<String, Double> getSharedAmount() {
        return sharedAmount;
    }

    public void setSharedAmount(Map<String, Double> sharedAmount) {
        this.sharedAmount = sharedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public Map<String, SharedStatus> getSharedStatus() {
        return sharedStatus;
    }

    public void setSharedStatus(Map<String, SharedStatus> sharedStatus) {
        this.sharedStatus = sharedStatus;
    }
}
