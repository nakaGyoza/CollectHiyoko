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
import android.animation.Animator;
import android.animation.AnimatorSet;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    ImageView hiyoko;
    WindowManager wm;
    Display disp;
    Point dispsize;

    ObjectAnimator objectAnimator;


    float dispwidth;
    float hiyowidth;

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
        dispwidth = dispsize.x;

        hiyoko.setImageResource(R.drawable.hiyoko);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 表示と同時にウィジェットの高さや幅を取得したいときは大抵ここで取る。
        if (hasFocus) {
            // ローディング中の画像を表示する
            this.hiyoko.setVisibility(hiyoko.VISIBLE);
           hiyowidth = this.hiyoko.getWidth();
           Log.d("ひよこの横幅","hiyowidth = " + hiyowidth);
        }
        super.onWindowFocusChanged(hasFocus);
        animateTranslationX(hiyoko);
    }

    private void animateTranslationX(ImageView target){
        // translationXプロパティを変化させます
        objectAnimator = ObjectAnimator.ofFloat(target, "translationX",0f,dispwidth - hiyowidth);
        // 5秒かけて実行させます
        objectAnimator.setDuration(1500);

        objectAnimator.setRepeatCount(Animation.INFINITE);
        objectAnimator.setRepeatMode(Animation.REVERSE);

        objectAnimator.start();

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
