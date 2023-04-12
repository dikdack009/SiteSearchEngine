package pet.diploma.sitesearchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pet.diploma.sitesearchengine.model.response.DetailedSite;
import pet.diploma.sitesearchengine.model.response.Statistic;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.*;

@Component
public class EmailService {

    private final JavaMailSender emailSender;
    private boolean checkOk = false;
    private boolean checkError = false;
    private final static String PATH_PICTURE_OK = "C:\\pet\\SiteSearchEngine\\src\\main\\resources\\img\\indexInfoOk.png";
    private final static String PATH_PICTURE_ERROR = "C:\\pet\\SiteSearchEngine\\src\\main\\resources\\img\\indexInfoError.png";
    @Getter
    private final Map<String, Integer> verification = new HashMap<>();

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendMessage(String to, int userId, Map<String, String> sites) throws UnsupportedEncodingException, MessagingException, SQLException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Multipart mp = new MimeMultipart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        String text = createMessageText(sites, userId);
        htmlPart.setContent(text, "text/html; charset = utf-8");
        mp.addBodyPart(htmlPart);

        DataSource fdsOk = new FileDataSource(PATH_PICTURE_OK);
        DataSource fdsError = new FileDataSource(PATH_PICTURE_ERROR);


        MimeBodyPart picturePart = new MimeBodyPart();
        if (checkOk) {
            picturePart.setDataHandler(new DataHandler(fdsOk));
            picturePart.setHeader("Content-ID", fdsOk.getName());
            mp.addBodyPart(picturePart);
        }
        if (checkError) {
            picturePart = new MimeBodyPart();
            picturePart.setDataHandler(new DataHandler(fdsError));
            picturePart.setHeader("Content-ID", fdsError.getName());
            mp.addBodyPart(picturePart);
        }
        helper.setTo(to);
        helper.setSubject("Индексация завершена");
        message.setContent(mp);
        message.setFrom(new InternetAddress("no_reply@example.com", "NoReply-SearchEngine"));
        Address address = new NewsAddress("", "noreply@serchengine.ru");
        message.setSender(address);

        emailSender.send(message);
        checkOk = false;
        checkError = false;
        System.out.println("Отправили письмо");
    }

    private String createMessageText(Map<String, String> sites, int userId) throws SQLException {
        int numberSites = sites.size();
        List<DetailedSite> detailedSites = getStatistics(userId);
        StringBuilder stat = new StringBuilder();
        for (String url : sites.keySet()) {
            String status = "";
            String file = "";
            String error = "";
            for (DetailedSite d : detailedSites) {

                if (d.getUrl().equals(url)) {
                    if (d.getStatus().equals("INDEXED")) {
                        status = "Проиндексировано";
                        file = "indexInfoOk.png";
                        checkOk = true;
                    }
                    else if (d.getStatus().equals("FAILED")) {
                        status = "Ошибка";
                        file = "indexInfoError.png";
                        error = ": "+ d.getError();
                        checkError = true;
                    }
                }
            }
            String name = sites.get(url);

            stat.append("<div style=\"display: flex;\n").append("margin-top: 10px;\">\n")
                    .append("<span style=\"width: 25vw;\"><b>").append(name).append("&nbsp;&mdash;</b>&nbsp;")
                    .append(url).append("</span>\n").append("<div style=\"display: flex; align-items: center\">\n")
                    .append("<img alt=\"hui\" width=\"40\" height=\"35\" src=\"cid:").append(file).append("\">\n")
                    .append("<span style=\"margin-right: 3vw; margin-left:10px\"><b>").append(status).append("</b>")
                    .append(error).append("</span>\n").append("</div>\n").append("</div>\n");
        }
        String siteInfo = numberSites + " веб-" + getNoun(numberSites);
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

    private String getNoun(int number) {
        int n = Math.abs(number);
        n %= 100;
        if (n >= 5 && n <= 20) {
            return "ресурсов";
        }
        n %= 10;
        if (n == 1) {
            return "ресурс";
        }
        if (n >= 2 && n <= 4) {
            return "ресурса";
        }
        return "ресурсов";
    }

    private List<DetailedSite> getStatistics(int userId) throws SQLException {
        return new Statistic(false, userId).getStatistics().getDetailed();
    }

    public void sendCheckCode(String to) throws MessagingException, UnsupportedEncodingException {
        Random r = new Random();
        String number1 = String.valueOf(r.nextInt(9) + 1);
        String number2 = String.valueOf(r.nextInt(10));
        String number3 = String.valueOf(r.nextInt(10));
        String number4 = String.valueOf(r.nextInt(10));
        String number5 = String.valueOf(r.nextInt(10));
        String number6 = String.valueOf(r.nextInt(10));
        String number = number1 + number2 + number3 + number4 + number5 + number6;
        String text = "Добрый день!\n" +
                "Ваш проверочный код - " + number + ".\n\n" +
                "\n" +
                "Введите этот код, чтобы активировать свою учетную запись.\n" +
                "\n";
        verification.put(to, Integer.valueOf(number));
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Подтверждение аккаунта");
        Multipart mp = new MimeMultipart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(text, "text/html; charset = utf-8");
        mp.addBodyPart(htmlPart);
        message.setContent(mp);
        message.setFrom(new InternetAddress("no_reply@example.com", "NoReply-SearchEngine"));
        Address address = new NewsAddress("", "noreply@serchengine.ru");
        message.setSender(address);
        emailSender.send(message);
    }
}
