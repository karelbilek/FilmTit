/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.dataimport.alignment.io

import cz.filmtit.share.parsing.UnprocessedChunk

import cz.filmtit.share.TimedChunk
import cz.filmtit.core.Configuration

import java.io._

/**
 * Helper object, that helps in writing pairs of sourceSentencesToFind to a given printwriter.
 * It is the "final" writer to aligned corpus.
 */
object AlignedCorpusWriter {


  /**
   * A helper method for cleaning all |, - and < and > from subtitles
   * @param what string that I want to clean
   * @return cleaned string
   */
  def quickClean(what: String): String = {
    return what.replaceAll("(^|\\|)\\s*-+\\s*", "").replaceAll("\\s*\\|\\s*", " ").replaceAll("<[^>]*>", "")
  }

  /**
   * Writes two chunks to given PrintWriter
   * @param pw printWriter to print to
   * @param chunk1  first chunk
   * @param chunk2  second chunk
   */
  def write(pw: java.io.PrintWriter, chunk1: UnprocessedChunk, chunk2: UnprocessedChunk) {
    //in the case of different sentence split, I still want to have the chunks there

    write(pw,
      quickClean(chunk1.getText),
      quickClean(chunk2.getText));
  }

  /**
   * Writes two chunks to given PrintWriter
   * @param pw printWriter to print to
   * @param chunk1  first chunk
   * @param chunk2  second chunk
   */
  def write(pw: java.io.PrintWriter, chunk1: TimedChunk, chunk2: TimedChunk) {
    write(pw, chunk1.getSurfaceForm, chunk2.getSurfaceForm)
  }


  /**
   * Writes two chunks to given file
   * @param pw printWriter to print to
   * @param chunk1  first chunk
   * @param chunk2  second chunk
   */
  def write(pw: java.io.PrintWriter, chunk1: String, chunk2: String) {

    pw.println(chunk1.replaceAll("\t", " ") + "\t" + chunk2.replaceAll("\t", " "))
  }
}
