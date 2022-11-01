package home.server.jwebplayer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "playlist_tracks")
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrack
{
    @Id
    @GeneratedValue
    @Getter
    @Setter
    private UUID id;

    @ManyToOne
    @Getter
    @Setter
    private Playlist playlist;

    @ManyToOne
    @Getter
    @Setter
    private Track track;

    @PrePersist
    public void generateId()
    {
        id = UUID.randomUUID();
    }
}
