package pet.skillbox.sitesearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Field;

public interface FieldRepository extends JpaRepository<Field, Integer> {
}
