package fr.litarvan.thundermusic.core;

import org.json.JSONException;
import org.json.JSONObject;

public class SongToDownload
{
    private String id;
    private String title;
    private String artist;
    private String thumbnail;
    private String url;

    private int progress;

    public SongToDownload(String id, String title, String artist, String thumbnail, String url)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.thumbnail = thumbnail;
        this.url = url;

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

    public String getUrl()
    {
        return url;
    }

    public void setProgress(int progress)
    {
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
                                  json.getString("thumbnail"),
                                  json.getString("url"));
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("id", id);
        result.put("title", title);
        result.put("artist", artist);
        result.put("thumbnail", thumbnail);
        result.put("url", url);
        result.put("progress", progress);

        return result;
    }
}
