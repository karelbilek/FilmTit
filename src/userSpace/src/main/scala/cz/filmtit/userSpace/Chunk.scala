package cz.filmit.userSpace;

import java._;
import java.util._;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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
  val timeStart : String,
  val timeEnd : String,
  val text : String,
  val userTranslation : String,
  val translated : Boolean ) {

  // requires(... time to match some patterns ...)

  // the list of matches for the Chunk
  val matches : List[Match]

  // all variables will have getters and setters
  // ??? does that mean it should be var instead of val?
  // in setter of databaseId loading of matches will be hidden
}