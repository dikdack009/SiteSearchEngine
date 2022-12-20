package pet.skillbox.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pet.skillbox.sitesearchengine.model.response.Statistic;
import pet.skillbox.sitesearchengine.model.thread.StatisticThread;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EmailServiceImpl  {

    private final JavaMailSender emailSender;
    String text =
           "<h2><div style = \"display: inline-block\"> Индексация 1 сайта на сайте </div>\n" +
                   "   <div style = \"display: inline-block\">\n" +
                   "     <a href = \"http://localhost:3000/\"> SearchEngine.ru </a>\n" +
                   "    </div>\n" +
                   "   <div style = \"display: inline-block\"> завершена.</div>\n" +
                   "     </h2>\n" +
                   "     <h3>Теперь вы можете осуществить поиск! </h3><h3> P.S. Nice COCK bro)</h3>";

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendMessage(String to, int userId, Map<String, String> sites) throws UnsupportedEncodingException, MessagingException, ExecutionException, InterruptedException, JSONException, SQLException {
        // ...

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Multipart mp = new MimeMultipart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(createMessageText(sites, userId), "text/html; charset = utf-8");
        mp.addBodyPart(htmlPart);

        DataSource fdsOk = new FileDataSource(
                "C:\\SiteSearchEngine\\src\\main\\resources\\img\\indexInfoOk.png");


        MimeBodyPart picturePart = new MimeBodyPart();
        picturePart.setDataHandler(new DataHandler(fdsOk));
        System.out.println("КАКОЕ_ТО ИМЯ " + fdsOk.getName()    );
        picturePart.setHeader("Content-ID", fdsOk.getName());
        mp.addBodyPart(picturePart);
//        helper.setFrom("noreply@serchengine.ru");
//        helper.setTo("d.harke@yandex.ru");
        helper.setTo("dikdacksun@gmail.com");
//        helper.setTo(to);
        helper.setSubject("Индексация завершена");
        message.setContent(mp);
        message.setFrom(new InternetAddress("no_reply@example.com", "NoReply-SearchEngine"));
        Address address = new NewsAddress("", "noreply@serchengine.ru");
        message.setSender(address);

        emailSender.send(message);
        System.out.println("Отправили письмо");
        // ...
    }

    private String createMessageText(Map<String, String> sites, int userId) throws ExecutionException, InterruptedException, JSONException, SQLException {
        int numberSites = sites.size();
        String js = getStatistics(userId);
        System.out.println(js);
        JSONObject json = new JSONObject(js);
        JSONArray array = json.getJSONObject("statistics").getJSONArray("detailed");
        StringBuilder stat = new StringBuilder();
        for (String url : sites.keySet()) {
            String status = "";
            String file = "";
            for (int i = 0; i < array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                if (object.getString("url").equals(url)) {
                    if (object.getString("status").equals("INDEXED")) {
                        status = "Проиндексировано";
                        file = "indexInfoOk.png";
                    }
                    else if (object.getString("status").equals("FAILED")) {
                        status = "Ошибка";
                        file = "indexInfoError.png";
                    }
                }
            }
            String name = sites.get(url);

            stat.append("<div style=\"display: flex;\n").append("margin-top: 10px;\">\n")
                    .append("<span style=\"width: 40vw;\"><b>").append(name).append("&nbsp;&mdash;</b>&nbsp;")
                    .append(url).append("</span>\n").append("<div style=\"display: flex; align-items: center\">\n")
                    .append("<img alt=\"hui\" width=\"35\" height=\"35\" src=\"cid:").append(file).append("\">\n")
                    .append("<span style=\"margin-right: 3vw; margin-left:10px\">").append(status).append("</span>\n")
                    .append("</div>\n").append("</div>\n");
        }
        String siteInfo = numberSites + " веб-" + getNoun(numberSites, "ресурс", "ресурса", "ресурсов");

        return
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\"/>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
                "    <link rel=\"stylesheet\"/>\n" +
                "    <style>\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "\n" +
                "        A {\n" +
                "            color: black;\n" +
                "        }\n" +
                "\n" +
                "        A:visited {\n" +
                "            color: black; /* Цвет посещенных ссылок */\n" +
                "        }\n" +
                "\n" +
                "        A:active {\n" +
                "            color: black; /* Цвет активных ссылок */\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<h2 style=\"\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "            text-align: center;\">\n" +
                "    Проиндексировано&nbsp;" + siteInfo + "&nbsp;на сайте&nbsp;<a style=\"color: black;\" href=\"http://localhost:3000/\">SearchEngine.ru</a>.\n" +
                "</h2>\n" +
                "<div style=\"\n" +
                "            align-items: center;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "            margin-top: 5vh;\n" +
                "            height: auto;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            justify-content: space-around;\">\n" +
                stat +
                "</div>\n" +
                "<div style=\"margin-top: 5vh;\n" +
                "            margin-left: 3vw;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\">\n" +
                "    <div>\n" +
                "        Для более детальной информации посетите сайт.\n" +
                "    </div>\n" +
                "    <div>\n" +
                "        Если вы не хотите получать письменные уведомления, то отключите их на сайте в разделе <b>«Профиль»</b>.\n" +
                "    </div>\n" +
                "\n" +
                "    <div style=\"font-size: 13px; margin-top: 4vh\">\n" +
                "        © 2022 SearchEngine, National Research Nuclear University MEPhI (Moscow Engineering Physics Institute)\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }

    private String getNoun(int number, String one, String two, String five) {
        int n = Math.abs(number);
        n %= 100;
        if (n >= 5 && n <= 20) {
            return five;
        }
        n %= 10;
        if (n == 1) {
            return one;
        }
        if (n >= 2 && n <= 4) {
            return two;
        }
        return five;
    }

    private String getStatistics(int userId) throws ExecutionException, InterruptedException, SQLException {
        return new Statistic(false, userId).getStatistics().getDetailed().toString();
    }
}
