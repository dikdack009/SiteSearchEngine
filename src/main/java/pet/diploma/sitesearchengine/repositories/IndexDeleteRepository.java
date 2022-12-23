package pet.diploma.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pet.diploma.sitesearchengine.model.DeleteIndex;

public interface IndexDeleteRepository extends JpaRepository<DeleteIndex, Integer> {
    @Modifying
    @Query("UPDATE DeleteIndex SET deleteNumber = deleteNumber + 1 WHERE id = 1")
    void updateIndexDeleteDelete(Integer newValue);
    @Modifying
    @Query("UPDATE DeleteIndex SET deleteNumber = 1 WHERE id = 1")
    void updateDefaultDeleteDelete();
}
