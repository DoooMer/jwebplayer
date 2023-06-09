package home.server.jwebplayer.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionExpiredListener implements ApplicationListener<SessionDestroyedEvent>
{

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event)
    {
        UUID uid = UUID.nameUUIDFromBytes(event.getId().getBytes());
        System.out.println("session destr " + uid);
        // TODO найти UserPlayback по uid и удалить
    }
}
