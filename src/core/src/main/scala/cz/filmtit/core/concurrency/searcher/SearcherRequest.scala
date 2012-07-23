package cz.filmtit.core.concurrency.searcher

import cz.filmtit.share.{Language, Chunk}

/**
 * @author Joachim Daiber
 */
case class SearcherRequest(chunk: Chunk, language: Language)
