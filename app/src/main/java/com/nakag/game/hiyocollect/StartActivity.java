package com.nakag.game.hiyocollect;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.nakag.game.hiyocollect.GamesClientHelper.GamesClientHelperListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class StartActivity extends Activity implements GamesClientHelperListener{

    MediaPlayer startBGM;
    FrameLayout deviceScreen;

    ArrayList<ImageView> hiyoko = new ArrayList<ImageView>();
    ArrayList<Integer> hiyokoSpeed = new ArrayList<Integer>();
    ArrayList<Boolean> doFallHiyoko = new ArrayList<Boolean>();

    Random random;

    Timer hiyokoTimer;
    Handler handler;

    /*
     * ひよこのサイズ
     */
    int hiyokoWidth;
    int hiyokoHeight;

    /*
     * 画面のサイズ
     */
    int screenHeight;
    int screenWidth;

    /*
     *  ひよこの数
     */
    int hiyokoNum = 15;

    int initSpeed;

    private GamesClientHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startBGM = MediaPlayer.create(this,R.raw.start_bgm);
        random = new Random();
        startBGM.setLooping(true);
        startBGM.start();
        handler = new Handler();

        helper = new GamesClientHelperImpl(this,this);
        helper.connect();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            deviceScreen = (FrameLayout) findViewById(R.id.frameLayout);

            /*
             * スクリーンの高さを取得する
             */
            screenHeight = deviceScreen.getHeight();
            screenWidth = deviceScreen.getWidth();

            /*
             * ひよこの縦横のサイズを取得する
             */
            hiyokoWidth = (int) (screenHeight / 13);
            hiyokoHeight = (int) (screenHeight / 13);

            initSpeed = (int)(screenHeight / 100);

             /*
              * ひよこを生成
              */
            for (int i = 0; i < hiyokoNum; i++) {
                newHiyoko(i);
            }

            for (int i = 0; i < hiyokoNum; i++) {
                displayHiyoko(i);
            }

            // ローディング中の画像を表示する
            for (int i = 0; i < hiyokoNum; i++) {
                hiyoko.get(i).setVisibility(hiyoko.get(i).VISIBLE);
            }

            fallHiyoko();
        }
    }

    /*
     * ひよこを落とす処理
     */
    public void fallHiyoko(){
        hiyokoTimer = new Timer();
        hiyokoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0 ; i < hiyokoNum ; i++ ){
                            if (doFallHiyoko.get(i)){
                                hiyoko.get(i).setY(hiyoko.get(i).getY() + hiyokoSpeed.get(i));
                                if (hiyoko.get(i).getY() > screenHeight + hiyokoHeight){
                                    doFallHiyoko.set(i,false);
                                    displayHiyoko(i);
                                }
                            }else{
                                if (random.nextInt(80) == 0){
                                    doFallHiyoko.set(i,true);
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 25);
    }

    /*
     * 新しいひよこの初期化
     */
    public void newHiyoko(int i) {
        hiyoko.add(new ImageView(this));
        doFallHiyoko.add(false);
        hiyokoSpeed.add(initSpeed);
        hiyoko.get(i).setImageResource(R.drawable.hiyoko);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (hiyokoWidth), (int) (hiyokoHeight));
        deviceScreen.addView(hiyoko.get(i), params);
    }


    public void displayHiyoko(int i) {
        hiyoko.get(i).setX(random.nextInt((int) (screenWidth - hiyokoWidth)));
        hiyoko.get(i).setY(-hiyokoHeight);
    }

    @Override
    protected  void onPause(){
        super.onPause();

        if (startBGM.isPlaying()) {
            startBGM.stop();
        }

        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if(startBGM.isPlaying()){
                startBGM.stop();
            }

            finish();
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        startBGM.release();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(this.getClass().getSimpleName(), "onConnected");
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
//        isGooglePlayServicesConnected = true;
//        findViewById(R.id.sign_in_button).setEnabled(false);
    }

    @Override
    public void onError(ConnectionResult result) {
        Log.e(this.getClass().getSimpleName(), "onError");
        Toast.makeText(this, "onError: ErrorCode" + result.getErrorCode(), Toast.LENGTH_SHORT).show();
    }
}
