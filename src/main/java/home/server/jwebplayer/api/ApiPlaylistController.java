package home.server.jwebplayer.api;

import home.server.jwebplayer.entity.Playlist;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.repository.PlaylistRepository;
import home.server.jwebplayer.repository.PlaylistTrackRepository;
import home.server.jwebplayer.repository.TrackRepository;
import home.server.jwebplayer.service.playback.PlaybackService;
import home.server.jwebplayer.service.user.UserGuestService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Controller
public class ApiPlaylistController
{
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final TrackRepository trackRepository;
    private final PlaybackService playbackService;
    private final UserGuestService guestService;

    @Autowired
    public ApiPlaylistController(
            PlaylistRepository playlistRepository,
            PlaylistTrackRepository playlistTrackRepository,
            TrackRepository trackRepository,
            PlaybackService playbackService,
            UserGuestService guestService
    )
    {
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
        this.trackRepository = trackRepository;
        this.playbackService = playbackService;
        this.guestService = guestService;
    }

    /**
     * Список плейлистов.
     */
    @GetMapping("/api/playlists")
    public ResponseEntity<ListDTO> index()
    {
        // TODO персонализировать (общие + личные)
        var playlists = (List<Playlist>) playlistRepository.findAll();
        var list = playlists
                .stream()
                .map(this::transformPlaylistToDto)
                .toList();

        return ResponseEntity.ok(new ListDTO(list));
    }

    /**
     * Добавление плейлиста.
     */
    @PostMapping("/api/playlists")
    public ResponseEntity<ApiPlaylistDTO> create(@RequestBody CreatePlaylist createPlaylist)
    {
        var playlist = new Playlist();
        playlist.setName(createPlaylist.getName());
        // TODO привязка к пользователю

        return ResponseEntity.ok(transformPlaylistToDto(playlistRepository.save(playlist)));
    }

    /**
     * Добавление трека в плейлист.
     */
    @PostMapping("/api/playlists/{playlistId}")
    public ResponseEntity<?> addTrack(@PathVariable UUID playlistId, @RequestBody PlaylistAddTrack playlistAddTrack)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }
        // TODO проверка принадлежности плейлиста

        var track = trackRepository.findById(playlistAddTrack.getTrackId().toString());

        if (track.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown track.");
        }

        var playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylist(playlist.get());
        playlistTrack.setTrack(track.get());

        playlistTrackRepository.save(playlistTrack);

        return ResponseEntity.ok(transformPlaylistTrackToDto(playlistTrack));
    }

    @PatchMapping("/api/playlists/{playlistId}")
    public ResponseEntity<?> updatePlaylist(@PathVariable UUID playlistId, @RequestBody CreatePlaylist createPlaylist)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }

        var model = playlist.get();
        model.setName(createPlaylist.getName());

        return ResponseEntity.ok(transformPlaylistToDto(playlistRepository.save(model)));
    }

    @DeleteMapping("/api/playlists/{playlistId}")
    @Transactional
    public ResponseEntity<?> deletePlaylist(@PathVariable UUID playlistId)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }

        playlistTrackRepository.deleteAllByPlaylistId(playlistId);
        playlistRepository.deleteById(playlistId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Список треков из плейлиста.
     */
    @GetMapping("/api/playlists/{playlistId}/tracks")
    public ResponseEntity<?> tracks(@PathVariable UUID playlistId)
    {
        // TODO проверка принадлежности плейлиста
        var playlistTracks = playlistTrackRepository.findAllByPlaylistId(playlistId);
        var list = playlistTracks
                .stream()
                .map(this::transformPlaylistTrackToDto)
                .toList();

        return ResponseEntity.ok(new ApiListTracksDTO(list));
    }

    /**
     * Выбор активного плейлиста для пользователя.
     */
    @PostMapping("/api/playlists/{playlistId}/select")
    public ResponseEntity<?> select(@PathVariable UUID playlistId)
    {
        UUID userId = guestService.currentUserId();

        if (userId != null) {
            playbackService.selectPlaylistForUser(userId, playlistId);
        }

        // TODO mark playlist as selected

        return ResponseEntity.ok().build();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    protected static class CreatePlaylist
    {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    private static class PlaylistAddTrack
    {
        @Getter
        private UUID trackId;
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

        public String getId()
        {
            return playlistTrack.getTrack().getId();
        }

        public String getName()
        {
            return playlistTrack.getTrack().getName();
        }

        public String getDirectory()
        {
            return playlistTrack.getTrack().getDirectory();
        }

        public String getDownloadUrl()
        {
            return "/download/" + playlistTrack.getTrack().getId();
        }
    }

    @Getter
    private static class ApiListTracksDTO
    {
        private final List<ApiPlaylistTrackDTO> playlistTracks;

        private final int total;

        private ApiListTracksDTO(List<ApiPlaylistTrackDTO> playlistTracks)
        {
            this.playlistTracks = playlistTracks;
            total = playlistTracks.size();
        }
    }
}
