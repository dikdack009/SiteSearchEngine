package pet.skillbox.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pet.skillbox.sitesearchengine.model.User;
import pet.skillbox.sitesearchengine.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService  {
    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(Long userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean saveUser(User user) {
        User userFromDB = userRepository.findByLogin(user.getLogin());
        if (userFromDB != null) {
            return false;
        }

        user.setLogin(bCryptPasswordEncoder.encode(user.getLogin()));
        user.setPassword(bCryptPasswordEncoder.encode(user.getLogin() +  user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public Optional<User> getByHashLogin(String login) {
        return userRepository.findAll().stream()
                .filter(user -> bCryptPasswordEncoder.matches(login, user.getLogin())).findFirst();
    }

//    public String getLoginByHashLogin(String login) {
//        return userRepository.findAll().stream()
//                .anyMatch(user -> bCryptPasswordEncoder.matches(login, user.getLogin())) ? ;
//    }

    public boolean validateUserPassword(String pair) {
        return userRepository.findAll().stream()
                .anyMatch(user -> bCryptPasswordEncoder.matches(pair, user.getPassword()));
    }

    public Optional<User> getByLogin(String login) {
        return Optional.ofNullable(userRepository.findByLogin(login));
    }

    public int getIdByLogin(String login) {
        return getByLogin(login).orElseThrow().getId();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByLogin(username);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}