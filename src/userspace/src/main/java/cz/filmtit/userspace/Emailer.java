package cz.filmtit.userspace;

/**
 *  A class used for sending email from the application. It is used as a confirmation of user registration
 *  and also when the user forgets his password and requires sending a new one via email.
 *
 * @author Pepa Čech
 */

import cz.filmtit.core.ConfigurationSingleton;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Properties;
public class Emailer {

    /**
     * The email configuration. It is load by the fetchConfig method from the general project configuaration.
     */
    private  Properties configuration = new Properties();

    /**
     * Email address of the email recipient.
     */
    private String email;
    /**
     * Subject of the email to be sent.
     */
    private String subject;
    /**
     * Actual text of the email.
     */
    private String message;
    /**
     * JBoss logger.
     */
     private USLogger logger = USLogger.getInstance();

    /**
     * Creates an empty object and sets the properties.
     */
    public Emailer() {
        fetchConfig();
    }

    /**
     * Collects the data necessary for sending the email.
     * @param email  Email address of the email recipient.
     * @param subject  Subject of the email
     * @param message The actual message.
     */
    public void collectData(String email, String subject, String message){
        this.email= email;
        this.message= message;
        this.subject = subject;
    }

    /**
     * Sends an email with parameters that has been collected before in the fields of the class.
     * @return Sign if the email has been successfully sent
     */
    public boolean send() {
        if (hasCollectedData()) {
            // send mail;
            javax.mail.Session session;
            logger.info("Emailer","Create session for mail login "+(String)configuration.getProperty("mail.filmtit.address") +" password"+ (String)configuration.getProperty("mail.filmtit.password"));
            session = javax.mail.Session.getDefaultInstance(configuration, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication((String)configuration.getProperty("mail.filmtit.address"), (String)configuration.getProperty("mail.filmtit.password"));
                }
            });
            session.setDebug(true);
            javax.mail.internet.MimeMessage message = new  javax.mail.internet.MimeMessage(session);
            try {
                message.addRecipient(Message.RecipientType.TO , new InternetAddress(this.email));
                message.setFrom(new InternetAddress((String)configuration.getProperty("mail.filmtit.address")));
                message.setSubject(this.subject);
                message.setText(this.message);

                Transport transportSSL = session.getTransport();
                transportSSL.connect((String)configuration.getProperty("mail.smtps.host"), Integer.parseInt(configuration.getProperty("mail.smtps.port")), (String)configuration.getProperty("mail.filmtit.address"), (String)configuration.getProperty("mail.filmtit.password")); // account used
                transportSSL.sendMessage(message, message.getAllRecipients());
                transportSSL.close();
           }
           catch (MessagingException ex) {
               logger.error("Emailer","An error while sending an email. " + ex);
           }
           return true;
        }
        logger.warning("Emailer","Emailer has not collected all data to be able to send an email.");
        return false;
    }
    /**     * Sends an email informing the recipient about his login and password in the application at the time
     * he registers to the application.
     * @param recipient An email address of the recipient
     * @param login Login of the user
     * @param pass Password of the user
     * @return Sign if the email has been successfully sent
     */
    public boolean sendRegistrationMail(String recipient , String login , String pass){
        // fills in the email items from the configuarion
        String messageTemp = (String)configuration.getProperty("mail.filmtit.registrationBody");
        String message = messageTemp.replace("%userlogin%",login).replace("%userpass%", pass);
        String subject = (String)configuration.getProperty("mail.filmtit.registrationSubject");
        // and sends the email (which also fills the data items of the object)
        return sendMail(recipient, subject, message);
    }

    /**
     * Sends an email informing the user that has forgotten his email about the url where he can change his password.
     * @param recipient  An email address of the recipient
     * @param login Login of the user
     * @param url The url of page the page the user supposed to change the password at.
     * @return Sign if the email has been successfully sent
     */
    public boolean sendForgottenPassMail(String recipient , String login , String url){
           String messageTemp = (String)configuration.getProperty("mail.filmtit.forgottenPassBody");
           String message = messageTemp.replace("%userlogin%",login).replace("%changeurl%",url);
           String subject = (String)configuration.getProperty("mail.filmtit.forgottenPassSubject");
           return sendMail(recipient,subject,message);
    }

    /**
     * A generic method for sending an email. First it saves the properties of the email and then calls the
     * actual send() method.
     * @param recipient An email address of the recipient
     * @param header The email subject
     * @param bodyMessage The email body.
     * @return Sign if the email has been successfully sent
     */
    public boolean sendMail(String recipient, String header, String bodyMessage) {
        collectData(recipient, header, bodyMessage);
        return send();
    }

    /**
     * Gets information if the object has collected all data items necessary to send an email.
     * @return Sign if the object has collected all data items necessary to send an email.
     */
    private boolean hasCollectedData() {
        if (this.email == null || this.message == null || this.subject == null) {
            return false;
        }
        return  !(this.email.isEmpty() || this.message.isEmpty() || this.subject.isEmpty());
    }


    private void fetchConfig() {
        java.io.InputStream input = null;

        // read configuration from configuration.xml
        HashMap<String,String> mailconfig =  ConfigurationSingleton.conf().configMail();
        for (String key : mailconfig.keySet() ) {
                configuration.setProperty(key, mailconfig.get(key));
        }

    }
}
