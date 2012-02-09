package cz.filmit.userSpace;

import java._;
import java.util._;

/**
 * @author Jindřich Libovický
 *
 *
 *
 */

class Chunk (
  // ID in the UserSpace database
  val databaseId : Int,
  // a database ID of the document it belongs to
  val documentId : Int,
  // timing of the Chunk ... there will be special class for timing
  val timing : String,
  val text : String,
  val userTranslation : String,
  val translated : Boolean ) {

  // the list of matches for the Chunk
  //val matches : List<Match>
  
  class Timing( val start : Int,  val end : Int) {
    require(start >= 0 && end > 0, "Times cannot be negative.")

    def this(timing : String) = {
      this(0, 0)
      throw new UnsupportedOperationException()
    }

  }
}