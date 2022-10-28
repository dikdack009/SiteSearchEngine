package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site,Integer> {
    Site getByName(String name);
    List<Site> findAllBy(Status status);
}
