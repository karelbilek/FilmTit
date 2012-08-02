package cz.filmtit.userspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 2.8.12
 * Time: 12:58
 * class to generate file from db
 */
public class GenerateFile {

    public enum  FileType{
      SRT,
      SUB,
      TXT,
    }

    public enum GenerateLang{
        ORIG,
        TRANS
    }

    private static String LINE = "\n";
    private static String ARROW = "-->";
    private List<TimeResult> data ;

     public GenerateFile() {
         data = new ArrayList<TimeResult>();
     }



  public StringBuilder generateFile(int idDocument , FileType type, org.hibernate.Session session){
        org.hibernate.Session dbSession = session;
        List<USTranslationResult> TranslationResults = dbSession.createQuery("select d from USTraslationResult d where d.documentDatabaseId = :id_document")
                .setParameter("id_document",idDocument).list(); //UPDATE hibernate  for more constraints
      //  List<USTranslationResult> TranslationResults = dbSession.createQuery("select d from USTranslationResult d").list(); //UPDATE hibernate  for more constraints
        System.out.println("Generate files");
        TimeResult actualSub = null;

        for (USTranslationResult result : TranslationResults)
        {

         if (actualSub == null || !actualSub.inSameTime(result)){
             if (actualSub!=null) {
                 data.add(actualSub);
             };

             actualSub = new TimeResult(result);
         }
         else{
            System.out.println("Pridani do zaznamu");
            actualSub.addResult(result);
         }
        }

        StringBuilder content = generateText(type);
        return content;
    }
     private StringBuilder generateText(FileType type)
     {
         if (type == FileType.SRT)
         {
             return generateTextSRT();
         }
         return null;
     }

    private StringBuilder generateTextSRT(){
        StringBuilder tmp = new StringBuilder();
        int i = 1;
        for (TimeResult r : data)
        {
          tmp.append(i).append(LINE);
          tmp.append(r.getStartString()).append(ARROW).append(r.getEndString()).append(LINE);
          tmp.append(r.getText()).append(LINE).append(LINE);
          i++;
        }
        return tmp;
    }

  private  class TimeResult
  {

      String startString;
      String endString;
      StringBuilder text;

      public  TimeResult(USTranslationResult s){
            startString = s.getStartTime();
            endString = s.getEndTime();
            text  = new StringBuilder(s.getText());
      }

      public boolean inSameTime(USTranslationResult s){
         if (s == null) return false;
         return  ((startString.compareTo(s.getStartTime())==0) && (endString.compareTo( s.getEndTime())==0 ));
      }

      public void addResult(USTranslationResult s){
          text.append(s.getText());
      }

      public String getEndString() {
          return endString;
      }

      public String getStartString() {
          return startString;
      }

      public StringBuilder getText() {
          return text;
      }
  }
}
