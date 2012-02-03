package cz.filmtit.core

import model.Language
import model.names.NEType

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object Configuration {

  //Database:
  val dbConnector = "jdbc:postgresql://localhost/filmtit"
  val dbUser = "postgres"
  val dbPassword = "postgres"

  //Named entity recognition:

  val modelPath = "/filmtit/models/"
  val neRecognizers = Map(
    Language.en -> List(
      (NEType.Person,       modelPath + "en-ner-person.bin"),
      (NEType.Place,        modelPath + "en-ner-location.bin"),
      (NEType.Organization, modelPath + "en-ner-organization.bin")
    ),
    Language.cz -> List()
  )


}