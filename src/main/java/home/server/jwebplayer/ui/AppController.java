package home.server.jwebplayer.ui;

import home.server.jwebplayer.service.user.UserGuestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController
{

    private final UserGuestService guestService;

    public AppController(UserGuestService guestService)
    {
        this.guestService = guestService;
    }

    @GetMapping("/")
    public String hello()
    {
        return "hello";
    }

    @GetMapping("/player")
    public String player()
    {
        // инициализируется UID гостя, сохраняется в сессию
        guestService.currentUserId();
        // TODO удалять состояния плеера гостей когда их сессия закрывается

        return "player";
    }

    @GetMapping("/controls")
    public String controls()
    {
        return "controls";
    }
}
