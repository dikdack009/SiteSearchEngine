package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page getByPath(String path);
    Integer countPagesBySiteId(Integer siteId);
    Integer countPagesByCode(int code);
    @Modifying
    @Query("DELETE FROM Page WHERE isDeleted > 0")
    void deleteByIsDeleted();
    Page getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Page SET isDeleted = ?2 WHERE site = ?1 and isDeleted = 0")
    void updatePageDelete(Site site, Integer newNumber);
    Integer countAllByIsDeletedAndSite(Integer isDeleted, Site site);
    Page getPageById(Integer id);
}
