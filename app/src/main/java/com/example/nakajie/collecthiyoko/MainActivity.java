package com.example.nakajie.collecthiyoko;

import android.animation.ObjectAnimator;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.view.Display;
import android.view.WindowManager;
import android.graphics.Point;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    ImageView hiyoko;
    WindowManager wm;
    Display disp;
    Point dispsize;

    ObjectAnimator objectAnimator;


    float disp_width;
    float disp_height;
    float hiyo_width;
    float hiyo_height;

    float hiyoY;
    int gravity = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hiyoko = (ImageView)findViewById(R.id.hiyoko);

        // WindowManagerのインスタンス取得
        wm = getWindowManager();
        // Displayのインスタンス取得
        disp = wm.getDefaultDisplay();
        dispsize = new Point();
        disp.getSize(dispsize);
        //画面の横幅取得
        disp_width = dispsize.x;
        //画面の縦幅取得
        disp_height = dispsize.y;

        hiyoko.setImageResource(R.drawable.hiyoko);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 表示と同時にウィジェットの高さや幅を取得したいときは大抵ここで取る。
        if (hasFocus) {
            // ローディング中の画像を表示する
            this.hiyoko.setVisibility(hiyoko.VISIBLE);
            hiyo_width = this.hiyoko.getWidth();
            hiyo_height = this.hiyoko.getHeight();
            Log.d("ひよこの横幅","hiyowidth = " + hiyo_width);
            //ひよこの初期Y座標取得
            hiyoY = hiyoko.getY();
            Log.d("ひよこのY座標","hiyoY = " + hiyoY);
        }
        super.onWindowFocusChanged(hasFocus);
        animateTranslationX(hiyoko);
    }

    private void animateTranslationX(ImageView target){
        // translationXプロパティを変化させます
        objectAnimator = ObjectAnimator.ofFloat(target, "translationX",0f,disp_width - hiyo_width);
        // 5秒かけて実行させます
        objectAnimator.setDuration(1500);

        objectAnimator.setRepeatCount(Animation.INFINITE);
        objectAnimator.setRepeatMode(Animation.REVERSE);
        objectAnimator.start();
    }

    public void hiyokoJump(View v){
        for(int jump_pow = 50;jump_pow >= -50;jump_pow -= gravity){
            if(hiyoY < 0.0){
                hiyoY = 0;
            }else if(hiyoY > disp_height - hiyo_height)
            {
                hiyoY = disp_height - hiyo_height;
            }
            hiyoko.setY(hiyoY - jump_pow);
            Log.d("ひよこのY座標","hiyoY = " + hiyoY);
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
}
