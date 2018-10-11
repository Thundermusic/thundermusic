package fr.litarvan.thundermusic.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.json.JSONArray;
import org.json.JSONObject;

public class MusicManager
{
    private Context context;
    private ResultReceiver resultReceiver;

    private File songFolder;
    private File cacheFile;
    private List<Song> songs;

    public MusicManager(Context context, ResultReceiver resultReceiver)
    {
        this.context = context;
        this.resultReceiver = resultReceiver;
    }

    public void load() throws Exception
    {
        songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        songFolder.mkdirs();

        cacheFile = new File(context.getCacheDir(), "musics.json");
        songs = new ArrayList<>();

        if (cacheFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)))) {
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                if (result.length() != 0) {
                    JSONArray songs = new JSONArray(result.toString());
                    for (int i = 0; i < songs.length(); i++) {
                        this.songs.add(Song.fromJSON(songs.getJSONObject(i)));
                    }
                }
            }
        }

        File[] files = songFolder.listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".mp3")) {
                continue;
            }

            Song original = null;
            for (Song song : songs) {
                if (song.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    original = song;
                    break;
                }
            }

            if (original == null) {
                //Mp3File audio = new Mp3File(file);
                //MP3File audio = new MP3File(file);
                AudioFile audio = AudioFileIO.read(file);
                String title = file.getName().substring(0, file.getName().lastIndexOf('.'));
                String artist = "Artiste inconnu";

                Tag tags = audio.getTag();
                //AbstractID3v2 tags = audio.getID3v2Tag();
                if (tags != null) {
                    /*title = tags.getTitle();
                    artist = tags.getArtist();*/
                    title = tags.getFirstField(FieldKey.TITLE).toString();
                    artist = tags.getFirstField(FieldKey.ARTIST).toString();
                }

                Song song = create(new SongToDownload(
                    UUID.randomUUID().toString().substring(0, 10),
                    title,
                    artist,
                    null
                ), file, null, false);

                updateThumb(song);
            } else if (original.getImage() != null && !original.getImage().exists()) {
                updateThumb(original);
            }
        }

        updateCache();
    }

    public File getFile(SongToDownload song)
    {
        String file = song.getArtist() + " - " + song.getTitle() + " (" + song.getId() + ").mp3";
        file = file.replaceAll("[/:|\\[\\]]", "s");

        return new File(songFolder, file);
    }

    public Song create(SongToDownload downloaded, File file, byte[] thumbnail, boolean updateCache) throws Exception
    {
        Song song = new Song(
            downloaded.getId(),
            downloaded.getTitle(),
            downloaded.getArtist(),
            null,
            file
        );

        songs.add(song);

        if (updateCache) {
            writeTags(song, thumbnail);
        }

        updateThumb(song);

        if (updateCache) {
            updateCache();
        }

        return song;
    }

    public void update(Song song) throws Exception
    {
        writeTags(song, null);
        updateCache();
    }

    public void remove(Song song) throws Exception
    {
        for (int i = 0; i < songs.size(); i++)
        {
            if (songs.get(i).getId().equals(song.getId()))
            {
                songs.remove(i);
            }
        }

        updateCache();
    }

    protected void updateThumb(Song song) throws Exception
    {
        //Mp3File file = new Mp3File(song.getFile());
        //MP3File file = new MP3File(song.getFile());
        //AbstractID3v2 tags = file.getID3v2Tag();
        AudioFile audio = AudioFileIO.read(song.getFile());
        Tag tags = audio.getTag();

        if (tags != null) {
            //byte[] image = tags.getFrame();
            byte[] image = tags.getFirstArtwork().getBinaryData();

            if (image != null) {
                //String mime = tags.getAlbumImageMimeType();
                String mime = tags.getFirstArtwork().getMimeType();
                File thumb = File.createTempFile("thumb-" + song.getId() + "-", "." + mime.substring(mime.lastIndexOf("/") + 1), context.getCacheDir());

                try (FileOutputStream out = new FileOutputStream(thumb)) {
                    out.write(image);
                }

                song.setImage(thumb);
            }
        }
    }

    protected void writeTags(Song song, byte[] thumbnail) throws Exception
    {
        File oldFile = new File(song.getFile().getAbsolutePath() + ".old");
        File newFile = new File(song.getFile().getAbsolutePath());

        song.getFile().renameTo(oldFile);

        /*Mp3File file = new Mp3File(oldFile);
        ID3v2 tags = file.hasId3v2Tag() ? file.getId3v2Tag() : new ID3v24Tag();*/
        AudioFile audio = AudioFileIO.read(oldFile);
        Tag tags = audio.getTag();

        /*tags.setComment(song.getId());
        tags.setTitle(song.getTitle());
        tags.setArtist(song.getArtist());*/
        tags.setField(FieldKey.TITLE, song.getTitle());
        tags.setField(FieldKey.ARTIST, song.getArtist());
        tags.setField(FieldKey.COMMENT, song.getId());

        if (thumbnail != null) {
            //tags.setAlbumImage(thumbnail, "image/jpeg");
            tags.deleteArtworkField();
            Artwork artwork = ArtworkFactory.getNew();
            artwork.setBinaryData(thumbnail);
            artwork.setMimeType("image/png");
            artwork.setPictureType(PictureTypes.DEFAULT_ID);
            artwork.setDescription("");

            tags.setField(artwork);
        }

        /*file.setId3v2Tag(tags);
        file.save(newFile.getAbsolutePath());*/
        audio.setTag(tags);
        audio.setFile(newFile);
        audio.commit();

        oldFile.delete();
    }

    protected void updateCache() throws Exception
    {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getTitle().trim().compareToIgnoreCase(t1.getTitle().trim());
            }
        });

        JSONArray songs = new JSONArray();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)))) {
            for (Song song : this.songs) {
                songs.put(song.toJSON());
            }

            writer.write(songs.toString());
        }

        JSONObject event = new JSONObject();
        JSONArray array = new JSONArray();

        for (Song song : this.songs) {
            array.put(song.toJSON());
        }

        event.put("type", "update");
        event.put("songs", array);

        Bundle bundle = new Bundle();
        bundle.putString("event", event.toString());

        resultReceiver.send(ThundermusicService.RSP_EVENT, bundle);
    }

    public List<Song> getSongs()
    {
        return songs;
    }
}
