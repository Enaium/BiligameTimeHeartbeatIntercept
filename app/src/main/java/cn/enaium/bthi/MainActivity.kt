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
import java.net.InetAddress


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Enaium")))
        }

        findViewById<Button>(R.id.donate).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://donate.enaium.cn/")))
        }


        val logText = findViewById<TextView>(R.id.log)
        fun log(msg: String) {
            logText.append("$msg\n")
            val scrollView = findViewById<ScrollView>(R.id.logScroll)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener {
            log("开始了!")
            startButton.visibility = View.GONE
            Server.start(object :
                Config(
                    findViewById<EditText>(R.id.host).text.toString(),
                    findViewById<EditText>(R.id.port).text.toString().toInt()
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