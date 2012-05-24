package cz.filmtit.core.concurrency

import cz.filmtit.share.{Language, Chunk}

/**
 * @author Joachim Daiber
 */
case class SearcherRequest(chunk: Chunk, language: Language)
