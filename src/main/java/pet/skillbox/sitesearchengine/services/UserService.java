package pet.skillbox.sitesearchengine.services;

import io.micrometer.core.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pet.skillbox.sitesearchengine.model.Role;
import pet.skillbox.sitesearchengine.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final List<User> users;

    public UserService() {
        this.users = List.of(
                new User(1, "anton", "1234", "Антон", "Иванов", Collections.singleton(Role.USER)),
                new User(2, "ivan", "12345", "Сергей", "Петров", Collections.singleton(Role.ADMIN))
        );
    }

    public Optional<User> getByLogin(@NonNull String login) {
        return users.stream()
                .filter(user -> login.equals(user.getLogin()))
                .findFirst();
    }

    public int getIdByLogin(@NonNull String login) {
        return users.stream()
                .filter(user -> login.equals(user.getLogin()))
                .findFirst()
                .get()
                .getId();
    }

}