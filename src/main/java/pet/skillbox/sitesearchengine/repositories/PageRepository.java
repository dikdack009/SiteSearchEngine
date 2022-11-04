package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page getByPath(String path);
    Integer countPagesBySiteId(Integer siteId);
    Integer countPagesByCode(int code);
    void deleteBySiteId(Integer siteId);
}
