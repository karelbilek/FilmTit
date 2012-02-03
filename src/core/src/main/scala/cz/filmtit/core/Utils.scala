package cz.filmtit.core

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object Utils {

  /** map method for Tuples */
  implicit def t2mapper[X, A <: X, B <: X](t: (A,B)) = new {
    def map[R](f: X => R) = (f(t._1), f(t._2))
  }

}
