package pet.skillbox.sitesearchengine.services;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.UnsupportedEncodingException;

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

    public void sendMessage(String to, String subject, String ctext) throws MessagingException, UnsupportedEncodingException {
        // ...

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        Multipart mp = new MimeMultipart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(text, "text/html; charset = utf-8");
        mp.addBodyPart(htmlPart);

        helper.setFrom("noreply@serchengine.ru");
        helper.setTo("d.harke@yandex.ru");
//        helper.setTo("dikdacksun@gmail.com");
        helper.setSubject("Индексация завершена!");
        message.setContent(mp);
        message.setFrom(new InternetAddress("no_reply@example.com", "NoReply-SearchEngine"));
        Address address = new NewsAddress("", "noreply@serchengine.ru");
        message.setSender(address);

        emailSender.send(message);
        // ...
    }
}
