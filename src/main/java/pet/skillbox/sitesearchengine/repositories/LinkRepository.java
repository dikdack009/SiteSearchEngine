package pet.skillbox.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.Link;
import pet.skillbox.sitesearchengine.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Integer> {
    @NotNull List<Link> findAll();
    Link getLinkByLink(String link);
    void deleteLinkByLink(String link);
    @Transactional
    @Modifying
    @Query("UPDATE Link SET isSelected = ?2 WHERE link= ?1")
    void updateLink(String link, Integer isSelected);

//    @Override
//    void deleteAll();
}
