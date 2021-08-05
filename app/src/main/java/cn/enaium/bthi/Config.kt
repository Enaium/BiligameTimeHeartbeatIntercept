package cn.enaium.bthi

/**
 * @author Enaium
 */
open class Config(val host: String, val port: Int) {
    open fun send(msg: String) {

    }
}