package com.manayenko.provectusfb.model;

import java.util.Map;

/**
 * Created by Artem on 01.12.2014.
 */
public class FacebookUserInfo{

    private Map<String, String> nameValuePairs;

    public Map<String, String> getProperties(){
        return nameValuePairs;
    }

}
