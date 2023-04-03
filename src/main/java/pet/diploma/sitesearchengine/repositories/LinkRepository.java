package pet.diploma.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pet.diploma.sitesearchengine.model.Link;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Integer> {
    @NotNull List<Link> findAll();
    Link getLinkByLinkAndUserId(String link, int userId);
    void deleteLinkByLinkAndUserId(String link, int userId);
    @Transactional
    @Modifying
    @Query("UPDATE Link SET isSelected = ?2 WHERE link= ?1 and userId = ?3")
    void updateLink(String link, Integer isSelected, int userId);

//    @Override
//    void deleteAll();
}
