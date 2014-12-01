package com.manayenko.provectusfb.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.manayenko.provectusfb.FBInfoApplication;
import com.manayenko.provectusfb.R;
import com.manayenko.provectusfb.events.EventCurrentInfoReceived;
import com.manayenko.provectusfb.events.EventFriendsListReceived;
import com.manayenko.provectusfb.manager.FacebookManager;
import com.manayenko.provectusfb.model.FacebookUserInfo;
import com.manayenko.provectusfb.model.Friend;
import com.manayenko.provectusfb.ui.activity.InfoActivity;
import com.manayenko.provectusfb.ui.adapter.FriendsListAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Artem on 29.11.2014.
 */
public class UserInfoFragment extends Fragment {

    private final int ANIMATION_DURATION = 500;
    private final float ANIMATION_ALPHA_TO = 1f;

    private FacebookManager facebookManager;

    private View friendsListRoot;
    private View headerRoot;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        friendsListRoot = getView().findViewById(R.id.friends_root);
        ViewHelper.setAlpha(friendsListRoot, 0f);

        headerRoot = getView().findViewById(R.id.header_root);
        ViewHelper.setAlpha(headerRoot, 0f);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FBInfoApplication application = (FBInfoApplication) getActivity().getApplication();
        facebookManager = application.getFacebookManager();

        if (facebookManager.hasStoredUserInfo()) {
            refreshUserInfo();
        } else
            facebookManager.requestCurrentUserInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        refreshFriendsList();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void refreshUserInfo() {
        FacebookUserInfo userInfo = facebookManager.getStoredUserInfo();
        if (userInfo != null) {
            fillUserInfo(userInfo);
        } else
            setLoadingFailedState();
    }

    private void refreshFriendsList() {
        facebookManager.requestFriendsList();
    }

    public void onEvent(EventCurrentInfoReceived eventCurrentInfoReceived) {
        if (eventCurrentInfoReceived.isSuccess()) {
            refreshUserInfo();
        } else {
            setLoadingFailedState();
            showToastMessage(eventCurrentInfoReceived.getFailMessage());
        }
    }

    public void onEvent(EventFriendsListReceived eventFriendsListReceived) {
        if (eventFriendsListReceived.isSuccess()) {
            initFriendsList(eventFriendsListReceived.getFriendsList());
        } else {
            String failMessage = eventFriendsListReceived.getFailMessage();
            showToastMessage(failMessage);
        }
        setProgressVisible(false);
    }

    private void showViewWithAnimation(View view) {
        ViewPropertyAnimator
                .animate(view)
                .alpha(ANIMATION_ALPHA_TO)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void initFriendsList(List<Friend> friends) {
        ListView listView = (ListView) getView().findViewById(R.id.friends_list);
        listView.setEmptyView(getView().findViewById(android.R.id.empty));

        boolean needToAnimateList = listView.getAdapter() == null;

        FriendsListAdapter adapter = new FriendsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(adapter);
        adapter.setFriendsList(friends);

        TextView friendsCount = (TextView) getView().findViewById(R.id.friends_count);
        friendsCount.setText(String.format(getString(R.string.friends_list_title), friends.size()));

        if(needToAnimateList)
            showViewWithAnimation(friendsListRoot);
    }


    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void fillUserInfo(FacebookUserInfo userInfo) {
        if (userInfo != null && userInfo.getProperties() != null) {
            Map<String, String> infoMap = userInfo.getProperties();

            ProfilePictureView profilePhoto = (ProfilePictureView) getView().findViewById(R.id.profile_photo);
            profilePhoto.setProfileId(infoMap.get("id"));

            TextView userName = (TextView) getView().findViewById(R.id.user_name);
            userName.setText(infoMap.get("name"));

            ViewGroup infoDetailsRoot = (ViewGroup) getView().findViewById(R.id.info_details_root);
            boolean needToAnimateHeader = infoDetailsRoot.getChildCount() == 0;
            infoDetailsRoot.removeAllViews();

            addNewProperty(infoDetailsRoot, getString(R.string.profile_info_email), infoMap.get("email"));
            addNewProperty(infoDetailsRoot, getString(R.string.profile_info_gender), infoMap.get("gender"));
            addNewProperty(infoDetailsRoot, getString(R.string.profile_info_open_profile), infoMap.get("link"));

            if(needToAnimateHeader)
                showViewWithAnimation(headerRoot);
        }
    }

    private void addNewProperty(ViewGroup root, String title, String value) {
        if (!TextUtils.isEmpty(value)) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View detailsRow = inflater.inflate(R.layout.row_user_info_detail, root, false);
            root.addView(detailsRow);

            TextView titleView = (TextView) detailsRow.findViewById(R.id.info_detail_title);
            titleView.setText(title);

            TextView valueView = (TextView) detailsRow.findViewById(R.id.info_detail_value);
            valueView.setText(Html.fromHtml(value));
        }
    }

    private void setLoadingFailedState() {
        getView().findViewById(R.id.info_loading_failed).setVisibility(View.VISIBLE);
    }

    private void setProgressVisible(boolean visible) {
        ((InfoActivity) getActivity()).setProgressVisible(visible);
    }

}
