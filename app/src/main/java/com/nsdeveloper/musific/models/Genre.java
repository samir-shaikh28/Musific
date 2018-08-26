package com.nsdeveloper.musific.models;

/**
 * Created by nsdeveloper on 6/18/2017.
 */

public class Genre {


    public final long id;
    public final String title;

    public Genre() {
        this.id = -1;
        this.title = "";
    }

    public Genre(long _id, String _title) {
        this.id = _id;
        this.title = _title;
    }


}



