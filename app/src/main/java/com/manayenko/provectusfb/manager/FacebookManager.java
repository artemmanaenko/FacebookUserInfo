package com.manayenko.provectusfb.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.manayenko.provectusfb.R;
import com.manayenko.provectusfb.events.EventCurrentInfoReceived;
import com.manayenko.provectusfb.events.EventFriendsListReceived;
import com.manayenko.provectusfb.model.FacebookUserInfo;
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

    private final String PREFS_USER_INFO = "prefs_user_info";

    private final int CACHE_LIFETIME = 60 * 1000;//1 minute, can be changed

    private Context context;
    private SharedPreferences prefs;

    private Gson gson;

    private List<Friend> friendsCache;
    private long lastCacheRefreshTime;

    public FacebookManager(Context context) {
        this.context = context;
        friendsCache = new ArrayList<Friend>();
        gson = new Gson();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
                if (response.getError() == null) {
                    storeUserInfo(me);
                    event = new EventCurrentInfoReceived();
                } else
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
        if (isFriendsCacheActual()) {
            sendCachedFriends();
            return;
        }

        Session session = Session.getActiveSession();
        Request request = new Request(session, ME_FRIENDS_REQUEST, null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        EventFriendsListReceived event = null;
                        if (response.getError() == null) {
                            String rawResponse = response.getRawResponse();
                            FriendsResponseData data = gson.fromJson(rawResponse, FriendsResponseData.class);
                            if (data != null) {
                                event = new EventFriendsListReceived(data.getData());
                                refreshFriendsCache(data.getData());
                            }
                        } else {
                            String failMessage = response.getError().getErrorUserMessage();
                            if (failMessage == null)
                                failMessage = context.getString(R.string.friends_list_get_error);
                            event = new EventFriendsListReceived(failMessage);
                        }
                        if (event != null)
                            EventBus.getDefault().post(event);
                    }
                }
        );
        request.executeAsync();
    }

    private boolean isFriendsCacheActual() {
        if(friendsCache.isEmpty())
            return false;
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastCacheRefreshTime;
        return timeDiff < CACHE_LIFETIME;
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

    private void clearFriendsCache() {
        friendsCache.clear();
    }

    private void storeUserInfo(GraphUser userInfo) {
        String userInfoStr = gson.toJson(userInfo.getInnerJSONObject());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_USER_INFO, userInfoStr);
        editor.commit();
    }

    public boolean hasStoredUserInfo() {
        return prefs.contains(PREFS_USER_INFO) && getStoredUserInfo() != null;
    }

    public FacebookUserInfo getStoredUserInfo() {
        String storedInfo = prefs.getString(PREFS_USER_INFO, "");
        FacebookUserInfo userInfo = gson.fromJson(storedInfo, FacebookUserInfo.class);
        return userInfo;
    }

    public void logout(){
        clearFriendsCache();
        clearCachedUserInfo();
    }

    private void clearCachedUserInfo(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().commit();
    }

}
