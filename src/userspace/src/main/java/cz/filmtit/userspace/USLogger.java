package cz.filmtit.userspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 6.8.12
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class USLogger {

     private static USLogger  logger = null;
     private LevelLogEnum logConsoleLevel;
     private LevelLogEnum logDbLevel;
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


    public void log(LevelLogEnum level , String context , String logMessage){
       // Todo if base level of

       //LevelLogEnum.contvertToInt(level) >=  LevelLogEnum.contvertToInt(base);
        StringBuilder messageCreator = new StringBuilder();
        Date datum = new Date();
        messageCreator.append(datum.toString()).append(" ").append(level.toString()).append("\n").append("Context: ").append(context).append("\n");
        messageCreator.append(logMessage);
        if (LevelLogEnum.contvertToInt(level) >=  LevelLogEnum.contvertToInt(logConsoleLevel))
        {
            switch (level)
            {
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

        if (LevelLogEnum.contvertToInt(level) >=  LevelLogEnum.contvertToInt(logDbLevel))
        {
            org.hibernate.Session dbSession  = usHibernateUtil.getSessionWithActiveTransaction();
            LogMessage message;
            message = new LogMessage(LevelLogEnum.contvertToInt(level),context,logMessage,datum);
            message.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
        }
    }

    public void  debug(String context, String message){
        log(LevelLogEnum.DebugNotice,context,message);
    }

    public void  info(String context, String message){
              log(LevelLogEnum.Notice,context,message);
    }

    public void warning(String context,String message){
        log(LevelLogEnum.Warning,context,message);
    }

    public void error(String context , String message){
        log(LevelLogEnum.Error,context,message);
    }

    public enum LevelLogEnum{
        Unknown,
        DebugNotice,
        Notice,
        Warning,
        Error;

        public static int  contvertToInt(LevelLogEnum l){
              switch (l) {
                  case DebugNotice : return 0;
                  case Notice: return 1;
                  case Warning: return 2;
                  case Error: return 3;
                  default:
                      return -1;
              }

        }

        public static LevelLogEnum convertTo(int l){
            switch (l){
                case 0 : return DebugNotice;
                case 1 : return Notice;
                case 2 : return Warning;
                case 3 : return Error;
                default:  return  Unknown;
            }
        }

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
