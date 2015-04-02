package org.ioweu.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class RecurringTransaction extends Transaction {
    private String recurringType;
    private int recurringTime;

    public String getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(String recurringType) {
        this.recurringType = recurringType;
    }

    public int getRecurringTime() {
        return recurringTime;
    }

    public void setRecurringTime(int recurringTime) {
        this.recurringTime = recurringTime;
    }
}
