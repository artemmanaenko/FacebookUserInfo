package com.manayenko.provectusfb.events;

/**
 * Created by Artem on 29.11.2014.
 */
public class BaseBusEvent {

    private String failMessage;
    private EventStatus status;

    public BaseBusEvent(String failMessage) {
        this.failMessage = failMessage;
        status = EventStatus.FAILED;
    }

    public BaseBusEvent(EventStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status != null && status.equals(EventStatus.SUCCESS);
    }

    public String getFailMessage() {
        return failMessage;
    }
}
