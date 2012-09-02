package cz.filmtit.userspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cz.filmtit.share.LevelLogEnum;

import java.util.Date;

/**
 * A singleton class ensuring logging of the client errors on the server.
 * @author Pepa Čech
 */
public class USLogger {

    /**
     * Instacne of USLogger
     */
    private static USLogger logger = null;
    /**
     *  Level log message for console
     */
    private LevelLogEnum logConsoleLevel;
    /**
     * Level log message for db
     */
    private LevelLogEnum logDbLevel;
    /**
     * Instance of the singleton class for managing database sessions.
     */
    private static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    /**
     *   Apache logger for console
     */
    private Log jLogger =  null;


    private USLogger(){
        jLogger = LogFactory.getLog("Userspace/Gui");
        logConsoleLevel = LevelLogEnum.DebugNotice;
        logDbLevel = LevelLogEnum.DebugNotice;
    }

    public synchronized static USLogger getInstance(){
        if (logger == null){
            logger = new USLogger();
        }
        return logger;
    }



    /**
     * A general method that logs and event.
     * @param level Log level (debug, warning ...)
     * @param context Context of the event
     * @param logMessage Detailed description fo the event
     */
    public void log(LevelLogEnum level , String context , String logMessage){
    	log(level, context, logMessage, null);
    }
    
    /**
     * A general method that logs and event.
     * @param level Log level (debug, warning ...)
     * @param context Context of the event
     * @param logMessage Detailed description fo the event
     * @param user The user in whose session the log was issued, or null
     */
    public void log(LevelLogEnum level , String context , String logMessage, USUser user){
        // Todo if base level of

        //LevelLogEnum.convertToInt(level) >=  LevelLogEnum.convertToInt(base);
        Date datum = new Date();
        
        StringBuilder messageCreator = new StringBuilder();
        
        messageCreator.append(datum);
        messageCreator.append(' ');
        messageCreator.append(level);
        messageCreator.append('\n');
        
        messageCreator.append("Context: ");
        messageCreator.append(context);
        messageCreator.append('\n');
        
        if (user != null) {
            messageCreator.append("User: ");
            messageCreator.append(user.getUserName());
            messageCreator.append('\n');
        }
        
        messageCreator.append(logMessage);
        
        if (LevelLogEnum.convertToInt(level) >=  LevelLogEnum.convertToInt(logConsoleLevel)) {
            switch (level) {
                case DebugNotice: jLogger.debug(messageCreator.toString());
                     break;
                case Notice: jLogger.info(messageCreator.toString());
                    break;
                case Warning : jLogger.warn(messageCreator.toString());
                    break;
                case Error:  jLogger.error(messageCreator.toString());
                    break;
                default:
                    jLogger.warn("Unknown log message");
            }
        }

        if (LevelLogEnum.convertToInt(level) >=  LevelLogEnum.convertToInt(logDbLevel)) {
            org.hibernate.Session dbSession  = usHibernateUtil.getSessionWithActiveTransaction();
            LogMessage message;
            message = new LogMessage(LevelLogEnum.convertToInt(level),context,logMessage,datum);
            message.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
        }
    }

    /**
     * Log message with debug level
     * @param context base of log
     * @param message whole message to log
     */
    public void debug(String context, String message){
        log(LevelLogEnum.DebugNotice,context,message);
    }

    /**
     * Log message with info level
     * @param context base of log
     * @param message whole message to log
     */
    public void info(String context, String message){
        log(LevelLogEnum.Notice,context,message);
    }

    /**
     * Log message with warning level
     * @param context base of log
     * @param message whole message to log
     */
    public void warning(String context, String message){
        log(LevelLogEnum.Warning,context,message);
    }

    /**
     * Log message with error level
     * @param context base of log
     * @param message whole message to log
     */
    public void error(String context, String message){
        log(LevelLogEnum.Error,context,message);
    }

}
