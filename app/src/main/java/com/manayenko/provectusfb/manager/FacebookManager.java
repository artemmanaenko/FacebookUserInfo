package com.manayenko.provectusfb.manager;

import android.content.Context;
import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.manayenko.provectusfb.events.EventCurrentInfoReceived;
import com.manayenko.provectusfb.events.EventFriendsListReceived;
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

    private Context context;

    public FacebookManager(Context context) {
        this.context = context;
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

    public void requestFriendsList() {
        Session session = Session.getActiveSession();
        Request request = new Request(session, "/me/taggable_friends", null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        EventFriendsListReceived event;
                        if (response.getError() == null) {
                            Gson gson = new Gson();
                            String rawResponse = response.getRawResponse();
                            FriendsResponseData data = gson.fromJson(rawResponse, FriendsResponseData.class);
                            event = new EventFriendsListReceived(data.getData());
                        } else {
                            event = new EventFriendsListReceived(response.getError().getErrorUserMessage());
                        }
                        EventBus.getDefault().post(event);
                    }
                }
        );
        request.executeAsync();
    }

}
