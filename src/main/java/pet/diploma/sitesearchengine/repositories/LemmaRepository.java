package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma getLemmaByLemmaAndSiteIdAndIsDeleted(String lemma, Integer site_id, Integer isDeleted);
    Lemma getLemmaByLemmaAndIsDeleted(String lemma, Integer isDeleted);
    @Modifying
    @Query("DELETE FROM Lemma WHERE isDeleted > 0")
    void deleteByIsDeleted();
    Lemma getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Lemma SET isDeleted = ?2 WHERE site = ?1 and isDeleted = 0")
    void updateLemmaDelete(Site site, Integer newIndex);
    Integer countAllByIsDeleted(Integer isDeleted);
}
