package home.server.jwebplayer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "playlists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Playlist
{
    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String name;

    @PrePersist
    public void generateId()
    {
        id = UUID.randomUUID();
    }
}
