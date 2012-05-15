package cz.filmtit.core

import cz.filmtit.core.Configuration
import scala.reflect.BeanProperty

//hack because I am lazy
object ConfigurationSingleton {
    @BeanProperty
    var conf: Configuration = null
}
