package com.manayenko.provectusfb;

import android.app.Application;

import com.manayenko.provectusfb.manager.FacebookManager;

/**
 * Created by Artem on 29.11.2014.
 */
public class FBInfoApplication extends Application{

    private FacebookManager facebookManager;

    @Override
    public void onCreate() {
        super.onCreate();
        facebookManager = new FacebookManager();
    }

    public FacebookManager getFacebookManager() {
        return facebookManager;
    }
}
