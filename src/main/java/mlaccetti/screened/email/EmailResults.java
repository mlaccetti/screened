package mlaccetti.screened.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Component
public class EmailResults {
  @Autowired
  private ApplicationTemp appTemp;

  @Autowired
  private JavaMailSender emailSender;

  private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

  public void send() throws Exception {
    final MimeMessage message = emailSender.createMimeMessage();

    final MimeMessageHelper email = new MimeMessageHelper(message, true);
    final String today = dateFormat.format(new Date());

    email.setFrom("michael@laccetti.com");
    // email.setTo("hmsscreening@gmail.com");
    email.setTo("michael+covid-result@laccetti.com");
    email.setBcc(new String[]{"analobo.me@gmail.com", "michael@laccetti.com"});
    email.setSubject("Isabella Laccetti Lobo - " + today);
    email.setText("Attached are Isabella's results for today.");

    final FileSystemResource resultFile = new FileSystemResource(new File(appTemp.getDir(), String.format("COVID-19 school_child care screening result - %s.pdf", today)));
    email.addAttachment(Objects.requireNonNull(resultFile.getFilename()), resultFile);

    emailSender.send(message);
  }
}
