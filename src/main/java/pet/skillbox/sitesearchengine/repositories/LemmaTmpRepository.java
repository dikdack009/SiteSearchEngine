package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.IndexTmp;
import pet.skillbox.sitesearchengine.model.LemmaTmp;

@Repository
public interface LemmaTmpRepository extends JpaRepository<LemmaTmp, Integer> {
    void deleteBySiteId(Integer siteId);
    LemmaTmp getFirstBySiteId(Integer siteId);
}
