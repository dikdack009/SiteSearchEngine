package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Lemma;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma getLemmaByLemma(String lemma);
}
