package pet.skillbox.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;

import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends JpaRepository<Site,Integer> {
    Site getByUrl(String url);
    @Transactional
    @Modifying
    @Query("UPDATE Site SET status = ?1, statusTime = ?2, lastError = ?3 WHERE id = ?4")
    void updateSiteStatus(Status status, LocalDateTime dateTime, String lastError, Integer id);
    void deleteById(@NotNull Integer id);
    Site getSiteById(Integer id);

}
