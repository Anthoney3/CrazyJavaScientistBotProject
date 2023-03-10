package com.crazy.scientist.crazyjavascientist;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMail {

  public void createAndSendEmailToMyEmail(String userMessage) {
    // Recipient's email ID needs to be mentioned.
    String to = "AnthoneyChiocca.ac@gmail.com";

    // Sender's email ID needs to be mentioned
    String from = "AnthoneyChiocca.ac@gmail.com";

    // Assuming you are sending email from through gmails smtp
    String host = "smtp.gmail.com";

    // Get system properties
    Properties properties = System.getProperties();

    // Setup mail server
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", "465");
    properties.put("mail.smtp.ssl.enable", "true");
    properties.put("mail.smtp.auth", "true");

    // Get the Session object.// and pass username and password
    Session session = Session.getInstance(
      properties,
      new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(
            "AnthoneyChiocca.ac@gmail.com",
            "yefnvnypylrtgfxa"
          );
        }
      }
    );

    // Used to debug SMTP issues
    session.setDebug(true);

    try {
      // Create a default MimeMessage object.
      MimeMessage message = new MimeMessage(session);

      // Set From: header field of the header.
      message.setFrom(new InternetAddress(from));

      // Set To: header field of the header.
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

      // Set Subject: header field
      message.setSubject("Crazy Java Scientist Feedback");

      // Now set the actual message
      message.setText(userMessage);

      log.info("Sending Message to personal Email...");
      // Send message
      Transport.send(message);
      log.info("Message Sent to Email Successfully....");
    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
  }
}
