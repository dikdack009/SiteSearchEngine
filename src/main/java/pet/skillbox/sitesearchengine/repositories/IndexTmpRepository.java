package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.IndexTmp;

@Repository
public interface IndexTmpRepository extends JpaRepository<IndexTmp, Integer> {
    void deleteBySiteId(int siteId);
}
