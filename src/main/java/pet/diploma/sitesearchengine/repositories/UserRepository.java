package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.diploma.sitesearchengine.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByLogin(String username);
    User getUserById (int id);
}
