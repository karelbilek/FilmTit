package cz.filmtit.core.concurrency.searcher

import cz.filmtit.share.{Language, Chunk}

/**
 * A request to the translation memory, the request is for a Chunk
 * in a particular language.
 *
 * @author Joachim Daiber
 * @author Karel Bilek
 */
case class SearcherRequest(chunk: Chunk, language: Language)
