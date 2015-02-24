package com.example.nakajie.collecthiyoko;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    ImageView hiyoko;
    ImageView niwatori;
    TextView scoreText;
    TextView time_text;

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

    int hiyokoY;
    int hiyokoX;
    int shokiY;
    int gravity = 3;
    int jump_pow = 42;
    int score;

    boolean hiyokoDirection = false; //false...right true...left

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

        hiyokoTranslateX();
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

            /*
             * ゲームの領域を取得する
             * Javaは基本 _ で繋がず単語の区切りで大文字にする
             * (スネークケース、キャメルケース)
             */
            gameFrameWidth = gameFrameView.getWidth();
            gameFrameHeight = gameFrameView.getHeight();

            // ローディング中の画像を表示する
            hiyoko.setVisibility(hiyoko.VISIBLE);
            niwatori.setVisibility(niwatori.VISIBLE);

            /*
             * ひよこの縦横のサイズを取得する
             */
            hiyokoWidth = hiyoko.getWidth();
            hiyokoHeight = hiyoko.getHeight();

            Log.d("ひよこの横幅", "hiyowidth = " + hiyokoWidth);

            /*
             * にわとりの縦横のサイズを取得する
             */
            niwatoriWidth = niwatori.getWidth();
            niwatoriHeight = niwatori.getHeight();

            // ひよこの初期座標取得
            hiyokoX = 0;
            hiyokoY = (int) (gameFrameHeight - hiyokoHeight);
            shokiY = (int) hiyoko.getY();
            Log.d("ひよこのY座標", "hiyokoY = " + hiyokoY);

            /*
             * にわとりの初期位置をランダムで決定する
             */
            appearNiwatoriRandom();

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


    public void hiyokoTranslateX() {
        translateTimer = new Timer();
        translateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        slideHiyoko(hiyokoDirection);
                        changeDirection();
                    }
                });
            }
        }, 0, 30);
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
                        if (checkCollision()) {
                            appearNiwatoriRandom();
                            addScore();
                            Log.d("score = ", "" + score);
                        }
                    }
                });

            }
        }, 0, 30);
    }

    public void hiyokoJump(View v) {
        jump_pow = 50;
        if (jumpTimer != null) {
            jumpTimer.cancel();
            jumpTimer = null;
        }

        //タイマーの初期化処理
        jumpTimer = new Timer();
        jumpTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        // ジャンプ中かどうかの判定もメソッドにしちゃう
                        if (isHiyokoJumping()) {    //ジャンプ中
                            hiyokoY -= jump_pow;
                        }

                        if (hiyokoY > shokiY) {   //地面にめり込んだ時
                            hiyokoY = shokiY;
                            jump_pow = 50;
                            jumpTimer.cancel();
                            jumpTimer = null;
                        }

                        if (hiyokoY < 0) {    //天井にめり込んだ時
                            hiyokoY = 0;
                            jump_pow = 0;
                        }

                        hiyoko.setY(hiyokoY);
                        jump_pow -= gravity;
                    }
                });
            }
        }, 0, 30);
    }


    /*
     * 使用するViewをまとめて初期化する
     */
    public void initViews() {
        hiyoko = (ImageView) findViewById(R.id.hiyoko);
        niwatori = (ImageView) findViewById(R.id.niwatori);

        scoreText = (TextView) findViewById(R.id.score);
        time_text = (TextView) findViewById(R.id.time);

        scoreText.setText("" + score);
        time_text.setText("" + time);
    }

    /*
     * にわとりをランダムに出現させる
     */
    public void appearNiwatoriRandom() {
        niwatori.setX(random.nextInt((int) (gameFrameWidth - niwatoriWidth)));
        niwatori.setY(random.nextInt((int) (gameFrameHeight - niwatoriHeight)));
    }

    /*
     * ひよこViewを横に移動させる
     */
    public void slideHiyoko(boolean direction) {
        if (direction == DIRECTION_LEFT) {
            hiyoko.setX(hiyoko.getX() - 30);
        } else if(direction == DIRECTION_RIGHT){
            hiyoko.setX(hiyoko.getX() + 30);
        }
    }

    /*
     * ひよこの向きを変更する
     */
    public void changeDirection() {
        if (isLeftSideOver()) {
            hiyokoDirection = DIRECTION_RIGHT;
        } else if (isRightSideOver()) {
            hiyokoDirection = DIRECTION_LEFT;
        }
    }

    /*
     * ひよこが左端を越えているかチェック
     */
    public boolean isLeftSideOver() {
        return hiyoko.getX() < 0;
    }

    /*
     * ひよこが右端を越えているかチェック
     */
    public boolean isRightSideOver() {
        return hiyoko.getX() + hiyokoWidth > gameFrameWidth;
    }

    /*
     * ひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollision() {
        return checkCollisionX() && checkCollisionY();
    }

    /*
     * X軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionX() {
        return hiyoko.getX() - niwatoriWidth < niwatori.getX() && niwatori.getX() < hiyoko.getX() + hiyokoWidth;
    }

    /*
     * Y軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionY() {
        return hiyoko.getY() - niwatoriHeight < niwatori.getY() && niwatori.getY() < hiyoko.getY() + hiyokoHeight;
    }

    /*
     * ひよこがジャンプ中かどうかを確かめる
     */
    public boolean isHiyokoJumping(){
        return hiyokoY <= shokiY && hiyokoY >= 0;
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
