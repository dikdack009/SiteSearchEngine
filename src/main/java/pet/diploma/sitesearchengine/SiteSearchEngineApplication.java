package pet.diploma.sitesearchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@SpringBootApplication
@EnableJpaRepositories
public class SiteSearchEngineApplication {
	public static void main(String[] args) {
		SpringApplication.run(SiteSearchEngineApplication.class, args);
	}
}

