package fr.litarvan.thundermusic.core;

import org.json.JSONException;
import org.json.JSONObject;

public class SongToDownload
{
    private String id;
    private String title;
    private String artist;
    private String thumbnail;

    private int progress;

    public SongToDownload(String id, String title, String artist, String thumbnail)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.thumbnail = thumbnail;
        this.progress = -2;
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

    public void setProgress(int progress)
    {
        System.out.println(title + " : progress : " + progress);
        this.progress = progress;
    }

    public int getProgress()
    {
        return progress;
    }

    public static SongToDownload fromJSON(JSONObject json) throws JSONException
    {
        return new SongToDownload(json.getString("id"),
                                  json.getString("title"),
                                  json.getString("artist"),
                                  json.getString("thumbnail"));
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("id", id);
        result.put("title", title);
        result.put("artist", artist);
        result.put("thumbnail", thumbnail);
        result.put("progress", progress);

        return result;
    }
}
