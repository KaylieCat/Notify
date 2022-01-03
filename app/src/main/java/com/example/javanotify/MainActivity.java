package com.example.javanotify;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


   private TextView mTextViewResult;
   private EditText number1;
   private EditText number2;
   private EditText number3;
   private EditText message;
   private Button button;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize yung mga laman ng activity
        mTextViewResult = findViewById(R.id.text1);
        number1 = (EditText) findViewById(R.id.number1);
        number2 = (EditText) findViewById(R.id.number2);
        number3 = (EditText) findViewById(R.id.number3);
        message = (EditText) findViewById(R.id.message);
        button = (Button) findViewById(R.id.button);


        //button na pag pinindot mag rurun yung code
        button.setOnClickListener(v -> {
            String num1 = number1.getText().toString();
            String num2 = number2.getText().toString();
            String num3 = number3.getText().toString();
            String msg = message.getText().toString();


            Map<String, String> params = new HashMap<>();
            params.put("message", msg);
            params.put("apikey", "f65fd3e33a16c7e97803b74b82e69c1b");
            params.put("sendername", "");
            params.put("number", num1);

            //ieencode yung url para sa query params
            String encodedURL = params.entrySet().stream()
                    .map(entry -> {
                        try {
                            return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return "";
                    })
                    .collect(Collectors.joining("&", "https://api.semaphore.co/api/v4/messages?", ""));
            Log.d("MainActivity", encodedURL);

            params.clear();

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(encodedURL)
                    .method("POST", body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewResult.setText("Error1");
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        String myResponse = response.body().string();

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewResult.setText(myResponse);
                            }
                        });
                    }
                    else{


                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewResult.setText("Error2");
                            }
                        });
                    }
                }
            });

        });




    }




}