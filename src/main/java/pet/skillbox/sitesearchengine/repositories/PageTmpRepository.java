package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.PageTmp;

@Repository
public interface PageTmpRepository extends JpaRepository<PageTmp, Integer> {
    void deleteBySiteId(Integer siteId);
}
