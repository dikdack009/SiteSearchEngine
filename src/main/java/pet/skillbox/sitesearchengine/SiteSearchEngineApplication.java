package pet.skillbox.sitesearchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;
import pet.skillbox.sitesearchengine.repositories.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteSearchEngineApplication {

	public static void main(String[] args) throws SQLException, IOException {

		int[] mas = {1,7,3,6,5,6};
		System.out.println(pivotIndex(mas));

//		SpringApplication.run(SiteSearchEngineApplication.class, args);
//		long mm = System.currentTimeMillis();
//		try {
//			CrawlingSystem crawlingSystem =  new CrawlingSystem(new Site(1, Status.INDEXING,
//					LocalDateTime.now(), null, "https://www.playback.ru/", "playback"));
//			crawlingSystem.start();
//		} catch (RuntimeException exception) {
//			exception.printStackTrace();
//		}
//
//		System.out.println("Закончили");
//		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");

//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//		System.out.print("Request -> ");
//		String request = reader.readLine();
//		System.out.println("OK");
//		long mm = System.currentTimeMillis();
//		new SearchSystem(request, "https://www.playback.ru/").request().forEach(System.out::println);
//		System.out.println("Закончили");
//		System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");


//		String tagDeleteRegex = "<[^>]+>";
//		String document = " <body> \n" +
//				"  <link rel=\"StyleSheet\" href=\"/include_new/styles.css\" type=\"text/css\" media=\"all\">  \n" +
//				"  <table class=\"item_main_info_acc\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"> \n" +
//				"   <tbody>\n" +
//				"    <tr>\n" +
//				"     <td class=\"img_td\"> <img itemprop=\"image\" src=\"/img/product/big/1122550.jpg\" alt=\"Фото Накладка силиконовая с микрофиброй DF xiOriginal-26 для Xiaomi Redmi Note 11/11S Черная\" border=\"0\"> </td> \n" +
//				"     <td class=\"dop_img_td_new\" rowspan=\"2\"> \n" +
//				"      <div class=\"item_big_name\">\n" +
//				"       Накладка силиконовая с микрофиброй DF xiOriginal-26 для Xiaomi Redmi Note 11/11S Черная\n" +
//				"       <br><span class=\"articool\">(Артикул: 1122550)</span> </body>" +
//				"</body>";
//		String regex = "<body([\\S\\s]*)>(\\s*)([\\S\\s]*)</body>";
//		Matcher m = Pattern.compile(regex).matcher(document);
//		String tagContent = null;
//		while(m.find()) {
//			tagContent = document.substring(m.start(), m.end() - "</body>".length());
//		}
//		assert tagContent != null;
//		System.out.println(tagContent);
//		tagContent = tagContent.replaceAll(tagDeleteRegex, "");
//		System.out.println("\n\n" + tagContent.trim());

//		m = Pattern.compile("(?:<title>)(?:\\s*)([\\S\\s]*)(?:</title>)").matcher(document);
//		String title = null;
//		while(m.find()) {
//			title = document.substring(m.start() + "<title>".length(), m.end() - "</title>".length());
//		}
//		System.out.println(title);
	}
}
