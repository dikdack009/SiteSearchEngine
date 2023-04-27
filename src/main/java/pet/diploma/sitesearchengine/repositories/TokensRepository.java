package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pet.diploma.sitesearchengine.model.Token;

@Repository
public interface TokensRepository extends JpaRepository<Token, Integer> {
    Token getTokensByLogin(String login);
    @Transactional
    @Modifying
    @Query("update Token set refreshToken = ?1 where login = ?2")
    void updateTokenByLogin(String token, String login);
}
