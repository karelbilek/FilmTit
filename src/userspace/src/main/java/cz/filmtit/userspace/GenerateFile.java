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



  public StringBuilder generateFile(long idDocument , GenerateLang  direction,org.hibernate.Session session){
        org.hibernate.Session dbSession = session;
        List<USTranslationResult> TranslationResults = dbSession.createQuery("select d from USTranslationResult d where d.documentDatabaseId = '"+idDocument+"'").list(); //UPDATE hibernate  for more constraints
      //  List<USTranslationResult> TranslationResults = dbSession.createQuery("select d from USTranslationResult d").list(); //UPDATE hibernate  for more constraints
        System.out.println("Generate files");
        TimeResult actualSub = null;
        System.out.println(TranslationResults.size() + " vysledku");
        for (USTranslationResult result : TranslationResults)
        {

         if (actualSub == null || !actualSub.inSameTime(result)){
             if (actualSub!=null) {
                 data.add(actualSub);
             };

             actualSub = new TimeResult(result,direction);
         }
         else{

            actualSub.addResult(result,direction);
         }
        }
        data.add(actualSub);
      System.out.println("Generate stringbuilder");
        StringBuilder content = generateTextSRT();
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
      StringBuilder text = null;
      GenerateLang type;
      int  lineLenght = 50;

      public  TimeResult(USTranslationResult s, GenerateLang type){
            startString = s.getStartTime();
            endString = s.getEndTime();
            this.type = type;
            text  = new StringBuilder(getResultText(s));
      }

      public  TimeResult(USTranslationResult s, GenerateLang type, int lineLenght){
          startString = s.getStartTime();
          endString = s.getEndTime();
          this.type = type;
          this.lineLenght = lineLenght;
          text  = new StringBuilder();
          addText(getResultText(s));
      }

      public boolean inSameTime(USTranslationResult s){
         if (s == null) return false;
         return  ((startString.compareTo(s.getStartTime())==0) && (endString.compareTo( s.getEndTime())==0 ));
      }

      public void addResult(USTranslationResult s, GenerateLang type){
          text.append(s.getText());
      }


      public String getEndString() {
          return endString;
      }

      public String getStartString() {
          return startString;
      }

      public StringBuilder getText() {
          StringBuilder returnData = new StringBuilder();
          if (text.length() > this.lineLenght)
          {
            int  lastPosition = 0;
            int  actual = 0;
             while ((actual = text.lastIndexOf(" ",lastPosition + this.lineLenght - 15))!=-1)
             {
               if (actual != lastPosition) {
                   returnData.append(LINE);
               }
               returnData.append(this.text.subSequence(lastPosition,actual));
               lastPosition = actual;
             }
              returnData.append(LINE);
              returnData.append(this.text.substring(lastPosition));
          }
          return text;
      }

      private String getResultText(USTranslationResult r){
          String tmp = null;
          if (this.type == GenerateLang.ORIG){
           tmp = r.getText();
          }
          else{
            tmp = r.getUserTranslation();
          }
          if (tmp == null) {return "";};
          return tmp;
      }
      private void addText(String t){

          this.text.append(t);
      }




  }
}
