package com.kth.simplesocketclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kth.simplesocketclient.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding activityMainBinding;
    private static String IP;
    private static int PORT;
    private Handler handler;
    private Gson gson;
    private JsonParser parser;
    private Socket socket;

    //TODO 20바이트 이상일 때, 유실되는 나머지 바이트를 복구하는 법.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        IP = getResources().getString(R.string.ip);
        String tmp = getResources().getString(R.string.port);
        PORT = Integer.parseInt(tmp);

        gson = new GsonBuilder()
                .setLenient()
                .create();
        parser = new JsonParser();
        handler = new Handler();

        activityMainBinding.connectSocket.setOnClickListener(v -> {
            SocketThread socketThread = new SocketThread();
            socketThread.start();
        });

        activityMainBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientUIThread clientUIThread = new ClientUIThread(socket);
                clientUIThread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityMainBinding.output.setText("없음");
    }

    class SocketThread extends Thread { // 연결부

        public void run() {
            try {
                socket = new Socket(IP, PORT);

                final byte[] messageByte = new byte[20];
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                inputStream.read(messageByte);

                String str = new String(messageByte, StandardCharsets.UTF_8);
                Log.d(TAG + " SocketThread", str.trim());
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    class ClientUIThread extends Thread { // 전송부

        public ClientUIThread(Socket socket) {
        }

        public void run() {

            try {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

//                Date date = new Date();
                String dateString = "{\"REQ\":\"WFSendDeviceInfo\",\"WF_SERIAL\":\"12345678\"}";

//                byte b[] = date.toString().getBytes();
                byte b2[] = dateString.getBytes();

                outputStream.write(b2);
                outputStream.flush();
                Log.d(TAG + " ClientUIThread", "서버로 보냄.");

                final byte[] messageByte = new byte[50];
                InputStream inputStream = socket.getInputStream();
                inputStream.read(messageByte);

                String str = new String(messageByte, StandardCharsets.UTF_8).trim(); // 바이트배열의 공백제거
                Log.d(TAG + " ClientUIThread", str);

                BaseModel tmp = gson.fromJson(str, BaseModel.class);
//                Log.d(TAG + " ClientUIThread", tmp.getAsString());

                Log.d(TAG + " 0ClientUIThread", "받은 데이터 : " + tmp.getWF_SER_NUM());

//                for (byte tmp : message){
//                    Log.d("ClientUIThread", "받은 데이터 : " + tmp);
//                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activityMainBinding.output.setText("받은 데이터 : " + new String(messageByte));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }
}
