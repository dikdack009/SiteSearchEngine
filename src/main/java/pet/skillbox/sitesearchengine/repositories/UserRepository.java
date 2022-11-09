package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
