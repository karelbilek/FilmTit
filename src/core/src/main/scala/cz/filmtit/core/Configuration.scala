package cz.filmtit.core

import cz.filmtit.core.model.Language
import model.annotation.{ChunkAnnotation, Name}
import java.io.File
import scala.xml._

/**
 * Configuration file for the external files and databases required by the TM.
 *
 * @author Joachim Daiber
 */

object Configuration {

  private val XMLFile = XML.loadFile("configuration.xml")

  //Database:
  private val dbXML = XMLFile \ "database"
  val dbConnector: String = (dbXML \ "connector").text
  val dbUser: String =  (dbXML \ "user").text
  val dbPassword: String = (dbXML \ "password").text

  //Named entity recognition:
  val modelPath: String = (XMLFile \ "modelPath").text
  val neRecognizers: Map[Language, List[Pair[ChunkAnnotation, String]]] = Map(
    Language.en -> List(
      (Name.Person,       modelPath+"en-ner-person.bin"),
      (Name.Place,        modelPath+"en-ner-location.bin"),
      (Name.Organization, modelPath+"en-ner-organization.bin")
    ),
    Language.cs -> List(

    )
  )

  private val heldoutXML = XMLFile \ "heldout"

  val heldoutSize = (heldoutXML \ "size").text.toDouble //percentage of all data
  val heldoutFile = new File((heldoutXML \ "path").text)

}
