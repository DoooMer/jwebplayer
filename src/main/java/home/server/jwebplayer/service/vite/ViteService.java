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

/**
 * Сервис интеграции с vite.
 * Реализует получение ссылки на файл с учетом конфигурации:
 * - хост dev сервера vite
 * - путь из манифеста production сборки
 */
@Component
public class ViteService
{
    /**
     * Настройки vite (путь к манифесту и хост)
     * Хост приоритетнее манифеста,
     * если указан хост - ссылка на файл будет с этим хостом,
     * иначе будет использоваться относительные ссылки из манифеста
     */
    private final ViteProperties properties;

    private Map<String, FileEntry> manifest;

    @Autowired
    public ViteService(ViteProperties properties)
    {
        if (properties.host() == null && properties.manifestPath() == null) {
            throw new IllegalArgumentException("Настройки vite некорректны, требуется host или manifest-path");
        }

        this.properties = properties;
    }

    public String createScriptUrl(String script)
    {
        var url = getUrlByHost(script);

        return url != null ? url : getUrlByManifest(script);
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

    /**
     * Получение ссылки на файл через манифест
     * @param script Относительный путь к файлу
     * @return Ссылка на файл
     */
    private String getUrlByManifest(String script)
    {
        if (manifest != null) {
            var entry = manifest.get(script);

            if (entry != null) {
                return "player/" + entry.file;
            }

        }

        return null;
    }

    /**
     * Получение ссылки на файл с хостом vite
     * @param script Относительный путь к файлу
     * @return Ссылка на файл
     */
    private String getUrlByHost(String script)
    {
        return properties.host() != null ? properties.host() + "/" + script : null;
    }

    private static class FileEntry
    {
        public List<String> css;
        public String file;
    }

}
