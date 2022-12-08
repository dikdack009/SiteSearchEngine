package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma getLemmaByLemmaAndSiteIdAndIsDeleted(String lemma, Integer site_id, Integer isDeleted);
    Lemma getLemmaByLemmaAndIsDeleted(String lemma, Integer isDeleted);
    @Modifying
    @Query("DELETE FROM Lemma WHERE isDeleted > 0")
    void deleteByIsDeleted();
    Lemma getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Lemma SET lemma = ?1, frequency = ?2 WHERE id = ?3")
    void updateLemma(String lemma, Integer frequency, Integer id);
    @Modifying
    @Query("UPDATE Lemma SET isDeleted = ?2 WHERE site = ?1 and isDeleted = 0")
    void updateLemmaDelete(Site site, Integer newIndex);
    Integer countAllByIsDeleted(Integer isDeleted);
}
