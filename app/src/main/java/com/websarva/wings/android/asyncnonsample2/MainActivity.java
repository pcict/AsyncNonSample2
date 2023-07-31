package com.websarva.wings.android.asyncnonsample2;


import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ボタンの取得
        Button btSync = findViewById(R.id.btSync);
        Button btAsync = findViewById(R.id.btAsync);
        Button btToast = findViewById(R.id.btToast);

        //ボタンにリスナーを登録
        btSync.setOnClickListener(new SyncClickListener());
        btAsync.setOnClickListener(new AsyncClickListener());
        btToast.setOnClickListener(new ToastClickListener());
    }

    //５秒待機メソッド
    public void SleepMethod(){
        try {
            Log.i("AsyncNonSample", "Sleep開始");
            Thread.sleep(5000); //5000ms
            Log.i("AsyncNonSample", "Sleep終了");
        }catch (Exception ex){}
    }

    private class Receiver implements Runnable{
        private final Handler _handler;

        //コンストラクタでhandlerオブジェクトを取得
        public Receiver(Handler handler){
            _handler = handler;
        }

        @WorkerThread
        @Override
        public void run() {
            SleepMethod();
            //UIスレッドに渡すデータ
            String result = "ワーカースレッドで5秒経過";
            //コンストラクタの引数にUIスレッドに渡すデータを入れる
            Poster poster = new Poster(result);
            //UIスレッドのLooperにHandlerを使って処理を送る
            _handler.post(poster);
        }
    }

    private class Poster implements Runnable{

        private final String _result;

        //コンストラクタでワーカースレッドから送られてきたデータを取得
        public Poster(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run() {
            //TextViewにワーカースレッドから送られてきたデータを表示
            TextView tvMsg = findViewById(R.id.tvMsg);
            tvMsg.setText(_result);
        }
    }


    //同期ボタン
    private class SyncClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            SleepMethod();
        }
    }

    //非同期ボタン
    private class AsyncClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            //Looperオブジェクトの取得
            Looper mainLooper = Looper.getMainLooper();
            //LooperにバインドしたHandlerオブジェクトの作成
            Handler handler = HandlerCompat.createAsync(mainLooper);

            Receiver receiver = new Receiver(handler);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(receiver);
        }
    }

    private class ToastClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Toast.makeText(MainActivity.this,"トースト表示",Toast.LENGTH_LONG).show();
        }
    }
}