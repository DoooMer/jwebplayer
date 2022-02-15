package home.server.jwebplayer;

import home.server.jwebplayer.service.watch.DirectoryWatcher;
import home.server.jwebplayer.service.watch.TrackListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppConfig
{
    @Value("${tracks.rootDirectory}")
    private String rootDirectory;

    @Bean
    public DirectoryWatcher directoryWatcher(TrackListener trackListener)
    {
        Path rootPath = Paths.get(rootDirectory.strip());
        return new DirectoryWatcher(rootPath, trackListener);
    }
}
