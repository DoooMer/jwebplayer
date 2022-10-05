package home.server.jwebplayer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tracks")
@AllArgsConstructor
@NoArgsConstructor
public class Track
{
    @Id
    @Getter
    @Setter
    private String id; // hash

    @Column(unique = true)
    @Getter
    @Setter
    private String path; // relative path

    @Column
    @Getter
    @Setter
    private String name; // track name

    @Column
    @Getter
    @Setter
    private String directory; // directory of path

    public Track(String hash, String path) //throws UnsupportedEncodingException
    {
        id = hash;
        this.path = path;//URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
        setDirectoryAndNameByPath(path);
    }

    @Override
    public boolean equals(Object obj)
    {

        if (obj instanceof Track) {
            return ((Track) obj).getId().equals(id);
        }

        return super.equals(obj);
    }

    /**
     * По пути определяет имя файла и директорию
     */
    private void setDirectoryAndNameByPath(String path)
    {
        var index = path.lastIndexOf('/');

        if (index >= 0) {
            directory = path.substring(0, index);
        }

        name = path.substring(Math.max(index + 1, 0), path.length() - 4);
    }
}
