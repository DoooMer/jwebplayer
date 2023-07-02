package home.server.jwebplayer.service.vite;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import home.server.jwebplayer.config.ViteProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ViteService
{
    private final ViteProperties properties;

    private Map<String, FileEntry> manifest;

    @Autowired
    public ViteService(ViteProperties properties)
    {
        this.properties = properties;
    }

    public String createScriptUrl(String script)
    {
        if (manifest != null) {
            var entry = manifest.get(script);

            if (entry != null) {
                return "player/" + entry.file;
            }

        }

        return properties.host() != null ? properties.host() + "/" + script : script;
    }

    @PostConstruct
    public void loadManifest() throws IOException, URISyntaxException
    {

        if (properties.manifestPath() == null || properties.manifestPath().length() == 0) {
            return;
        }

        var path = Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(properties.manifestPath() + "manifest.json")).toURI());

        Stream<String> lines = Files.lines(path);
        var json = lines.collect(Collectors.joining("\n"));
        lines.close();

        var mapper = new ObjectMapper();
        manifest = mapper.readValue(json, new TypeReference<>()
        {
        });
    }

    private static class FileEntry
    {
        public List<String> css;
        public String file;
        public Boolean isEntry;
        public String src;
        public List<String> imports;
    }

}
