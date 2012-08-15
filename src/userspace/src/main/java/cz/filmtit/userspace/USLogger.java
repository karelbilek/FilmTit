package cz.filmtit.userspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * A singleton class ensuring logging of the client errors on the server.
 * @author Pepa Čech
 */
public class USLogger {

    private static USLogger logger = null;
    private LevelLogEnum logConsoleLevel;
    private LevelLogEnum logDbLevel;
    /**
     * Instance of the singleton class for managing database sessions.
     */
    private static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    private Log jLogger =  null;
    private LevelLogEnum base = LevelLogEnum.Notice;

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
        // Todo if base level of

        //LevelLogEnum.convertToInt(level) >=  LevelLogEnum.convertToInt(base);
        StringBuilder messageCreator = new StringBuilder();
        Date datum = new Date();
        messageCreator.append(datum.toString()).append(" ").append(level.toString()).append("\n").append("Context: ").append(context).append("\n");
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

    public void debug(String context, String message){
        log(LevelLogEnum.DebugNotice,context,message);
    }

    public void info(String context, String message){
        log(LevelLogEnum.Notice,context,message);
    }

    public void warning(String context, String message){
        log(LevelLogEnum.Warning,context,message);
    }

    public void error(String context, String message){
        log(LevelLogEnum.Error,context,message);
    }

    /**
     * Enum
     */
    public enum LevelLogEnum{
        Unknown,
        DebugNotice,
        Notice,
        Warning,
        Error;

        /**
         * Converts the log level to integer.
         * @param logLevel A log level
         * @return Integer representing the log level
         */
        public static int convertToInt(LevelLogEnum logLevel) {
              switch (logLevel) {
                  case DebugNotice : return 0;
                  case Notice: return 1;
                  case Warning: return 2;
                  case Error: return 3;
                  default:
                      return -1;
              }
        }

        /**
         * Converts an integer to a log level.
         * @param logLevelInt  Log level as an integer
         * @return Log level corresponding the given interger
         */
        public static LevelLogEnum convertTo(int logLevelInt){
            switch (logLevelInt){
                case 0 : return DebugNotice;
                case 1 : return Notice;
                case 2 : return Warning;
                case 3 : return Error;
                default:  return  Unknown;
            }
        }

        /**
         * Converts the log level to string.
         * @return
         */
        @Override
         public String toString() {
                switch (this) {
                    case DebugNotice : return "Debug: ";
                    case Notice: return "Notice: ";
                    case Warning: return "Warning: ";
                    case Error: return "Error: ";
                    default:
                        return  "No level: " ;
                }

            }
        }

}
