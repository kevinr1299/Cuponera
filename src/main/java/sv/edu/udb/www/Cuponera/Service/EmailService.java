package sv.edu.udb.www.Cuponera.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service("EmailService")
public class EmailService {
	
	@Autowired
	public JavaMailSender mailSender;
	
	public void sendSimpleMessage(String to, String subject, String text) throws MessagingException {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		mailSender.send(message);
	}
	
	public void sendMimeMessage(String to, String subject, String text) throws MessagingException {
		MimeMessage mime = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mime,false,"utf-8");
		mime.setContent(text, "text/html");
		helper.setTo(to);
		helper.setSubject(subject);
		mailSender.send(mime);
	}
}
