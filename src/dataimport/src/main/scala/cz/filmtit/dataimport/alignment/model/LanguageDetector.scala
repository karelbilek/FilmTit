package cz.filmtit.dataimport.alignment.model

import _root_.java.io.File
import cz.filmtit.core.Configuration
import com.cybozu.labs.langdetect.Detector
import com.cybozu.labs.langdetect.DetectorFactory
//import com.cybozu.labs.langdetect.Language
import cz.filmtit.share.Language

object LanguageDetector {

   val spaceRegex="""^\s*$""".r
    
    //not very clean I think :/
    var loaded:Boolean = false
   
   def init(conf:Configuration):Unit = {
        if (!loaded) {
            val directory = new File("dataimport/src/main/resources/language_profiles")
            DetectorFactory.loadProfile(directory.getCanonicalPath)
            loaded=true
        }
        
    }
    
    def detect(text:String, c:Configuration):Option[cz.filmtit.share.Language] = {
        if (spaceRegex.findFirstIn(text).isDefined) {
            None;
        } else {
            try {
                init(c)
                
                val detector = DetectorFactory.create()
                detector.append(text)
                
                import scala.collection.JavaConverters._   
                
                val probabilities = detector.getProbabilities.asScala
                val language = probabilities.find{l=>l.lang == "cs" || l.lang=="en"}.map{recog=>Language.fromCode(recog.lang)}

                language
            } catch {
                case e:com.cybozu.labs.langdetect.LangDetectException => println("Problem with string "+text);
                None
            }
        }
    }
}
