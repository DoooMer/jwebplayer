package home.server.jwebplayer.service.playback;

import home.server.jwebplayer.entity.Track;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class UserPlayback
{
    private final UUID userId;

    private UUID playlistId;

    private LinkedList<String> queue = new LinkedList<>();

    private Integer queuePosition = 0;

    public UserPlayback(UUID userId)
    {
        this.userId = userId;
        playlistId = null;
    }

    /**
     * Загрузка плейлиста.
     */
    public UserPlayback setPlaylist(UUID playlistId, List<Track> tracks)
    {
        this.playlistId = playlistId;

        // забирать из базы?

        queue.clear();

        for (Track track : tracks) {
            queue.add(track.getId());
        }

        queuePosition = 0;

        return this;
    }

    public UUID getPlaylistId()
    {
        return playlistId;
    }

    /**
     * Получение ID текущего трека из плейлиста.
     */
    public String current()
    {
        return queue.size() > 0 ? queue.get(queuePosition) : null;
    }

    /**
     * Переключение на следующий трек в списке.
     */
    public void next()
    {
        int size = queue.size();

        if (size < 1) {
            queuePosition = 0;
            return;
        }

        queuePosition++;

        if (queuePosition >= size) {
            queuePosition = 0;
        }
    }

    /**
     * Переключение активного трека.
     */
    public void select(String id) throws Exception
    {
        int index = queue.indexOf(id);

        if (index < 0) {
            throw new Exception("Track " + id + " not found.");
        }

        queuePosition = index;
    }
}
