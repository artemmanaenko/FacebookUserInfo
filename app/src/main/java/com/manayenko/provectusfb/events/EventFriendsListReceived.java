package com.manayenko.provectusfb.events;

import com.manayenko.provectusfb.model.Friend;

import java.util.List;

/**
 * Created by Artem on 29.11.2014.
 */
public class EventFriendsListReceived extends BaseBusEvent {

    private List<Friend> friendsList;

    public EventFriendsListReceived(String failMessage) {
        super(failMessage);
    }

    public EventFriendsListReceived(List<Friend> friendsList) {
        super(EventStatus.SUCCESS);
        this.friendsList = friendsList;
    }

    public List<Friend> getFriendsList() {
        return friendsList;
    }

}
