package pet.skillbox.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pet.skillbox.sitesearchengine.model.Field;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
    @NotNull List<Field> findAll();
}
