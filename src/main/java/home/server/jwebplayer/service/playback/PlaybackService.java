package home.server.jwebplayer.service.playback;

import home.server.jwebplayer.entity.Playlist;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.entity.Track;
import home.server.jwebplayer.repository.PlaylistTrackRepository;
import home.server.jwebplayer.service.playlist.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Управляет воспроизведением.
 * Хранит пользовательские состояния воспроизведения.
 */
@Service
public class PlaybackService
{
    private final PlaylistService playlistService;

    private final PlaylistTrackRepository playlistTrackRepository;

    private final HashMap<UUID, UserPlayback> usersPlayback = new HashMap<>();

    public PlaybackService(PlaylistService playlistService, PlaylistTrackRepository playlistTrackRepository)
    {
        this.playlistService = playlistService;
        this.playlistTrackRepository = playlistTrackRepository;
    }

    /**
     * Получение состояние воспроизведения для пользователя.
     */
    public UserPlayback getForUser(UUID userId)
    {
        UserPlayback userPlayback = usersPlayback.get(userId);

        if (userPlayback == null) {
            userPlayback = new UserPlayback(userId);

            // плейлист по-умолчанию
            Playlist playlist = playlistService.getDefault();
            loadPlaylist(userPlayback, playlist.getId());

            usersPlayback.put(userId, userPlayback);
        }

        return userPlayback;
    }

    /**
     * Переключение плейлиста в состоянии воспроизведения пользователя.
     */
    public UserPlayback selectPlaylistForUser(UUID userId, UUID playlistId)
    {
        return loadPlaylist(getForUser(userId), playlistId);
    }

    /**
     * Загрузка плейлиста.
     */
    @Transactional
    UserPlayback loadPlaylist(UserPlayback userPlayback, UUID playlistId)
    {
        List<Track> playlistTracks = playlistTrackRepository.findAllByPlaylistId(playlistId)
                .stream()
                .map(PlaylistTrack::getTrack)
                .toList();

        userPlayback.setPlaylist(playlistId, playlistTracks);

        return userPlayback;
    }
}
