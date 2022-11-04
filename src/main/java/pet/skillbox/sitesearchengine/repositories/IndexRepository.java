package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Index;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    void deleteBySiteId(int siteId);
}
