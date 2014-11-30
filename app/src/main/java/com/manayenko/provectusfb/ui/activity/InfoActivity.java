package com.manayenko.provectusfb.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.manayenko.provectusfb.FBInfoApplication;
import com.manayenko.provectusfb.R;
import com.manayenko.provectusfb.manager.FacebookManager;

public class InfoActivity extends ActionBarActivity {

    private FacebookManager facebookManager;
    private UiLifecycleHelper lifecycleHelper;

    private View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        progress = findViewById(R.id.progress);

        FBInfoApplication application = (FBInfoApplication) getApplication();
        facebookManager = application.getFacebookManager();

        if (!facebookManager.isFbSessionValid()) {
            showAuthActivity();
            return;
        }

        lifecycleHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isClosed())
                    showAuthActivity();
            }
        });
        lifecycleHelper.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!facebookManager.isFbSessionValid())
            showAuthActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lifecycleHelper != null)
            lifecycleHelper.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lifecycleHelper != null)
            lifecycleHelper.onDestroy();
    }

    private void logout() {
        Session.getActiveSession().closeAndClearTokenInformation();
        facebookManager.clearCache();
        showAuthActivity();
    }

    private void showAuthActivity() {
        Intent startIntent = new Intent(this, AuthActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
    }

    public void setProgressVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        progress.setVisibility(visibility);
    }
}
