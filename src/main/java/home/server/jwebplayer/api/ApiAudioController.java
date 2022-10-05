package home.server.jwebplayer.api;

import home.server.jwebplayer.dto.ApiTrackDto;
import home.server.jwebplayer.dto.PlaylistDto;
import home.server.jwebplayer.entity.Playlist;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.entity.Track;
import home.server.jwebplayer.service.AudioService;
import home.server.jwebplayer.service.playlist.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ApiAudioController
{
    private final AudioService audioService;

    private final PlaylistService playlistService;

    @Autowired
    public ApiAudioController(AudioService audioService, PlaylistService playlistService)
    {
        this.audioService = audioService;
        this.playlistService = playlistService;
    }

    @GetMapping("/api/tracks")
    public ResponseEntity<PlaylistDto> currentPlaylist()
    {
        Playlist defaultPlaylist = playlistService.getDefault();
        var tracks = playlistService.getTracks(defaultPlaylist).stream()
                .map(this::transformPlaylistTrackToDto)
                .toList();

        return ResponseEntity.ok(new PlaylistDto(tracks));
    }

    @GetMapping("/api/playback/current")
    public ResponseEntity<ApiTrackDto> current()
    {
        Track track = audioService.getCurrent();

        if (track == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transformTrackToDto(track));
    }

    @GetMapping("/api/playback/next")
    public ResponseEntity<ApiTrackDto> next()
    {
        audioService.next();

        return current();
    }

    @GetMapping("/api/playback/{hash}")
    public ResponseEntity<ApiTrackDto> play(@PathVariable String hash) throws Exception
    {
        Track track = audioService.getCurrentByHash(hash);

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
        return  new ApiTrackDto(playlistTrack.getTrack(), "/download/" + playlistTrack.getTrack().getId());
    }
}
