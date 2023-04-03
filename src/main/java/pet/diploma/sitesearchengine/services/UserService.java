package pet.diploma.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pet.diploma.sitesearchengine.repositories.UserRepository;
import pet.diploma.sitesearchengine.model.User;

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

    public User findUserById(Integer userId) {
        return userRepository.getUserById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean saveUser(User user) {
        User userFromDB = userRepository.getUserByLogin(user.getLogin());
        if (userFromDB != null) {
            return false;
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public boolean updateCheckedUser(String email) {
        User userFromDB = userRepository.getUserByLogin(email);
        if (userFromDB == null) {
            return false;
        }
        userFromDB.setEmailChecked(true);
        userRepository.save(userFromDB);
        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public boolean validateUserPassword(String login, String password) {
        return bCryptPasswordEncoder.matches(password, userRepository.getUserByLogin(login).getPassword());
    }

    public Optional<User> getByLogin(String login) {
        return Optional.ofNullable(userRepository.getUserByLogin(login));
    }

    public int getIdByLogin(String login) {
        return getByLogin(login).orElseThrow().getId();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.getUserByLogin(username);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}