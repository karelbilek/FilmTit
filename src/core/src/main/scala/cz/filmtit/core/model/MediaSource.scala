package cz.filmtit.core.model

import collection.mutable.HashSet

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class MediaSource(val title: String,  val year: Int, var genres: HashSet[String])