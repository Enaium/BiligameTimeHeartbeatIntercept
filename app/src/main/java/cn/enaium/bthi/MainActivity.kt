package cn.enaium.bthi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
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
        fun log(msg: String, log: LogType = LogType.INFO) {
            Thread {
                runOnUiThread {
                    val time = "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}]"

                    val type = when (log) {
                        LogType.WARNING -> "[WARNING]"
                        LogType.ERROR -> "[ERROR]"
                        else -> "[INFO]"
                    }

                    val spannableString = SpannableString("$time$type$msg\n")
                    spannableString.setSpan(
                        ForegroundColorSpan(
                            when (log) {
                                LogType.WARNING -> Color.YELLOW
                                LogType.ERROR -> Color.RED
                                else -> Color.GREEN
                            }
                        ), time.length, time.length + type.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )


                    logText.append(spannableString)

                    val scrollView = findViewById<ScrollView>(R.id.logScroll)
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }.start()
        }

        fun openUrl(url: String) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }


        val versionTextView = findViewById<TextView>(R.id.version)
        versionTextView.setTextColor(Color.BLUE)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionTextView.append(versionName)

        versionTextView.setOnClickListener {
            log("正在检测...")
            Thread {
                try {
                    val url = URL("https://api.github.com/repos/Enaium/BiligameTimeHeartbeatIntercept/releases")
                    val json = String(url.openStream().readBytes())
                    val jsonArray = JSONArray(json)
                    val currentVersion = JSONObject(jsonArray[0].toString()).getString("tag_name")

                    if (!versionName.equals(currentVersion)) {
                        log("发现新版本:${currentVersion}", LogType.WARNING)
                        runOnUiThread {
                            versionTextView.text = "最版本:${currentVersion}点击下载"
                            versionTextView.setTextColor(Color.RED)
                            versionTextView.setOnClickListener {
                                openUrl("https://github.com/Enaium/BiligameTimeHeartbeatIntercept/releases")
                            }
                        }
                    } else {
                        log("是最新版本")
                    }
                } catch (t: Throwable) {
                    log("检测失败", LogType.ERROR)
                    log(t.message.toString(), LogType.ERROR)
                }
            }.start()
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
            try {
                if (!hostEditText.text.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) {
                    log("请输入有效Host", LogType.ERROR)
                    return@setOnClickListener
                }
                configEdit.putString("host", hostEditText.text.toString())
                configEdit.putInt("port", portHostEditText.text.toString().toShort().toInt())//先检测是否为short再转为int
                configEdit.apply()
                log("等待启动中...")
                startButton.visibility = View.GONE
                hostEditText.visibility = View.GONE
                portHostEditText.visibility = View.GONE
                Server.start(object :
                    Config(
                        hostEditText.text.toString(),
                        portHostEditText.text.toString().toInt()
                    ) {
                    override fun send(msg: String, logType: LogType) {
                        log(msg, logType)
                    }
                })
            } catch (t: Throwable) {
                log(t.message.toString(), LogType.ERROR)
            }
        }

        findViewById<Button>(R.id.clearLog).setOnClickListener {
            logText.text = ""
        }
    }
}