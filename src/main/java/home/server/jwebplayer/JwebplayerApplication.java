package home.server.jwebplayer;

import home.server.jwebplayer.config.ViteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ViteProperties.class)
public class JwebplayerApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(JwebplayerApplication.class, args);
    }

}
