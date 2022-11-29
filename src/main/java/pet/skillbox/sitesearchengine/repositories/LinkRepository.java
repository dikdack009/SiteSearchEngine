package pet.skillbox.sitesearchengine.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import pet.skillbox.sitesearchengine.model.Link;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Integer> {
    @NotNull List<Link> findAll();
    Link getLinkByLink(String link);
}
