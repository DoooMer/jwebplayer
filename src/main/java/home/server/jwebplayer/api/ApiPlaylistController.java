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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> create(@RequestBody PlaylistData playlistData)
    {
        // default - зарезервированное название плейлиста по-умолчанию
        if (playlistData.getName().equalsIgnoreCase("default")) {
            return ResponseEntity.unprocessableEntity().body("Playlist's name 'default' is reserved. You cannot use it.");
        }

        var playlist = new Playlist();
        playlist.setName(playlistData.getName());
        // TODO привязка к пользователю

        return ResponseEntity.ok(transformPlaylistToDto(playlistRepository.save(playlist)));
    }

    /**
     * Добавление трека в плейлист.
     */
    @PostMapping("/api/playlists/{playlistId}")
    public ResponseEntity<?> addTrack(@PathVariable UUID playlistId, @RequestBody PlaylistTrackData playlistTrackData)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }

        if (playlist.get().getName().equalsIgnoreCase("default")) {
            return ResponseEntity.unprocessableEntity().body("You cannot add track to default playlist.");
        }
        // TODO проверка принадлежности плейлиста

        var track = trackRepository.findById(playlistTrackData.getTrackId().toString());

        if (track.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown track.");
        }

        var playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylist(playlist.get());
        playlistTrack.setTrack(track.get());

        playlistTrackRepository.save(playlistTrack);

        return ResponseEntity.ok(transformPlaylistTrackToDto(playlistTrack));
    }

    /**
     * Редактирование плейлиста.
     */
    @PatchMapping("/api/playlists/{playlistId}")
    public ResponseEntity<?> updatePlaylist(@PathVariable UUID playlistId, @RequestBody PlaylistData playlistData)
    {
        var playlist = playlistRepository.findById(playlistId);

        if (playlist.isEmpty()) {
            return ResponseEntity.badRequest().body("Unknown playlist.");
        }

        var model = playlist.get();
        model.setName(playlistData.getName());

        return ResponseEntity.ok(transformPlaylistToDto(playlistRepository.save(model)));
    }

    /**
     * Удаление плейлиста.
     */
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
     * Удаленине трека из плейлиста.
     */
    @DeleteMapping("/api/playlists/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> deleteTrack(@PathVariable UUID playlistId, @PathVariable String trackId)
    {
        var trackInPlaylist = playlistTrackRepository.findFirstByPlaylistIdAndTrackId(playlistId, trackId);

        if (trackInPlaylist.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("There is no track or playlist."));
        }

        // Запрещается удалять треки из плейлиста по-умолчанию
        if (trackInPlaylist.get().getPlaylist().getName().equalsIgnoreCase("default")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError("You cannot delete track from default playlist."));
        }

        playlistTrackRepository.deleteById(trackInPlaylist.get().getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Выбор активного плейлиста для пользователя.
     */
    @PostMapping("/api/playlists/{playlistId}/select")
    @Deprecated
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
    public static class PlaylistData
    {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PlaylistTrackData
    {
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

    @Getter
    public static class ListDTO
    {
        private final List<ApiPlaylistDTO> playlists;

        private final int total;

        private ListDTO(List<ApiPlaylistDTO> playlists)
        {
            this.playlists = playlists;
            total = playlists.size();
        }
    }

    @AllArgsConstructor
    public static class ApiPlaylistDTO
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

    @AllArgsConstructor
    @Getter
    public static class ApiError
    {
        private String message;
    }
}
