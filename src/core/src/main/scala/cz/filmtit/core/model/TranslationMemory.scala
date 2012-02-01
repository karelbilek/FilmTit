package cz.filmtit.core.model

import cz.filmtit.core.model.Language._

/**
 * @author Joachim Daiber
 *
 *
 *
 */

trait TranslationMemory {

  def initialize(pairs: Array[TranslationPair])

  def reindex()

  def nBest(chunk: Chunk, mediaSource: MediaSource, language: Language,
            n: Int = 10): List[ScoredTranslationPair]

  def firstBest(chunk: Chunk, mediaSource: MediaSource, language: Language):
    Option[ScoredTranslationPair]

  def addMediaSource(mediaSource: MediaSource): Long


}