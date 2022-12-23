package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Index;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Modifying
    @Query("DELETE FROM Index WHERE isDeleted > 0")
    void deleteByIsDeleted();

    Index getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Index SET isDeleted = ?2 WHERE site = ?1 and isDeleted = 0")
    void updateIndexDelete(Site site, Integer newNumber);
    Integer countAllByIsDeleted(Integer isDeleted);

}
