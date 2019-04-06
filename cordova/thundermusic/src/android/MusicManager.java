package fr.litarvan.thundermusic.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
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
    private EventManager eventManager;

    private File songFolder;
    private File cacheFile;
    private List<Song> songs;

    private Runnable onUpdate;

    private MediaMetadataRetriever retriever;

    public MusicManager(Context context, EventManager eventManager, Runnable onUpdate)
    {
        this.context = context;
        this.eventManager = eventManager;
        this.onUpdate = onUpdate;

        this.retriever = new MediaMetadataRetriever();
    }

    public void load() throws Exception
    {
        songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        songFolder.mkdirs();

        cacheFile = new File(context.getCacheDir(), "musics-v2.json");
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
                        Song song = Song.fromJSON(songs.getJSONObject(i));
                        if (song.getFile().exists()) {
                            this.songs.add(song);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TM-MusicManager", "Error while loading cache file", e);
                cacheFile.delete();
                this.songs = new ArrayList<>();
            }
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        File[] files = songFolder.listFiles();
        for (File file : files) {
            try {
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
					String title = file.getName().substring(0, file.getName().lastIndexOf('.'));
					String artist = "Artiste inconnu";

					try {
						AudioFile audio = AudioFileIO.read(file);

						Tag tags = audio.getTag();
						if (tags != null) {
							String t = tags.getFirst(FieldKey.TITLE);
							String a = tags.getFirst(FieldKey.ARTIST);

							if (t != null && !t.isEmpty()) {
							    title = t;
                            }

							if (a != null && !a.isEmpty()) {
							    artist = a;
                            }
						}
					} catch (Exception e) {
						Log.e("TM-MusicManager", "Error while reading song tags of (" + file.getName() + ")", e);
					}

                    // TODO: Convert mp3 tag reader to retriever way
                    create(new Song(
                        UUID.randomUUID().toString().substring(0, 10),
                        title,
                        artist,
                        getDuration(file),
                        null,
                        file
                    ), null, false);
                } else if (original.getThumbnail() != null && !original.getThumbnail().exists()) {
                    updateThumb(original);
                }
            } catch (Exception e) {
                Log.e("TM-MusicManager", "Error while loading one of the songs (" + file.getName() + ")", e);
            }
        }

        retriever.release();

        updateCache();
    }

    public synchronized String getDuration(File song)
    {
        retriever.setDataSource(song.getAbsolutePath());

        long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        long seconds = duration % 60;
        long minutes = (duration - seconds) / 60;

        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
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
            getDuration(file),
            null,
            file
        );

        return create(song, thumbnail, updateCache);
    }

    public Song create(Song song, byte[] thumbnail, boolean updateCache) throws Exception
    {
        System.out.println("mmmh la creation de chanson : " + song.getTitle());

        if (updateCache) {
            writeTags(song, thumbnail);
        }

        try {
            updateThumb(song);
        } catch (Exception e) {
            Log.e("TM-MusicManager", "Error while loading one of the songs thumbnails (" + song.getFile().getName() + ")", e);
        }

        songs.add(song);

        if (updateCache) {
            System.out.println("ON UPDATE CACHE");
            updateCache();
        }

        return song;
    }

    public void update(Song song) throws Exception
    {
        for (int i = 0; i < songs.size(); i++)
        {
            if (songs.get(i).getId().equals(song.getId()))
            {
                songs.set(i, song);
                break;
            }
        }

        writeTags(song, null);
        updateCache();
    }

    public void remove(Song song) throws Exception
    {
        for (int i = 0; i < songs.size(); i++)
        {
            if (songs.get(i).getId().equals(song.getId()))
            {
                songs.get(i).getFile().delete();
                songs.remove(i);

                break;
            }
        }

        updateCache();
    }

    protected void updateThumb(Song song) throws Exception
    {
        AudioFile audio = AudioFileIO.read(song.getFile());
        Tag tags = audio.getTag();

        if (tags != null) {
            Artwork artwork = tags.getFirstArtwork();

            if (artwork != null) {
                byte[] image = artwork.getBinaryData();
                String mime = tags.getFirstArtwork().getMimeType();
                File thumb = File.createTempFile("thumb-" + song.getId() + "-", "." + mime.substring(mime.lastIndexOf("/") + 1), context.getCacheDir());

                try (FileOutputStream out = new FileOutputStream(thumb)) {
                    out.write(image);
                }

                song.setThumbnail(thumb);
            }
        }
    }

    protected void writeTags(Song song, byte[] thumbnail) throws Exception
    {
        AudioFile audio = AudioFileIO.read(song.getFile());
        Tag tags = audio.getTag();

        tags.setField(FieldKey.TITLE, song.getTitle());
        tags.setField(FieldKey.ARTIST, song.getArtist());
        tags.setField(FieldKey.COMMENT, song.getId());

        if (thumbnail != null) {
            tags.deleteArtworkField();
            Artwork artwork = ArtworkFactory.getNew();
            artwork.setBinaryData(thumbnail);
            artwork.setMimeType("image/png");
            artwork.setPictureType(PictureTypes.DEFAULT_ID);
            artwork.setDescription("");

            tags.setField(artwork);
        }

        audio.setTag(tags);
        audio.commit();
    }

    protected void updateCache() throws Exception
    {
        final Collator collator = Collator.getInstance();
        collator.setStrength(Collator.NO_DECOMPOSITION);

        Collections.sort(songs, new Comparator<Song>()
        {
            @Override
            public int compare(Song song, Song t1)
            {
                return collator.compare(song.getTitle().trim(), t1.getTitle().trim());
            }
        });

        JSONArray songs = new JSONArray();
        cacheFile.delete();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)))) {
            for (Song song : this.songs) {
                songs.put(song.toJSON());
            }

            writer.write(songs.toString());
        } catch (Exception e) {
            Log.e("TM-MusicManager", "Error while writing cache", e);
        }

        JSONObject event = new JSONObject();
        JSONArray array = new JSONArray();

        for (Song song : this.songs) {
            array.put(song.toJSON());
        }

        event.put("type", "update");
        event.put("songs", array);

        eventManager.emit(event);

        onUpdate.run();
    }

    public List<Song> getSongs()
    {
        return songs;
    }
}
