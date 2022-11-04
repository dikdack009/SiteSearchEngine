package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma getLemmaByLemmaAndSiteId(String lemma, Integer siteId);
    Lemma getLemmaByLemma(String lemma);
    void deleteBySiteId(Integer siteId);
}
