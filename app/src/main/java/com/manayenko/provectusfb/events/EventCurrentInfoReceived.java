package com.manayenko.provectusfb.events;

/**
 * Created by Artem on 29.11.2014.
 */
public class EventCurrentInfoReceived extends BaseBusEvent {

    public EventCurrentInfoReceived(String failMessage) {
        super(failMessage);
    }

    public EventCurrentInfoReceived() {
        super(EventStatus.SUCCESS);
    }

}
