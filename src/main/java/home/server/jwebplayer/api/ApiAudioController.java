package home.server.jwebplayer.api;

import home.server.jwebplayer.dto.ApiTrackDto;
import home.server.jwebplayer.dto.PlaylistDto;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.entity.Track;
import home.server.jwebplayer.repository.PlaylistTrackRepository;
import home.server.jwebplayer.repository.TrackRepository;
import home.server.jwebplayer.service.playback.PlaybackService;
import home.server.jwebplayer.service.user.UserGuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class ApiAudioController
{
    private final PlaybackService playbackService;
    private final TrackRepository trackRepository;
    private final UserGuestService guestService;
    private final PlaylistTrackRepository playlistTrackRepository;

    public ApiAudioController(
            PlaybackService playbackService,
            TrackRepository trackRepository,
            UserGuestService guestService,
            PlaylistTrackRepository playlistTrackRepository
    )
    {
        this.playbackService = playbackService;
        this.trackRepository = trackRepository;
        this.guestService = guestService;
        this.playlistTrackRepository = playlistTrackRepository;
    }

    @GetMapping("/api/tracks")
    @Deprecated
    public ResponseEntity<PlaylistDto> currentPlaylist()
    {
        UUID userId = guestService.currentUserId();
        UUID playlistId = playbackService.getForUser(userId).getPlaylistId();

        var tracks = playlistTrackRepository.findAllByPlaylistId(playlistId)
                .stream()
                .map(this::transformPlaylistTrackToDto)
                .toList();

        return ResponseEntity.ok(new PlaylistDto(tracks));
    }

    @GetMapping("/api/playback/current")
    public ResponseEntity<ApiTrackDto> current()
    {
        UUID userId = guestService.currentUserId();
        String trackId = null;

        if (userId != null) {
            trackId = playbackService.getForUser(userId).current();
        }

        if (trackId == null) {
            return ResponseEntity.notFound().build();
        }

        var track = trackRepository.findById(trackId);

        return track
                .map(value -> ResponseEntity.ok(transformTrackToDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/playback/next")
    public ResponseEntity<ApiTrackDto> next()
    {
        UUID userId = guestService.currentUserId();

        if (userId != null) {
            playbackService.getForUser(userId).next();
        }

        return current();
    }

    // TODO add prev

    @GetMapping("/api/playback/{trackId}")
    public ResponseEntity<ApiTrackDto> play(@PathVariable String trackId) throws Exception
    {
        UUID userId = guestService.currentUserId();

        if (userId != null) {
            playbackService.getForUser(userId).select(trackId);
        }

        Track track = trackRepository.findById(trackId).orElse(null);

        if (track == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transformTrackToDto(track));
    }

    private ApiTrackDto transformTrackToDto(Track track)
    {
        return new ApiTrackDto(track, "/download/" + track.getId());
    }

    private ApiTrackDto transformPlaylistTrackToDto(PlaylistTrack playlistTrack)
    {
        return new ApiTrackDto(playlistTrack.getTrack(), "/download/" + playlistTrack.getTrack().getId());
    }
}
