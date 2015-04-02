package org.ioweu.impl;

public class DebtUser extends User {
    private String owetouser;
    private double amount;

    public String getOwetouser() {
        return owetouser;
    }

    public void setOwetouser(String owetouser) {
        this.owetouser = owetouser;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
