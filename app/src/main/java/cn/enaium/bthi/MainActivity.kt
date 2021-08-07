package cn.enaium.bthi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.enaium.bthi.server.Server
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())

        val logText = findViewById<TextView>(R.id.log)
        fun log(msg: String) {
            logText.append(
                "\n[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}]$msg"
            )

            val scrollView = findViewById<ScrollView>(R.id.logScroll)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        fun openUrl(url: String) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        val versionTextView = findViewById<TextView>(R.id.version)
        versionTextView.setTextColor(Color.BLUE)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionTextView.append("${versionName}点击检测是否有新版本")

        versionTextView.setOnClickListener {
            try {
                val url = URL("https://api.github.com/repos/Enaium/BiligameTimeHeartbeatIntercept/releases")
                val json = String(url.openStream().readBytes())
                val jsonArray = JSONArray(json)
                val currentVersion = JSONObject(jsonArray[0].toString()).getString("tag_name")

                if (!versionName.equals(currentVersion)) {
                    versionTextView.text = "最版本:${currentVersion}点击下载"
                    versionTextView.setTextColor(Color.RED)
                    versionTextView.setOnClickListener {
                        openUrl("https://github.com/Enaium/BiligameTimeHeartbeatIntercept/releases")
                    }
                }
            } catch (e: Throwable) {

            }
        }

        val config = getSharedPreferences("config", MODE_PRIVATE)
        val configEdit = config.edit()
        val hostEditText = findViewById<EditText>(R.id.host)
        val portHostEditText = findViewById<EditText>(R.id.port)
        hostEditText.setText(config.getString("host", "127.0.0.1"))
        portHostEditText.setText(config.getInt("port", 25560).toString())

        findViewById<Button>(R.id.github).setOnClickListener {
            openUrl("https://github.com/Enaium")
        }

        findViewById<Button>(R.id.donate).setOnClickListener {
            openUrl("https://donate.enaium.cn/")
        }

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener {
            startButton.visibility = View.GONE
            hostEditText.visibility = View.GONE
            portHostEditText.visibility = View.GONE
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