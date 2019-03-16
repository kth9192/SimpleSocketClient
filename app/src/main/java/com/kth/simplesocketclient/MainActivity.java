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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.kth.simplesocketclient.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding activityMainBinding;
    private static String IP;
    private static int PORT;
    private Handler handler;
    private Gson gson;
    private JsonParser parser;

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
        activityMainBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientThread thread = new ClientThread();
                thread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityMainBinding.output.setText("없음");
    }

    class ClientThread extends Thread {

        public void run() {

            try {
                Socket socket = new Socket(IP, PORT);

                OutputStream outputStream = socket.getOutputStream();

                Date date = new Date();
                String dateString = "the test sendtext";

                byte b[] = date.toString().getBytes();
                byte b2[] = dateString.getBytes();

                outputStream.write(b2);
                outputStream.flush();
                Log.d("ClientThread", "서버로 보냄.");

                final byte[] messageByte = new byte[1024];
                InputStream inputStream = socket.getInputStream();
                inputStream.read(messageByte);

//                String str = new String(messageByte, StandardCharsets.UTF_8);
//                Log.d("ClientThread" , str);

                String tmp = parser.parse(new String(messageByte)).getAsString();
                tmp = tmp.replace("\\u0000", "");
                tmp = tmp.replace("\\","");


                Log.d("ClientThread" , tmp);

                JSONObject jsonObject = new JSONObject(tmp);

                Log.d("ClientThread", "받은 데이터 : " + jsonObject.get("name"));

//                for (byte tmp : message){
//                    Log.d("ClientThread", "받은 데이터 : " + tmp);
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
