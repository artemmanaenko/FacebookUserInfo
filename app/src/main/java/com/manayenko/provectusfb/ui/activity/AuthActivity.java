package com.manayenko.provectusfb.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.manayenko.provectusfb.FBInfoApplication;
import com.manayenko.provectusfb.R;
import com.manayenko.provectusfb.manager.FacebookManager;


public class AuthActivity extends ActionBarActivity {

    private final int REQUEST_CODE_LOGIN_VIA_FACEBOOK = 64206;

    private FacebookManager facebookManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        FBInfoApplication application = (FBInfoApplication) getApplication();
        facebookManager = application.getFacebookManager();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setApplicationId(getString(R.string.facebook_app_id));
        loginButton.setPublishPermissions(facebookManager.getPermissionsList());

        if (facebookManager.isFbSessionValid())
            showInfoActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_LOGIN_VIA_FACEBOOK:
                Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK)
                    Session.openActiveSession(this, true, facebookManager.getPermissionsList(), sessionStateCallback);
                break;
        }
    }

    private Session.StatusCallback sessionStateCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                showInfoActivity();
            }
        }
    };

    private void showInfoActivity() {
        Intent startIntent = new Intent(this, InfoActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
    }

}
