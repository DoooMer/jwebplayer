package home.server.jwebplayer.ui;

import home.server.jwebplayer.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
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
            userService.register(username);
        } catch (Exception e) {
            return "register";
        }

        return "redirect:/login";
    }
}
