package home.server.jwebplayer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vite")
public record ViteProperties(String manifestPath, String host)
{
}
