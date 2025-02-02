package home.server.jwebplayer.service.watch;

import home.server.jwebplayer.entity.Track;
import home.server.jwebplayer.repository.TrackRepository;
import home.server.jwebplayer.service.playlist.PlaylistPlaybackService;
import home.server.jwebplayer.service.playlist.PlaylistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EventListener;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
// TODO: only add track to DB
public class TrackListener implements EventListener
{
    private final TrackRepository trackRepository;
    private final PlaylistService playlistService;
    private final PlaylistPlaybackService playlistPlaybackService;

    @Autowired
    public TrackListener(
            TrackRepository trackRepository,
            PlaylistService playlistService,
            PlaylistPlaybackService playlistPlaybackService
    )
    {
        this.trackRepository = trackRepository;
        this.playlistService = playlistService;
        this.playlistPlaybackService = playlistPlaybackService;
    }

    public void onCreated(String path)
    {
        Track track = findTrack(path).orElse(new Track(UUID.randomUUID().toString(), path));
        log.debug("Add track {}", track.getId());
        trackRepository.save(track);

        log.debug("Add track {} to default playlist", track.getId());
        playlistService.addTrackToDefaultPlaylist(track);

        log.info("Track {} has been added", track.getId());
        afterChange();
    }

    public void onDeleted(String path)
    {
        Optional<Track> track = findTrack(path);

        if (track.isPresent()) {
            log.debug("Found track {}", track.get().getId());

            log.debug("Delete track {} from all playlists", track.get().getId());
            playlistPlaybackService.delete(track.get());
            playlistService.delete(track.get());

            log.debug("Delete track {}", track.get().getId());
            trackRepository.deleteById(track.get().getId());

            log.info("Track {} has been removed", track.get().getId());
        }

        afterChange();
    }

    private Optional<Track> findTrack(String path)
    {
        return trackRepository.findFirstByPath(path);
    }

    private void afterChange()
    {
        log.info("Tracks list updated");
    }
}
