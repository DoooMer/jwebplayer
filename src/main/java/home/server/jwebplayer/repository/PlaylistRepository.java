package home.server.jwebplayer.repository;

import home.server.jwebplayer.entity.Playlist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistRepository extends CrudRepository<Playlist, UUID>
{
}
