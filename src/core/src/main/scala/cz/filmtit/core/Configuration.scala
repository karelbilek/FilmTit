package cz.filmtit.core

import cz.filmtit.core.model.annotation.Name
import cz.filmtit.core.model.Language


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
      (Name.Person,       modelPath + "en-ner-person.bin"),
      (Name.Place,        modelPath + "en-ner-location.bin"),
      (Name.Organization, modelPath + "en-ner-organization.bin")
    ),
    Language.cz -> List(

     )
  )


}