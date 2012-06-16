package cz.filmtit.dataimport.alignment


object SubHelper {

   /**
   * Converts time to milliseconds
   * @param time time information as string
   * @return milliseconds
   */
    def timeToNumber(time:String):Long = {
      try {
        val Array(hour, minute, second, mili) = time.split("[,:.]");
        hour.replaceAll(" ","").toLong*3600*1000+
          minute.replaceAll(" ","").toLong*60*1000+
          second.replaceAll(" ","").toLong*1000+
          mili.replaceAll(" ","").toLong
      } catch {
        case e:Exception=> 0L
      }
    }

}
