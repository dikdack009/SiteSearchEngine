package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Index;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Site;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    void deleteByIsDeleted(Integer isDeleted);

    Index getFirstBySiteId(Integer siteId);
    Index getBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Index SET isDeleted = ?1 WHERE site = ?2")
    void updateIndexDelete(Integer isDeleted, Site site);
    Integer countAllByIsDeleted(Integer isDeleted);

}
