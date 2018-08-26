package com.nsdeveloper.musific.dataloaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.nsdeveloper.musific.models.Song;
import com.nsdeveloper.musific.utils.PreferencesUtility;

import java.util.ArrayList;

/**
 * Created by nsdeveloper on 6/18/2017.
 */

public class GenreSongLoader {



    private static final long[] sEmptyList = new long[0];

    public static ArrayList<Song> getSongsForGenre(Context context, long genreID) {

        Cursor cursor = makeGenreSongCursor(context, genreID);
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String genre = cursor.getString(3);
                int duration = cursor.getInt(4);
                int trackNumber = cursor.getInt(5);
                /*This fixes bug where some track numbers displayed as 100 or 200*/
                while (trackNumber >= 1000) {
                    trackNumber -= 1000; //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
                }
                long artistId = cursor.getInt(6);
                long gernreId = genreID;

                arrayList.add(new Song(id, gernreId, artistId, title, artist, genre, duration, trackNumber));
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }





    public static Cursor makeGenreSongCursor(Context context, long genreID) {
        ContentResolver contentResolver = context.getContentResolver();
        final String genreSongSortOrder = PreferencesUtility.getInstance(context).getGenreSongSortOrder();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String string = "is_music=1 AND title != '' AND album_id=" + genreID;
        Cursor cursor = contentResolver.query(uri,
                new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id"},
                string, null,genreSongSortOrder);
        return cursor;
    }


}
