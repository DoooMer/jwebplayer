package home.server.jwebplayer.service.playlist;

import home.server.jwebplayer.entity.Playlist;
import home.server.jwebplayer.entity.PlaylistTrack;
import home.server.jwebplayer.entity.Track;
import home.server.jwebplayer.repository.PlaylistRepository;
import home.server.jwebplayer.repository.PlaylistTrackRepository;
import home.server.jwebplayer.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PlaylistService
{
    private final static String DEFAULT_PLAYLIST_NAME = "default";

    private final TrackRepository trackRepository;

    private final PlaylistRepository playlistRepository;

    private final PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    public PlaylistService(
            TrackRepository trackRepository,
            PlaylistRepository playlistRepository,
            PlaylistTrackRepository playlistTrackRepository
    )
    {
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
    }

    public void generateDefault()
    {
        var playlist = findDefaultPlaylist();
        var playlistTracks = playlistTrackRepository.findAllByPlaylistId(playlist.getId());

        for (Track track : trackRepository.findAll()) {
            playlistTracks.stream()
                    .filter(playlistTrack -> playlistTrack.getTrack().equals(track))
                    .findFirst()
                    .orElseGet(() -> {
                        var newPlaylistTrack = new PlaylistTrack();
                        newPlaylistTrack.setPlaylist(playlist);
                        newPlaylistTrack.setTrack(track);

                        playlistTracks.add(newPlaylistTrack);

                        return newPlaylistTrack;
                    });
        }

        playlistTrackRepository.saveAll(playlistTracks);
    }

    public Playlist getById(UUID id)
    {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist is not exists"));
    }

    public Playlist getDefault()
    {
        return playlistRepository.findByName(DEFAULT_PLAYLIST_NAME)
                .orElseThrow(() -> new RuntimeException("Default playlist not generated yet"));
    }

    public List<PlaylistTrack> getTracks(Playlist playlist)
    {
        return playlistTrackRepository.findAllByPlaylistId(playlist.getId());
    }

    public void addTrackToDefaultPlaylist(Track track)
    {
        var defaultPlaylist = findDefaultPlaylist();

        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylist(defaultPlaylist);
        playlistTrack.setTrack(track);

        playlistTrackRepository.save(playlistTrack);
    }

    public void delete(Track track)
    {
        playlistTrackRepository.deleteAll(playlistTrackRepository.findAllByTrackId(track.getId()));
    }

    private Playlist findDefaultPlaylist()
    {
        return playlistRepository.findByName(DEFAULT_PLAYLIST_NAME).orElseGet(() -> {
            var newPlaylist = new Playlist();
            newPlaylist.setName(DEFAULT_PLAYLIST_NAME);

            playlistRepository.save(newPlaylist);

            return newPlaylist;
        });
    }
}
