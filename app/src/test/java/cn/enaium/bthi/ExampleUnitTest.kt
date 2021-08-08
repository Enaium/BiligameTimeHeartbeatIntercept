package cn.enaium.bthi

import cn.enaium.bthi.server.Server
import org.json.JSONArray
import org.junit.Test

import org.junit.Assert.*
import java.net.InetAddress
import java.net.URL

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
fun main(array: Array<String>) {
    Server.start(object : Config(InetAddress.getLocalHost().hostAddress, 25560) {
        override fun send(msg: String, logType: LogType) {
            println(msg)
        }
    })
}