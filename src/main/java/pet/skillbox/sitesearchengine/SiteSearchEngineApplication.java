package pet.skillbox.sitesearchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class SiteSearchEngineApplication {

	public static void main(String[] args) throws SQLException, IOException {
//		SpringApplication.run(SiteSearchEngineApplication.class, args);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("(1 - index; 0 - search) -> ");
		if (reader.readLine().equals("1")) {
			indexing();
		} else {
			search();
		}
		//TODO: убрать индексацию поисковиков, подумать над максимальным кол-вом ссылок
	}

	public static void indexing() {
		long mm = System.currentTimeMillis();
		try {
			CrawlingSystem crawlingSystem =  new CrawlingSystem(new Site(1, Status.INDEXING,
					LocalDateTime.now(), null, "https://www.playback.ru/", "PlayBack"));
			crawlingSystem.start();
		} catch (RuntimeException | SQLException | InterruptedException e ) {
			e.printStackTrace();
		}

		System.out.println("Закончили");
		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
	}

	public static void search() throws IOException, SQLException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Request -> ");
		String request = reader.readLine();
		System.out.println("OK");
		long mm = System.currentTimeMillis();
		new SearchSystem(request, "https://www.playback.ru/").request().forEach(System.out::println);
		System.out.println("Закончили");
		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
	}

	private static Connection connectPath(String path) {
		return Jsoup.connect(path)
				.userAgent("DuckSearchBot")
				.referrer("https://www.google.com")
				.ignoreContentType(true)
				.ignoreHttpErrors(true);
	}
}
