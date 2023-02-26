package home.server.jwebplayer.service.user;

import home.server.jwebplayer.entity.Role;
import home.server.jwebplayer.entity.User;
import home.server.jwebplayer.repository.RoleRepository;
import home.server.jwebplayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Сервис регистрации пользователя.
 */
@Service
public class UserRegisterService
{
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${jwebplayer.user.defaultPassword}")
    private String defaultPassword;

    @Autowired
    public UserRegisterService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрация пользователя с предопределенным паролем
     */
    public void register(String username) throws Exception
    {
        var existed = userRepository.findByUsername(username);

        if (existed.isPresent()) {
            throw new Exception("User already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(staticUserPassword());

        var defaultRole = roleRepository.findByNameIgnoreCase("ROLE_USER");

        if (defaultRole.isPresent()) {
            var roles = new ArrayList<Role>();
            roles.add(defaultRole.get());
            user.setRoles(roles);
        }

        userRepository.save(user);
    }

    private String staticUserPassword()
    {
        return passwordEncoder.encode(defaultPassword);
    }
}
