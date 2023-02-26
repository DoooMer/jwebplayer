package home.server.jwebplayer.ui;

import home.server.jwebplayer.service.user.UserRegisterService;
import home.server.jwebplayer.service.user.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController
{
    private final UserService userService;

    private final UserRegisterService userRegisterService;

    public UserController(UserService userService, UserRegisterService userRegisterService)
    {
        this.userService = userService;
        this.userRegisterService = userRegisterService;
    }

    @GetMapping("/login")
    public String loginForm(Model model)
    {
        model.addAttribute("defaultPassword", userService.getDefaultPassword());

        return "login";
    }

    @GetMapping("/register")
    public String registerForm()
    {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username)
    {
        try {
            userRegisterService.register(username);
        } catch (Exception e) {
            return "register";
        }

        // get user's details for new user
        var user = userService.loadUserByUsername(username);

        // authenticate user immediately
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authenticationToken);

        return "redirect:/";
    }
}
