package com.manayenko.provectusfb.events;

import com.facebook.model.GraphUser;

/**
 * Created by Artem on 29.11.2014.
 */
public class EventCurrentInfoReceived extends BaseBusEvent {

    private GraphUser userInfo;

    public EventCurrentInfoReceived(String failMessage) {
        super(failMessage);
    }

    public EventCurrentInfoReceived(GraphUser userInfo) {
        super(EventStatus.SUCCESS);
        this.userInfo = userInfo;
    }

    public GraphUser getUserInfo() {
        return userInfo;
    }

}
