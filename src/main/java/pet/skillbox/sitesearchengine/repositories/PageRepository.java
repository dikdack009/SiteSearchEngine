package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Page;

public interface PageRepository extends JpaRepository<Page, Integer> {
    Page getByPath(String path);
}
