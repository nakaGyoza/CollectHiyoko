package com.example.nakajie.collecthiyoko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    Random random;


    Handler handler;
    Timer jumpTimer = null,
          translateTimer = null,
          collisionTimer = null,
          gameTimer = null;

    int time = 30;

    /*
     * 各View
     */

    ImageView hiyokoImage;
    ImageView hiyoko[];
    ImageView niwatori;
    TextView scoreText;
    TextView time_text;
    View screen;
    FrameLayout gameScreen;


    /*
     * ひよこのサイズ
     */
    float hiyokoWidth;
    float hiyokoHeight;

    /*
     * にわとりのサイズ
     */
    float niwatoriWidth;
    float niwatoriHeight;

    /*
     * ゲームのフレームのサイズ
     */
    float gameFrameWidth;
    float gameFrameHeight;

    /*
     * 画面の高さ
     */
    float screenHeight;

    /*
     * にわとりの速度　
     */
    final int NIWATORI_SPEED_X = 30;

    int niwatoriY;
    int niwatoriX;
    int shokiY;
    int gravity = 2;
    int jump_pow = 42;
    int score;

    boolean niwatoriDirection = false; //false...right true...left

    /*
     * 方向と値が決まっているのなら、名前をつけちゃう
     */
    final boolean DIRECTION_LEFT = true;
    final boolean DIRECTION_RIGHT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Viewの初期化(findViewById)はまとめてメソッド化しちゃおう
        initViews();

        // handlerは一個でいいかも
        // jump_Handler = new Handler();
        // tori_Handler = new Handler();
        handler = new Handler();
        random = new Random();



        niwatoriTranslate();
        startCollisionTimer();
        startGameTimer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 表示と同時にウィジェットの高さや幅を取得したいときは大抵ここで取る。
        if (hasFocus) {
            /*
             * 変数名は分かりやすいものをつける(ゲームの枠を表すViewなのでgameFrameView)
             * あと、色んな所で使う必要のない変数はローカルに定義する
             */
            FrameLayout gameFrameView = (FrameLayout) findViewById(R.id.gameFrame);
            LinearLayout deviceScreen = (LinearLayout) findViewById(R.id.linearLayout);


            /*
             * ゲームの領域を取得する
             * Javaは基本 _ で繋がず単語の区切りで大文字にする
             * (スネークケース、キャメルケース)
             */
            gameFrameWidth = gameFrameView.getWidth();
            gameFrameHeight = gameFrameView.getHeight();

            /*
             * スクリーンの高さを取得する
             */
            screenHeight = deviceScreen.getHeight();

             /*
             * ひよこの初期位置をランダムで決定する
             */
            appearHiyokoRandom();

            // ローディング中の画像を表示する
            for (byte i = 0;i < 3;i++) {
                hiyoko[i].setVisibility(hiyoko[i].VISIBLE);
            }
            niwatori.setVisibility(niwatori.VISIBLE);

            /*
             * ひよこの縦横のサイズを取得する
             */
            hiyokoWidth = hiyoko[0].getWidth();
            hiyokoHeight = hiyoko[0].getHeight();

            Log.d("ひよこの横幅", "hiyowidth = " + hiyokoWidth);

            /*
             * にわとりの縦横のサイズを取得する
             */
            niwatoriWidth = niwatori.getWidth();
            niwatoriHeight = niwatori.getHeight();

            // にわとりの初期座標取得
            niwatoriX = 0;
            niwatoriY = (int) (gameFrameHeight - niwatoriHeight);
            niwatori.setY(niwatoriY);
            shokiY = niwatoriY;
            Log.d("にわとりのY座標", "niwatoriY = " + niwatoriY + "ニワトリとんでる？ : " + String.valueOf(isNiwatoriJumping()) + " 初期Y　：" + shokiY);



        }
        super.onWindowFocusChanged(hasFocus);
    }

    public void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        time--;
                        if (time < 0) {
                            gameTimer.cancel();
                            gameTimer = null;
                            finish();
                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                            Bundle bandle = new Bundle();
                            bandle.putInt("スコア", score);
                            intent.putExtras(bandle);
                            startActivity(intent);
                        }else {
                            time_text.setText("" + time);
                        }

                    }
                });
            }
        },0,1000);
    }


    public void niwatoriTranslate() {
        translateTimer = new Timer();
        translateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        slideNiwatori(niwatoriDirection);
                        changeDirection();

                            if(niwatoriY > shokiY) {
                                niwatoriY = shokiY;
                                jump_pow = 42;

                            }
                            if (niwatoriY < 0) {
                                niwatoriY = 0;
                                jump_pow = 0;
                            }


                            niwatori.setY(niwatoriY);
                            jump_pow -= gravity;
                            if (isNiwatoriJumping()){
                                niwatoriY -= jump_pow;;
                            }
                    }
                });
            }
        }, 0, 30);
    }

    public void niwatoriJumpTap(View v){
        Log.d("にわとりY座標 : ","" +niwatoriY);
        jump_pow = 42;
        niwatoriY -= jump_pow;
    }


    public void startCollisionTimer() {  //当たり判定
        if (collisionTimer != null) {
            collisionTimer.cancel();
            collisionTimer = null;
        }

        collisionTimer = new Timer();
        collisionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 衝突判定の条件式をメソッドにしちゃう
                       for (byte i = 0; i < 3 ; i++) {
                           if (checkCollision(i)) {
                               appearHiyokoRandom();
                               addScore();
                               Log.d("score = ", "" + score);
                           }
                       }
                    }
                });

            }
        }, 0, 30);
    }





    /*
     * 使用するViewをまとめて初期化する
     */
    public void initViews() {
        scoreText = (TextView) findViewById(R.id.score);
        time_text = (TextView) findViewById(R.id.time);

        gameScreen = (FrameLayout)findViewById(R.id.gameFrame);
        screen = findViewById(R.id.linearLayout);

        scoreText.setText("" + score);
        time_text.setText("" + time);

        niwatori = (ImageView) findViewById(R.id.niwatori);

        hiyoko = new ImageView[3];
        for (byte i = 0 ; i < 3 ; i++){
            hiyoko[i] = new ImageView(this);
            hiyoko[i].setImageResource(R.drawable.hiyoko);
            gameScreen.addView(hiyoko[i]);
        }
    }

    /*
     * ひよこをランダムに出現させる
     */
    public void appearHiyokoRandom() {
        for (byte i = 0 ; i < 3 ; i++) {
            hiyoko[i].setX(random.nextInt((int) (gameFrameWidth - hiyokoWidth)));
            hiyoko[i].setY(random.nextInt((int) (gameFrameHeight - hiyokoHeight)));
        }
    }

    /*
     * にわとりViewを横に移動させる
     */
    public void slideNiwatori(boolean direction) {
        if (direction == DIRECTION_LEFT) {
            niwatori.setImageResource(R.drawable.niwatori_left);
            niwatori.setX(niwatori.getX() - NIWATORI_SPEED_X);
        } else if(direction == DIRECTION_RIGHT){
            niwatori.setImageResource(R.drawable.niwatori_right);
            niwatori.setX(niwatori.getX() + NIWATORI_SPEED_X);
        }
    }

    /*
     * にわとりの向きを変更する
     */
    public void changeDirection() {
        if (isLeftSideOver()) {
            niwatoriDirection = DIRECTION_RIGHT;
        } else if (isRightSideOver()) {
            niwatoriDirection = DIRECTION_LEFT;
        }
    }

    /*
     * にわとりが左端を越えているかチェック
     */
    public boolean isLeftSideOver() {
        return niwatori.getX() < 0;
    }

    /*
     * にわとりが右端を越えているかチェック
     */
    public boolean isRightSideOver() {
        return niwatori.getX() + niwatoriWidth > gameFrameWidth;
    }

    /*
     * ひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollision(byte b) {
        return checkCollisionX(b) && checkCollisionY(b);
    }

    /*
     * X軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionX(byte b) {
        return niwatori.getX() + niwatoriWidth > hiyoko[b].getX() && niwatori.getX() < hiyoko[b].getX() + hiyokoWidth;
    }

    /*
     * Y軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionY(byte b) {
        return niwatori.getY() + niwatoriHeight > hiyoko[b].getY() && niwatori.getY() < hiyoko[b].getY() + hiyokoHeight;
    }

    /*
     * にわとりがジャンプ中かどうかを確かめる
     */
    public boolean isNiwatoriJumping(){
        return niwatoriY < shokiY && niwatoriY >= 0;
    }

    /*
     * スコアを1足してviewに表示する
     */
    public void addScore() {
        score++;
        scoreText.setText(score + "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (jumpTimer != null) {
            jumpTimer.cancel();
        }
    }

}
