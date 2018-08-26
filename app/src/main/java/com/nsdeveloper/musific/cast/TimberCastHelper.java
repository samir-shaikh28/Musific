/*
 * Copyright (C) 2017 Samir Shaikh
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.nsdeveloper.musific.cast;

import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.nsdeveloper.musific.models.Song;
import com.nsdeveloper.musific.utils.Constants;
import com.nsdeveloper.musific.utils.TimberUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class TimberCastHelper  {

    public static void startCasting(CastSession castSession, Song song) {

        String ipAddress = TimberUtils.getIPAddress(true);
        URL baseUrl;
        try {
            baseUrl = new URL("http", ipAddress, Constants.CAST_SERVER_PORT, "" );
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        String songUrl = baseUrl.toString() + "/song?id=" + song.id;
        String albumArtUrl = baseUrl.toString() + "/albumart?id=" + song.albumId;

        MediaMetadata musicMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        musicMetadata.putString(MediaMetadata.KEY_TITLE, song.title);
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, song.artistName);
        musicMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, song.albumName);
        musicMetadata.putInt(MediaMetadata.KEY_TRACK_NUMBER, song.trackNumber);
        musicMetadata.addImage(new WebImage(Uri.parse(albumArtUrl)));

        try {
            MediaInfo mediaInfo = new MediaInfo.Builder(songUrl)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("audio/mpeg")
                    .setMetadata(musicMetadata)
                    .setStreamDuration(song.duration)
                    .build();
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            remoteMediaClient.load(mediaInfo, true, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
