package com.example.nakajie.collecthiyoko;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {


    Random rnd;

    ObjectAnimator objectAnimator;

    Timer jump_Timer = null;
    Handler jump_Handler;

    Timer translate_Timer = null;
    Handler translate_Handler;

    Timer tori_Timer = null;
    Handler tori_Handler;

    Timer time_Timer = null;
    Handler time_Handler;
    int m;
    int s;
    int time = 30;

    FrameLayout hiyokoView;
    ImageView hiyoko;
    ImageView niwatori;
    TextView score_text;
    TextView time_text;

    float hiyo_width;
    float hiyo_height;
    float tori_width;
    float tori_height;
    float view_width;
    float view_height;

    int hiyoY;
    int hiyoX;
    int shokiY;
    int gravity = 3;
    int jump_pow = 42;
    int score;

    boolean hiyo_direction = false; //false...right true...left


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hiyoko = (ImageView) findViewById(R.id.hiyoko);
        niwatori = (ImageView)findViewById(R.id.niwatori);
        hiyokoView = (FrameLayout) findViewById(R.id.hiyokoView);
        score_text = (TextView)findViewById(R.id.score);
        score_text.setText("" + score);
        time_text = (TextView)findViewById(R.id.time);
        time_text.setText("0:00");

        jump_Handler = new Handler();
        tori_Handler = new Handler();
        translate_Handler = new Handler();
        rnd = new Random();

        hiyokoTranslationX();
        hiyokoCollision();


        //hiyoko.setImageResource(R.drawable.hiyoko);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 表示と同時にウィジェットの高さや幅を取得したいときは大抵ここで取る。
        if (hasFocus) {

            view_width = this.hiyokoView.getWidth();
            view_height = this.hiyokoView.getHeight();

            // ローディング中の画像を表示する
            this.hiyoko.setVisibility(hiyoko.VISIBLE);
            hiyo_width = this.hiyoko.getWidth();
            hiyo_height = this.hiyoko.getHeight();
            Log.d("ひよこの横幅", "hiyowidth = " + hiyo_width);
            //ひよこの初期座標取得
            hiyoX = 0;
            hiyoY = (int)(view_height - hiyo_height);
            shokiY = (int)hiyoko.getY();
            Log.d("ひよこのY座標", "hiyoY = " + hiyoY);

            this.niwatori.setVisibility(niwatori.VISIBLE);
            tori_width = this.niwatori.getWidth();
            tori_height = this.niwatori.getHeight();
            niwatori.setX(rnd.nextInt((int)(view_width - tori_width)));
            niwatori.setY(rnd.nextInt((int)(view_height - tori_height)));

        }
        super.onWindowFocusChanged(hasFocus);
    }




    public void hiyokoTranslationX() {
        translate_Timer = new Timer();
        translate_Timer.schedule(new TimerTask() {
            @Override
            public void run() {
                translate_Handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (hiyo_direction == false){
                            hiyoko.setX(hiyoko.getX() + 10);
                            if (hiyoko.getX() + hiyo_width > view_width){ hiyo_direction = true; }
                        }else{
                            hiyoko.setX(hiyoko.getX() - 10);
                            if (hiyoko.getX() < 0){ hiyo_direction = false; }
                        }
                    }
                });
            }
        },0,30);
    }

    public void hiyokoCollision(){  //当たり判定
        tori_Timer = new Timer();
        tori_Timer.schedule(new TimerTask() {
            @Override
            public void run() {

                tori_Handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (hiyoko.getX() < niwatori.getX() + tori_width && niwatori.getX() < hiyoko.getX() + hiyo_width){
                            if (hiyoko.getY() < niwatori.getY() + tori_height && niwatori.getY() < hiyoko.getY() + hiyo_height){
                                niwatori.setX(rnd.nextInt((int)(view_width - tori_width)));
                                niwatori.setY(rnd.nextInt((int)(view_height - tori_height)));
                                score++;
                                score_text.setText("" + score);
                                Log.d("score = ","" + score);
                            }
                        }

                    }
                });

            }
        },0,30);
    }

    public void hiyokoJump(View v) {
        jump_pow = 50;
        if (jump_Timer != null){
            jump_Timer.cancel();
         }
        jump_Timer = null;

        if (jump_Timer == null) {

            //タイマーの初期化処理
            jump_Timer = new Timer();
            jump_Timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    jump_Handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (hiyoY <= shokiY && hiyoY >= 0) {    //ジャンプ中
                                hiyoY -= jump_pow;
                            }

                            if (hiyoY > shokiY) {   //地面にめり込んだ時
                                hiyoY = shokiY;
                                jump_pow = 50;
                                jump_Timer.cancel();
                                jump_Timer = null;

                            }

                            if (hiyoY < 0) {    //天井にめり込んだ時
                                hiyoY = 0;
                                jump_pow = 0;
                            }
                            hiyoko.setY(hiyoY);
                            jump_pow -= gravity;
                        }
                    });
                }
            },0, 30);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onDestroy() {
        super.onDestroy();
        if(jump_Timer != null){
            jump_Timer.cancel();
        }
    }

}
