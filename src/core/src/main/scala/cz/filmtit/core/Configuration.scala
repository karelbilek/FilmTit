package cz.filmtit.core

import cz.filmtit.core.model.Language
import model.annotation.{ChunkAnnotation, Name}
import java.io.File


/**
 * Configuration file for the external files and databases required by the TM.
 *
 * @author Joachim Daiber
 */

object Configuration {

  //Database:
  val dbConnector = "jdbc:postgresql://localhost/filmtit"
  val dbUser = "postgres"
  val dbPassword = "postgres"

  //Named entity recognition:
  val modelPath = "/filmtit/models/"
  val neRecognizers: Map[Language, List[Pair[ChunkAnnotation, String]]] = Map(
    Language.en -> List(
      (Name.Person,       modelPath+"en-ner-person.bin"),
      (Name.Place,        modelPath+"en-ner-location.bin"),
      (Name.Organization, modelPath+"en-ner-organization.bin")
    ),
    Language.cs -> List(

    )
  )

  val heldoutSize = 0.02 //percentage of all data
  val heldoutFile = new File("/filmtit/heldout.csv")

}