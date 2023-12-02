package home.server.jwebplayer.service.vite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import home.server.jwebplayer.config.ViteProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис интеграции с vite.
 * Реализует получение ссылки на файл с учетом конфигурации:
 * - хост dev сервера vite
 * - путь из манифеста production сборки
 */
@Component
@Slf4j
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

        log.info("Loading vite configuration (" + properties.manifestPath() + ", " + properties.host() + ")");
    }

    public String createScriptUrl(String script)
    {
        var url = getUrlByHost(script);

        return url != null ? url : getUrlByManifest(script);
    }

    @PostConstruct
    public void loadManifest() throws IOException
    {

        if (properties.manifestPath() == null || properties.manifestPath().length() == 0) {
            return;
        }

        var inputStream = getClass().getResourceAsStream("/" + properties.manifestPath().trim() + "manifest.json");

        if (inputStream == null) {
            log.error("Manifest file access error (path: /" + properties.manifestPath().trim() + "manifest.json)");
            return;
        }

        var reader = new BufferedReader(new InputStreamReader(inputStream));
        var json = reader.lines().collect(Collectors.joining("\n"));

        manifest = (new ObjectMapper()).readValue(json, new TypeReference<>()
        {
        });
    }

    /**
     * Получение ссылки на файл через манифест
     *
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
     *
     * @param script Относительный путь к файлу
     * @return Ссылка на файл
     */
    private String getUrlByHost(String script)
    {
        return properties.host() != null ? properties.host() + "/" + script : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileEntry
    {
        public List<String> css;
        public String file;
    }

}
