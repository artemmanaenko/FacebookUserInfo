package com.manayenko.provectusfb.model;

/**
 * Created by Artem on 29.11.2014.
 */
public class Friend {
    private String id;
    private String name;
    private Picture picture;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        if (picture.data != null && picture.data.url != null)
            return picture.data.url;
        else
            return null;
    }

    public class Picture {
        PictureData data;
    }

    private class PictureData {
        private String url;
    }
}
