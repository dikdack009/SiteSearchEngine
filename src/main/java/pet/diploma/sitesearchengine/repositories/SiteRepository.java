package pet.diploma.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Status;

import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends JpaRepository<Site,Integer> {
    Site getByUrlAndUserId(String url, int userId);
    @Transactional
    @Modifying
    @Query("UPDATE Site SET status = ?1, statusTime = ?2, lastError = ?3 WHERE id = ?4")
    void updateSiteStatus(Status status, LocalDateTime dateTime, String lastError, Integer id);
    Site getSiteByUrl(String url);
    @Transactional
    @Modifying
    @Query("UPDATE Site SET isDeleted = isDeleted + ?1 WHERE id = ?1 and isDeleted = 0")
    void updateSiteDelete(Integer id, Integer isDeleted);
    @Modifying
    @Query("DELETE FROM Site WHERE isDeleted > 0")
    void deleteByIsDeleted();
}
