package cz.filmtit.core

import scala.reflect.BeanProperty

//hack because I am lazy
object ConfigurationSingleton {
    @BeanProperty
    var conf: cz.filmtit.core.Configuration = null
}
