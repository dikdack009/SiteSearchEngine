package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.IndexTmp;
import pet.skillbox.sitesearchengine.model.PageTmp;

@Repository
public interface PageTmpRepository extends JpaRepository<PageTmp, Integer> {
    void deleteBySiteId(Integer siteId);
    PageTmp getFirstBySiteId(Integer siteId);
    @Query("SELECT MAX(id) FROM PageTmp")
    Integer getMaxPageTmpId();
}
