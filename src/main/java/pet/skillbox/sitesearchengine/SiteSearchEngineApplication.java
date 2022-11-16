package pet.skillbox.sitesearchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SiteSearchEngineApplication {
	public static void main(String[] args) {
		SpringApplication.run(SiteSearchEngineApplication.class, args);
	}
}

