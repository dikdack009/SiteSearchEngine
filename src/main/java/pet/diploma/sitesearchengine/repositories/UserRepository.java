package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import pet.diploma.sitesearchengine.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    @Modifying
    @Query("update User u set u.notify = ?1 where u.login = ?2")
    void updateUserNotifyByLogin(@NonNull boolean notify, @NonNull String login);
    @Transactional
    @Modifying
    @Query("update User set password = ?1 where login = ?2")
    void updateUserPasswordByLogin(@NonNull String password, @NonNull String login);
    User getUserByLogin(String username);
    User getUserById (int id);
}
