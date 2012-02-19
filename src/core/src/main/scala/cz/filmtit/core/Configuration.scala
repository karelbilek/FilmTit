package cz.filmtit.core

import cz.filmtit.core.model.Language
import model.annotation.{ChunkAnnotation, Name}


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


}