package cz.filmtit.core

import scala.reflect.BeanProperty

/**
 * Static object that holds the single configuration instance
 * for the currently running server.
 *
 * @author Karel Bilek
 */
object ConfigurationSingleton {
    @BeanProperty
    var conf: cz.filmtit.core.Configuration = null
}
