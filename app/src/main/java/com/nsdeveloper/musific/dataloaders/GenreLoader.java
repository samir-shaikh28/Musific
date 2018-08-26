package com.nsdeveloper.musific.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;


import com.nsdeveloper.musific.models.Genre;
import com.nsdeveloper.musific.utils.PreferencesUtility;

import java.util.ArrayList;
import java.util.List;


public class GenreLoader {

    public static Genre getGenre(Cursor cursor) {
        Genre genre = new Genre();
        if (cursor != null) {
            if (cursor.moveToFirst())
                genre = new Genre(cursor.getLong(0), cursor.getString(1));
        }
        if (cursor != null)
            cursor.close();
        return genre;
    }


    public static List<Genre> getGenresForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                arrayList.add(new Genre(cursor.getLong(0), cursor.getString(1)));
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }

    public static List<Genre> getAllGenre(Context context) {
        return getGenresForCursor(makeGenreCursor(context, null, null));
    }

    public static Genre getGenre(Context context, long id) {
        return getGenre(makeGenreCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static List<Genre> getGenre(Context context, String paramString, int limit) {
        List<Genre> result = getGenresForCursor(makeGenreCursor(context, "genre LIKE ?", new String[]{paramString + "%"}));
        if (result.size() < limit) {
            result.addAll(getGenresForCursor(makeGenreCursor(context, "genre LIKE ?", new String[]{"%_" + paramString + "%"})));
        }
        return result.size() < limit ? result : result.subList(0, limit);
    }


    public static Cursor makeGenreCursor(Context context, String selection, String[] paramArrayOfString) {
        final String genreSortOrder = PreferencesUtility.getInstance(context).getGenreSortOrder();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[]{"_id", MediaStore.Audio.GenresColumns.NAME}, selection, paramArrayOfString, genreSortOrder);

        return cursor;
    }
}
