package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.Site;

import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends JpaRepository<Site,Integer> {
    Site getByUrl(String url);
    @Transactional
    @Modifying
    @Query("UPDATE Site c SET c.status = ?1, c.statusTime = ?2, c.lastError = ?3 WHERE c.url = ?4")
    void updateSiteStatus(String status, LocalDateTime dateTime, String lastError, String url);

}
