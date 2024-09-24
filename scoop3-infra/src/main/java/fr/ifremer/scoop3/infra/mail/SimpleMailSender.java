package fr.ifremer.scoop3.infra.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;

public class SimpleMailSender {

    private static final String MAILER_VERSION = "Java";
    private static final boolean DEBUG = true;

    /**
     * @param smtpServer
     *            SMTP server
     * @param mailFrom
     *            email Address of the sender
     * @param mailToArray
     *            Array of the recipients
     * @param mailSubject
     *            subject of the email
     * @param mailBody
     *            body of the email (HTML)
     * @return TRUE if there is no error when sending the email
     */
    public static boolean sendEmailSMTP(final String smtpServer, final String mailFrom, final String[] mailToArray,
	    final String mailSubject, final String mailBody) {
	boolean result = false;
	try {

	    final InternetAddress[] internetAddresses = new InternetAddress[mailToArray.length];
	    int mailToIndex = 0;
	    for (final String mailTo : mailToArray) {
		internetAddresses[mailToIndex] = new InternetAddress(mailTo);
		mailToIndex++;
	    }

	    /*
	     * Prepare mail
	     */
	    final Properties prop = System.getProperties();
	    prop.put("mail.smtp.host", smtpServer);

	    final Session session = Session.getDefaultInstance(prop, null);
	    final Message message = new MimeMessage(session);

	    message.setHeader("X-Mailer", MAILER_VERSION);
	    message.setSentDate(new Date());
	    session.setDebug(DEBUG);

	    /*
	     * Set sender
	     */
	    message.setFrom(new InternetAddress(mailFrom));

	    /*
	     * Add recipients
	     */
	    message.addRecipients(Message.RecipientType.TO, internetAddresses);
	    // message.addRecipients(Message.RecipientType.CC, copyDest);

	    /*
	     * Add subject and body
	     */
	    message.setSubject(mailSubject);
	    message.setContent(mailBody, "text/html");

	    /*
	     * Send message ...
	     */
	    Transport.send(message);
	    result = true;
	} catch (final MessagingException e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	return result;
    }

    public static void main(final String[] args) {
	final String smtpServer = FileConfig.getScoop3FileConfig().getString("bpc-controller.mail.smtp-server"); // "brehat.ifremer.fr";

	final String mailFrom = FileConfig.getScoop3FileConfig().getString("bpc-controller.mail.mail-from"); // "wenceslas.duros@ifremer.fr";
	final String[] mailToArray = (FileConfig.getScoop3FileConfig()
		.getString("bpc-controller.mail.mail-from") != null)
			? FileConfig.getScoop3FileConfig().getString("bpc-controller.mail.mail-from").split(",")
			: new String[] { mailFrom }; // new String[] { wenceslas.duros@ifremer.fr };

	final String mailSubject = FileConfig.getScoop3FileConfig().getString("bpc-controller.mail.message-subject"); // "Scoop3
														      // -
														      // Erreur
														      // inattendue";

	final String userName = System.getProperty("user.name");
	String hostName = "";
	try {
	    hostName = InetAddress.getLocalHost().getHostName();
	} catch (final UnknownHostException e1) {
	    SC3Logger.LOGGER.error(e1.getMessage(), e1);
	}

	String exceptionStr = "";
	try {
	    ((String) null).replace("", "");
	} catch (final Exception e1) {
	    SC3Logger.LOGGER.error(e1.getMessage(), e1);

	    final StringBuilder strBuilder = new StringBuilder();
	    strBuilder.append(e1.getClass().getCanonicalName());
	    for (final StackTraceElement stackTraceElt : e1.getStackTrace()) {
		strBuilder.append("<br />  ");
		strBuilder.append(stackTraceElt);
	    }
	    exceptionStr = strBuilder.toString();
	}

	final String filename = "...";

	/*
	 * <html>Bonjour, <br /><br />Ce message est envoy&eacute; automatiquement par Scoop 3 car une erreur inattendue
	 * a &eacute;t&eacute; lev&eacute;e.<br /><br />Nom de l''utilisateur : {0}<br />Nom de la machine : {1}<br
	 * />Fichier en cours de contr&ocirc;le : {2}<br /><br /><br />D&eacute;tail de l''erreur :<hr />{3}<br /><hr
	 * /><br /><br />Scoop3</html>
	 */
	final String mailBody = MessageFormat.format(
		FileConfig.getScoop3FileConfig().getString("bpc-controller.mail.message-body"), userName, hostName,
		filename, exceptionStr);

	SimpleMailSender.sendEmailSMTP(smtpServer, mailFrom, mailToArray, mailSubject, mailBody);

    }
}