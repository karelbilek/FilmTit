package cz.filmtit.userspace;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 28.7.12
 * Time: 18:07
 *
 */



import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class Emailer {
     private static  String headRegistraion = "Registration on Filmtit";
     private static  String headForgottenPass = "Request for change password on Filmtit";
     private static  String sender = "filmtit@filmtit.cz";


    private static Properties configuration = new Properties();

     private String email;
     private String message;

 public Emailer(String email , String message)
 {

     setData(email,message);
 }
 public Emailer()
 {

 }

 public void setData(String email,String message)
 {
     this.email= email;
     this.message= message;
 }

 public boolean  send()
 {
     if (isFilled())
     {
         // send mail;
         return true;
     }
     return false;
 }

 public boolean send(String email, String message)
 {
     setData(email,message);
     return send();
 }

 public boolean sendMail(String recipier , String sender, String header , String bodyMessage)
 {
     javax.mail.Session session;
     session = javax.mail.Session.getDefaultInstance(configuration,null);
     javax.mail.internet.MimeMessage message = new  javax.mail.internet.MimeMessage(session);
     try {

         message.addRecipient(
                 Message.RecipientType.TO ,  new InternetAddress(recipier)
         );
         message.setSubject(header);
         message.setText( bodyMessage);
         Transport.send(message);
     } catch (MessagingException ex)
     {
         System.err.println("Cannot send mail " + ex);

     }
     return true;

 }
   private boolean isFilled()
   {
       return !(this.email.isEmpty() || this.message.isEmpty());
   }

   static {
       fetchConfig();
   }

    private static void fetchConfig() {
        java.io.InputStream input = null;

        try {
            // read configuration file
            input = new FileInputStream("/cz/filmtit/userspace/configmail.xml");
            // setting config properties
            configuration.loadFromXML(input);
            } catch (IOException e) {
              System.err.print("Can`t open configmail.xml with mail configuration");
            }

         finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.print("Can`t close file with mail configuration");
                }
            }
        }


    }

}
