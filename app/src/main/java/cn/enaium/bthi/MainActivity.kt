package cn.enaium.bthi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.enaium.bthi.server.Server
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val logText = findViewById<TextView>(R.id.log)
        fun log(msg: String) {
            val calendar = Calendar.getInstance()
            logText.append(
                "\n[${calendar.get(Calendar.HOUR_OF_DAY)}" +
                        ":${calendar.get(Calendar.MINUTE)}" +
                        ":${calendar.get(Calendar.SECOND)}]$msg"
            )
            val scrollView = findViewById<ScrollView>(R.id.logScroll)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        val config = getSharedPreferences("config", MODE_PRIVATE)
        val configEdit = config.edit()
        val hostEditText = findViewById<EditText>(R.id.host)
        val portHostEditText = findViewById<EditText>(R.id.port)
        hostEditText.setText(config.getString("host", "127.0.0.1"))
        portHostEditText.setText(config.getInt("port", 25560).toString())

        findViewById<Button>(R.id.github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Enaium")))
        }

        findViewById<Button>(R.id.donate).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://donate.enaium.cn/")))
        }

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener {
            startButton.visibility = View.GONE
            hostEditText.isEnabled = false
            portHostEditText.isEnabled = false
            configEdit.putString("host", hostEditText.text.toString())
            configEdit.putInt("port", portHostEditText.text.toString().toInt())
            configEdit.apply()
            log("Host:${hostEditText.text} Port:${portHostEditText.text}")
            Server.start(object :
                Config(
                    hostEditText.text.toString(),
                    portHostEditText.text.toString().toInt()
                ) {
                override fun send(msg: String) {
                    log(msg)
                }
            })
        }

        findViewById<Button>(R.id.clearLog).setOnClickListener {
            logText.text = ""
        }
    }
}