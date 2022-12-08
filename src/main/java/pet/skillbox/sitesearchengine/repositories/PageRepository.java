package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Page;
import pet.skillbox.sitesearchengine.model.Site;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page getByPath(String path);
    Integer countPagesBySiteId(Integer siteId);
    Integer countPagesByCode(int code);
    @Modifying
    @Query("DELETE FROM Page WHERE isDeleted > 0")
    void deleteByIsDeleted();
    @Query("SELECT MAX(id) FROM Page")
    Integer getMaxPageId();
    Page getFirstBySiteId(Integer siteId);
    @Modifying
    @Query("UPDATE Page SET isDeleted = ?2 WHERE site = ?1 and isDeleted = 0")
    void updatePageDelete(Site site, Integer newNumber);
    Integer countAllByIsDeleted(Integer isDeleted);
}
