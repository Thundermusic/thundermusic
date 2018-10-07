package fr.litarvan.thundermusic.core;

import org.json.JSONException;
import org.json.JSONObject;

public class SongToDownload
{
    private String id;
    private String title;
    private String artist;
    private String thumbnail;

    public SongToDownload(String id, String title, String artist, String thumbnail)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.thumbnail = thumbnail;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public static SongToDownload fromJSON(JSONObject json) throws JSONException
    {
        return new SongToDownload(json.getString("id"),
                                  json.getString("title"),
                                  json.getString("artist"),
                                  json.getString("thumbnail"));
    }
}
