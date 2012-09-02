package cz.filmtit.core.concurrency.tokenizer

import cz.filmtit.share.Chunk

/**
 * @author Joachim Daiber
 * @author Karel Bilek
 */
case class TokenizerRequestNormal(chunk:Chunk)
case class TokenizerRequestPos(chunk:Chunk)
