package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Index;

public interface IndexRepository extends JpaRepository<Index, Integer> {
}
