package home.server.jwebplayer.service.user;

import home.server.jwebplayer.entity.Role;
import home.server.jwebplayer.entity.User;
import home.server.jwebplayer.repository.RoleRepository;
import home.server.jwebplayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Сервис управления пользователями
 */
@Service
public class UserService implements UserDetailsService
{
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${jwebplayer.user.defaultPassword}")
    private String defaultPassword;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Получение UserDetails для авторизации
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        var result = userRepository.findByUsername(username);

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        var user = result.get();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities(user.getRoles()))
                .build();
    }

    private Collection<GrantedAuthority> authorities(Collection<Role> roles)
    {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
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

    public String getDefaultPassword()
    {
        return defaultPassword;
    }

    private String staticUserPassword()
    {
        return passwordEncoder.encode(defaultPassword);
    }
}
