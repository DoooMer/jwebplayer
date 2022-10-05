package home.server.jwebplayer.api;

import home.server.jwebplayer.entity.Playlist;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.repository.PlaylistRepository;
import home.server.jwebplayer.repository.PlaylistTrackRepository;
import home.server.jwebplayer.repository.TrackRepository;
import home.server.jwebplayer.service.playlist.UserPlaylistService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class ApiPlaylistController
{
    private final PlaylistRepository playlistRepository;

    private final PlaylistTrackRepository playlistTrackRepository;

    private final TrackRepository trackRepository;

    private final UserPlaylistService userPlaylistService;

    @Autowired
    public ApiPlaylistController(
            PlaylistRepository playlistRepository,
            PlaylistTrackRepository playlistTrackRepository,
            TrackRepository trackRepository,
            UserPlaylistService userPlaylistService
    )
    {
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
        this.trackRepository = trackRepository;
        this.userPlaylistService = userPlaylistService;
    }

    @GetMapping("/api/playlists")
    public ResponseEntity<ListDTO> index()
    {
        var playlists = (List<Playlist>) playlistRepository.findAll();
        var list = playlists
                .stream()
                .map(this::transformPlaylistToDto)
                .toList();

        return ResponseEntity.ok(new ListDTO(list));
    }

    @PostMapping("/api/playlists")
    public ResponseEntity<ApiPlaylistDTO> create(@RequestParam String name)
    {
        var playlist = new Playlist();
        playlist.setName(name);

        playlistRepository.save(playlist);

        return ResponseEntity.ok(transformPlaylistToDto(playlist));
    }

    @PostMapping("/api/playlists/{playlistId}")
    public ResponseEntity<?> addTrack(@PathVariable UUID playlistId, @RequestParam String trackId)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }

        var track = trackRepository.findById(trackId);

        if (track.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown track.");
        }

        var playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylist(playlist.get());
        playlistTrack.setTrack(track.get());

        playlistTrackRepository.save(playlistTrack);

        return ResponseEntity.ok(transformPlaylistTrackToDto(playlistTrack));
    }

    @GetMapping("/api/playlists/{playlistId}/tracks")
    public ResponseEntity<?> tracks(@PathVariable UUID playlistId)
    {
        var playlistTracks = playlistTrackRepository.findAllByPlaylistId(playlistId);
        var list = playlistTracks
                .stream()
                .map(this::transformPlaylistTrackToDto)
                .toList();

        return ResponseEntity.ok(new ApiListTracksDTO(list));
    }

    @PostMapping("/api/playlist/{playlistId}/select")
    public ResponseEntity<?> select(@PathVariable UUID playlistId)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userPlaylistService.select(playlist.get());

        // TODO mark playlist as selected
        return ResponseEntity.ok().build();
    }

    private ApiPlaylistDTO transformPlaylistToDto(Playlist playlist)
    {
        return new ApiPlaylistDTO(playlist);
    }

    private ApiPlaylistTrackDTO transformPlaylistTrackToDto(PlaylistTrack playlistTrack)
    {
        return new ApiPlaylistTrackDTO(playlistTrack);
    }

    private static class ListDTO
    {
        @Getter
        private final List<ApiPlaylistDTO> playlists;

        @Getter
        private final int total;

        private ListDTO(List<ApiPlaylistDTO> playlists)
        {
            this.playlists = playlists;
            total = playlists.size();
        }
    }

    @AllArgsConstructor
    private static class ApiPlaylistDTO
    {
        private Playlist playlist;

        public UUID getId()
        {
            return playlist.getId();
        }

        public String getName()
        {
            return playlist.getName();
        }
    }

    @AllArgsConstructor
    private static class ApiPlaylistTrackDTO
    {
        private PlaylistTrack playlistTrack;

        public UUID getPlaylistId()
        {
            return playlistTrack.getPlaylist().getId();
        }

        public String getTrackId()
        {
            return playlistTrack.getTrack().getId();
        }
    }

    private static class ApiListTracksDTO
    {
        @Getter
        private final List<ApiPlaylistTrackDTO> playlistTracks;

        @Getter
        private final int total;

        private ApiListTracksDTO(List<ApiPlaylistTrackDTO> playlistTracks)
        {
            this.playlistTracks = playlistTracks;
            total = playlistTracks.size();
        }
    }
}
