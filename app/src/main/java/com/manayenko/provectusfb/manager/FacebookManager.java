package com.manayenko.provectusfb.manager;

import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.manayenko.provectusfb.events.EventCurrentInfoReceived;
import com.manayenko.provectusfb.events.EventFriendsListReceived;
import com.manayenko.provectusfb.model.Friend;
import com.manayenko.provectusfb.model.FriendsResponseData;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Artem on 29.11.2014.
 */
public class FacebookManager {

    private static final List<String> PERMISSIONS = new ArrayList<String>() {
        {
            add("email");
            add("public_profile");
            add("user_friends");
        }
    };

    private final String USER_PROFILE_FIELDS = "id, name, gender, email, link";
    private final String ME_FRIENDS_REQUEST = "/me/taggable_friends";

    private final int CACHE_LIFETIME = 20 * 1000;//20 seconds, can be changed

    private List<Friend> friendsCache;
    private long lastCacheRefreshTime;

    public FacebookManager() {
        friendsCache = new ArrayList<Friend>();
    }

    public List<String> getPermissionsList() {
        return PERMISSIONS;
    }

    public boolean isFbSessionValid() {
        if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
            return false;
        }
        return true;
    }

    private boolean isCacheActual() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastCacheRefreshTime;
        return timeDiff < CACHE_LIFETIME;
    }

    public void requestCurrentUserInfo() {
        Request request = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser me, Response response) {
                EventCurrentInfoReceived event;
                if (response.getError() == null)
                    event = new EventCurrentInfoReceived(me);
                else
                    event = new EventCurrentInfoReceived(response.getError().getErrorUserMessage());
                EventBus.getDefault().post(event);
            }
        });
        Bundle params = new Bundle();
        params.putString("fields", USER_PROFILE_FIELDS);
        request.setParameters(params);
        request.executeAsync();
    }

    private void sendCachedFriends() {
        List<Friend> friendsList = new ArrayList<Friend>(friendsCache);//new list is required to avoid concurrent modification
        EventFriendsListReceived event = new EventFriendsListReceived(friendsList);
        EventBus.getDefault().post(event);
    }

    private void refreshFriendsCache(List<Friend> newFriendsList) {
        friendsCache.clear();
        friendsCache.addAll(newFriendsList);
        lastCacheRefreshTime = System.currentTimeMillis();
    }

    public void requestFriendsList() {
        if (!friendsCache.isEmpty() && isCacheActual()) {
            sendCachedFriends();
            return;
        }

        Session session = Session.getActiveSession();
        Request request = new Request(session, ME_FRIENDS_REQUEST, null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        EventFriendsListReceived event = null;
                        if (response.getError() == null) {
                            Gson gson = new Gson();
                            String rawResponse = response.getRawResponse();
                            FriendsResponseData data = gson.fromJson(rawResponse, FriendsResponseData.class);
                            if (data != null) {
                                event = new EventFriendsListReceived(data.getData());
                                refreshFriendsCache(data.getData());
                            }
                        } else {
                            event = new EventFriendsListReceived(response.getError().getErrorUserMessage());
                        }
                        if (event != null)
                            EventBus.getDefault().post(event);
                    }
                }
        );
        request.executeAsync();
    }

    public void clearCache() {
        friendsCache.clear();
    }

}
