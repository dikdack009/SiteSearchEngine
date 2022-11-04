package pet.skillbox.sitesearchengine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;
import pet.skillbox.sitesearchengine.repositories.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;

@SpringBootApplication
@EnableJpaRepositories
public class SiteSearchEngineApplication {

	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		SpringApplication.run(SiteSearchEngineApplication.class, args);

//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//		System.out.print("(1 - index; 0 - search) -> ");
//		if (reader.readLine().equals("1")) {
//			indexing();
//		} else {
//			search();
//		}
	}
//	public static void indexing() throws SQLException {
//		long mm = System.currentTimeMillis();
//		CrawlingSystem crawlingSystem =  new CrawlingSystem(new Site(1, Status.INDEXING,
//				LocalDateTime.now(), null, "https://www.playback.ru/", "PlayBack"));
//		try {
//			crawlingSystem.start();
//			DBConnection.updateSite("https://www.playback.ru/", "INDEXED", null);
//		} catch (RuntimeException | SQLException | InterruptedException e ) {
//			DBConnection.updateSite("https://www.playback.ru/", "FAILED", crawlingSystem.getLastError());
//			e.printStackTrace();
//		}
//		System.out.println("Закончили");
//		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
//		System.out.println((double)Math.round(System.currentTimeMillis() - mm) / 60000 + " min.");
//	}

	public static void search() throws IOException, SQLException, InterruptedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Request -> ");
		String request = reader.readLine();
		System.out.println("OK");
		Logger rootLogger = LogManager.getLogger("search");
		rootLogger.info("Поиск - <" + request + ">");
		long mm = System.currentTimeMillis();
//		List<SearchResult> results = new SearchSystem(request, "https://www.playback.ru/").request();
//		rootLogger.info("Нашли " + results.size() + " стр. за " + (double)(System.currentTimeMillis() - mm) / 1000 + " сек.");
//		results.forEach(System.out::println);
//		System.out.println("Закончили");
//		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
		System.out.println((double)(System.currentTimeMillis() - mm) / 60000 + " min.");
	}

	private static Connection connectPath(String path) {
		return Jsoup.connect(path)
				.userAgent("DuckSearchBot")
				.referrer("https://www.google.com")
				.ignoreContentType(true)
				.ignoreHttpErrors(true);
	}
}
