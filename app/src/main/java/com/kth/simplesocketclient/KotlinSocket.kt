package com.kth.simplesocketclient

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.kth.simplesocketclient.databinding.ActivityMainBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets

class KotlinSocket : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var activityMainBinding: ActivityMainBinding? = null
    private var IP: String? = null
    private var PORT: Int = 0
    private var handler: Handler? = null
    private var gson: Gson? = null
    private var parser: JsonParser? = null
    private lateinit var socket: Socket

    //TODO 20바이트 이상일 때, 유실되는 나머지 바이트를 복구하는 법.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        IP = resources.getString(R.string.ip)
        val tmp = resources.getString(R.string.port)
        PORT = Integer.parseInt(tmp)

        gson = GsonBuilder()
                .setLenient()
                .create()
        parser = JsonParser()
        handler = Handler()

        activityMainBinding!!.connectSocket.setOnClickListener { v ->
            val socketThread = SocketThread()
            socketThread.start()
        }

        activityMainBinding!!.send.setOnClickListener {
            val clientUIThread = ClientUIThread(socket)
            clientUIThread.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityMainBinding!!.output.text = "없음"
    }

    internal inner class SocketThread : Thread() { // 연결부

        override fun run() {
            try {
                socket = Socket(IP, PORT)

                val messageByte = ByteArray(20)
                val inputStream = DataInputStream(socket!!.getInputStream())
                inputStream.read(messageByte)

                val str = String(messageByte, StandardCharsets.UTF_8)
                Log.d("$TAG SocketThread", str.trim { it <= ' ' })
            } catch (e: IOException) {
                Log.e(TAG, e.localizedMessage)
            }

        }
    }

    internal inner class ClientUIThread// 전송부

    (socket: Socket?) : Thread() {

        override fun run() {

            try {
                val outputStream = DataOutputStream(socket!!.getOutputStream())

                //                Date date = new Date();
                val dateString = "{\"REQ\":\"WFSendDeviceInfo\",\"WF_SERIAL\":\"12345678\"}"

                //                byte b[] = date.toString().getBytes();
                val b2 = dateString.toByteArray()

                outputStream.write(b2)
                outputStream.flush()
                Log.d("$TAG ClientUIThread", "서버로 보냄.")

                val messageByte = ByteArray(50)
                val inputStream = socket!!.getInputStream()
                inputStream.read(messageByte)

                val str = String(messageByte, StandardCharsets.UTF_8).trim { it <= ' ' } // 바이트배열의 공백제거
                Log.d("$TAG ClientUIThread", str)

                val tmp = gson!!.fromJson(str, BaseModel::class.java)
                //                Log.d(TAG + " ClientUIThread", tmp.getAsString());

                Log.d("$TAG 0ClientUIThread", "받은 데이터 : " + tmp.wF_SER_NUM)

                //                for (byte tmp : message){
                //                    Log.d("ClientUIThread", "받은 데이터 : " + tmp);
                //                }

                handler!!.post { activityMainBinding!!.output.text = "받은 데이터 : " + String(messageByte) }
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }

        }
    }
}