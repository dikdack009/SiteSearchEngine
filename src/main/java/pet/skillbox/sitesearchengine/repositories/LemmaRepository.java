package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma getLemmaByLemmaAndSiteId(String lemma, Integer siteId);
    Lemma getLemmaByLemma(String lemma);
    void deleteByIsDeleted(Integer isDeleted);
    Lemma getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Lemma SET lemma = ?1, frequency = ?2 WHERE id = ?3")
    void updateLemma(String lemma, Integer frequency, Integer id);
    @Modifying
    @Query("UPDATE Lemma SET isDeleted = ?1 WHERE site = ?2")
    void updateLemmaDelete(Integer isDeleted, Site site);
}
