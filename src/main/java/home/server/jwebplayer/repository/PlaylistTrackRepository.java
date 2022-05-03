package home.server.jwebplayer.repository;

import home.server.jwebplayer.entity.PlaylistTrack;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistTrackRepository extends CrudRepository<PlaylistTrack, UUID>
{
    List<PlaylistTrack> findAllByPlaylistId(UUID playlistId);
}
