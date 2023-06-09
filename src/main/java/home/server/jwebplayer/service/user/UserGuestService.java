package home.server.jwebplayer.service.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Service
public class UserGuestService
{
    private final HttpSession httpSession;

    private final UserService userService;

    public UserGuestService(HttpSession httpSession, UserService userService)
    {
        this.httpSession = httpSession;
        this.userService = userService;
    }

    public UUID currentUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"));

        if (!isAnonymous || authentication.getName() == null) {
            var user = userService.getByUsername(authentication.getName());

            if (user != null) {
                return user.getId();
            }

        }

        String sessionUid = (String) httpSession.getAttribute("uid");

        if (sessionUid != null) {
            return UUID.fromString(sessionUid);
        }


        UUID uid = UUID.nameUUIDFromBytes(httpSession.getId().getBytes());
        httpSession.setAttribute("uid", uid.toString());

        return uid;
    }
}
