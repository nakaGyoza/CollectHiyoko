package com.nakag.game.hiyocollect;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    Random random;


    Handler handler;
    Timer translateTimer = null,
            collisionTimer = null,
            gameTimer = null;

    /*
     * タイマーの時間を減らすための変数
     */
    int time = 10;
    int timeCnt = 0; //1秒ごとにtimeを１減らすための変数

    /*
     * 各View・
     */

    ArrayList<ImageView> hiyoko = new ArrayList<ImageView>();
    ImageView niwatori;
    TextView scoreText;
    TextView time_text;
    TextView score_text;
    View screen;
    FrameLayout gameScreen;
    ProgressBar progressBar;
    Button goToStart;
    Button quitGame;
    LinearLayout shareButtons;
    ImageView twitterButton;
    ImageView lineButton;
    MediaPlayer gameBGM;
    MediaPlayer kokekokko;
    Vibrator hiyokoGetEffect;

    boolean doScreenSizeGet = false;    //二度onWindowFocusChangedが呼ばれると落ちるのでそれを防ぐ
    boolean isResult = false;           //結果画面にいるか
    boolean doVibrate = true;           //バイブレーションするか

    private final static int LINE_ID = 0;
    private final static int TWITTER_ID = 1;
    private final String[] sharePackages = {"jp.naver.line.android", "com.twitter.android"};


    /*
      BGM,効果音再生
     */
    SoundPool playSe;
    int seId[];


    /*
     * ひよこのサイズ
     */
    int hiyokoWidth;
    int hiyokoHeight;

    /*
     * にわとりのサイズ
     */
    int niwatoriWidth;
    int niwatoriHeight;

    /*
     * にわとりの速度
     */
    int niwatoriSpeedX;

    /*
     * ゲームのフレームのサイズ
     */
    int gameFrameWidth;
    int gameFrameHeight;


    /*
     * 画面のサイズ
     */
    int screenHeight;
    int screenWidth;


    /*
     * にわとりの座標
     */
    int niwatoriY;
    int niwatoriX;
    int shokiY;

    /*
     *  ひよこの数
     */
    int hiyokoNum = 6;

    /*
     * ジャンプ処理用変数
     */
    int gravity = 1;
    int jumpPow;
    int defaultJumpPow = 42;

    /*
     * スコア、ゲージ用変数
     */
    int score;
    int gaugePoint = 0;

    /*
     * 重くなるのを防ぐためにひよこの数によってバイブの回数を制限
     */
    int vibeLimit;


    boolean niwatoriDirection = false; //false...right true...left

    /*
     * にわとりが左を向いているときは真で、右を向いているときは偽
     */
    final boolean DIRECTION_LEFT = true;
    final boolean DIRECTION_RIGHT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        random = new Random();

        seId = new int[6];

        gameBGM = MediaPlayer.create(this, R.raw.bgm);
        gameBGM.setLooping(true);
        gameBGM.start();
        kokekokko = MediaPlayer.create(this, R.raw.end_game);
        hiyokoGetEffect = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 予め音声データを読み込む
        playSe = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);

        seId = new int[6];

        seId[0] = playSe.load(this, R.raw.add_hiyoko, 1);
        seId[1] = playSe.load(this, R.raw.end_game, 1);
        seId[2] = playSe.load(this, R.raw.extend_time, 1);
        seId[3] = playSe.load(this, R.raw.game_start, 1);
        seId[4] = playSe.load(this, R.raw.jump, 1);
        seId[5] = playSe.load(this, R.raw.piyo, 1);

        if (!isResult) {
            gameBGM.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //インスタンス解放
        for (int i = 0; i < 6; i++) {
            playSe.unload(seId[i]);
        }
        playSe.release();

        gameBGM.pause();

        if (kokekokko.isPlaying()) {
            kokekokko.stop();
        }
        hiyokoGetEffect.cancel();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 表示と同時にウィジェットの高さや幅を取得
        if (hasFocus) {

            if (!doScreenSizeGet) {
                FrameLayout gameFrameView = (FrameLayout) findViewById(R.id.gameFrame);
                LinearLayout deviceScreen = (LinearLayout) findViewById(R.id.rootLayout);

            /*
             * ゲームの領域を取得する
             */
                gameFrameWidth = gameFrameView.getWidth();
                gameFrameHeight = gameFrameView.getHeight();

            /*
             * スクリーンの高さを取得する
             */
                screenHeight = deviceScreen.getHeight();
                screenWidth = deviceScreen.getWidth();

                // Viewの初期化(findViewById)をまとめたメソッド
                initViews();

                //端末の高さによってジャンプ処理の変数の値を変更する
                defaultJumpPow = (int) (42 * gameFrameHeight / 1418);
                gravity = (int) (2 * gameFrameHeight / 1418);

            /*
             * にわとりの速度　
             */
                niwatoriSpeedX = (int) (screenWidth / 25);

                niwatoriTranslate();
                startCollisionTimer();
                startGameTimer();

            /*
             * ひよこの縦横のサイズを取得する
             */
                hiyokoWidth = (int) (gameFrameHeight / 11.5);
                hiyokoHeight = (int) (gameFrameHeight / 11.5);

             /*
              * ひよこを生成
              */
                for (int i = 0; i < hiyokoNum; i++) {
                    newHiyoko(i);
                }


            /*
             * にわとりの縦横のサイズを取得する
             */
                niwatoriWidth = (int) (gameFrameHeight / 8.5);
                niwatoriHeight = (int) (gameFrameHeight / 8.5);

                niwatori = new ImageView(this);
                niwatori.setImageResource(R.drawable.niwatori_right);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(niwatoriWidth, niwatoriHeight, Gravity.BOTTOM);
                gameScreen.addView(niwatori, params);


             /*
             * ひよこの初期位置をランダムで決定する
             */
                for (int i = 0; i < hiyokoNum; i++) {
                    appearHiyokoRandom(i);
                }

                // ローディング中の画像を表示する
                for (int i = 0; i < hiyokoNum; i++) {
                    hiyoko.get(i).setVisibility(hiyoko.get(i).VISIBLE);
                }


                niwatori.setVisibility(niwatori.VISIBLE);

                // にわとりの初期座標取得
                niwatoriX = 0;
                niwatoriY = gameFrameHeight - niwatoriHeight;
                niwatori.setY(niwatoriY);
                shokiY = niwatoriY;

                doScreenSizeGet = true;
            }
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

                        decreaseGauge();

                        timeCnt += 1;
                        if (timeCnt == 20) {
                            time--;
                            timeCnt = 0;
                        }
                        if (time < 1) {

                            doVibrate = false;

                            hiyokoGetEffect.cancel();

                            if (translateTimer != null) {
                                translateTimer.cancel();
                                translateTimer = null;
                            }
                            if (collisionTimer != null) {
                                collisionTimer.cancel();
                                collisionTimer = null;
                            }
                            if (gameTimer != null) {
                                gameTimer.cancel();
                                gameTimer = null;
                            }

                            /* 結果画面へ遷移 */
                            goToResult();
                            isResult = true;

                        } else {
                            time_text.setText(time + "sec");
                        }

                    }
                });
            }
        }, 0, 50);
    }

    /*
     * 結果画面
     */
    private void goToResult() {

        setContentView(R.layout.activity_result);
        score_text = (TextView) findViewById(R.id.score_text);

        for (int i = 0; i < 6; i++) {
            playSe.stop(seId[i]);
        }

        gameBGM.stop();

        goToStart = (Button) findViewById(R.id.goToStart);
        quitGame = (Button) findViewById(R.id.quitGame);

        shareButtons = (LinearLayout) findViewById(R.id.shareButtons);
        twitterButton = (ImageView) findViewById(R.id.twitterButton);
        lineButton = (ImageView) findViewById(R.id.lineButton);

        AlphaAnimation feedin_btn = new AlphaAnimation(0, 1);
        feedin_btn.setDuration(2000);

        AnimationSet anime_share_btn = new AnimationSet(true);
        AlphaAnimation feedin_share_btn = new AlphaAnimation(0, 1);
        TranslateAnimation slide_share_btn = new TranslateAnimation(-250, 0, 0, 0);
        feedin_share_btn.setDuration(800);
        slide_share_btn.setDuration(1000);

        anime_share_btn.addAnimation(feedin_share_btn);
        anime_share_btn.addAnimation(slide_share_btn);

        anime_share_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                twitterButton.setClickable(false);
                lineButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                twitterButton.setClickable(true);
                lineButton.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shareButtons.startAnimation(anime_share_btn);

        feedin_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                goToStart.setClickable(false);
                quitGame.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                goToStart.setClickable(true);
                quitGame.setClickable(true);
            }
        });
        goToStart.startAnimation(feedin_btn);
        quitGame.startAnimation(feedin_btn);

        score_text.setText("SCORE:" + score);
        kokekokko.start();
    }

    /*
     * 結果画面からスタート画面への遷移
     */
    public void goToStart(View v) {
        if (kokekokko.isPlaying()) {
            kokekokko.stop();
        }
        finish();
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
    }

    /*
     * 結果画面からゲーム終了
     */
    public void quitGame(View v) {
        if (kokekokko.isPlaying()) {
            kokekokko.stop();
        }

        finish();
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

                        if (niwatoriY > shokiY) {
                            niwatoriY = shokiY;
                            jumpPow = defaultJumpPow;

                        }
                        if (niwatoriY < 0) {
                            niwatoriY = 0;
                            jumpPow = 0;
                        }


                        niwatori.setY(niwatoriY);
                        jumpPow -= gravity;
                        if (isNiwatoriJumping()) {
                            niwatoriY -= jumpPow;
                            ;
                        }
                    }
                });
            }
        }, 0, 25);
    }

    public void niwatoriJumpTap(View v) {
        jumpPow = defaultJumpPow;
        niwatoriY -= jumpPow;
        playSe.play(seId[4], 1, 1, 0, 0, 1);
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
                        for (int i = 0; i < hiyokoNum; i++) {
                            if (checkCollision(i)) {

                                vibrate();
                                playSe.play(seId[5], 1, 1, 0, 0, 1);
                                appearHiyokoRandom(i);
                                addScore();
                                if (score % 10 == 0) {    //スコアが10上がるごとにひよこを１匹追加
                                    hiyokoNum++;
                                    newHiyoko(hiyokoNum - 1);
                                    playSe.play(seId[0], 1, 1, 0, 0, 1);
                                }
                                getGaugePoint();
                            }
                        }
                    }
                });
            }
        }, 0, 40);
    }

    /*
     * バイブレーション処理
     */
    public void vibrate() {

        if (Thread.activeCount() < 15) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    hiyokoGetEffect.vibrate(50);
                }
            }).start();
        }
        Log.d("Active Count", ":" + Thread.activeCount());
    }


    /*
     * ゲームをリトライする
     */
    public void retryGame(View v) {
        hiyokoGetEffect.cancel();
        gameTimer.cancel();
        gameTimer = null;
        translateTimer.cancel();
        translateTimer = null;
        collisionTimer.cancel();
        collisionTimer = null;
        for (int i = 0; i < 6; i++) {
            playSe.stop(seId[i]);
        }
        gameBGM.stop();
        finish();
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
    }


    /*
     * 使用するViewをまとめて初期化する
     */
    public void initViews() {
        scoreText = (TextView) findViewById(R.id.score);
        time_text = (TextView) findViewById(R.id.time);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setMax(5000);
        gameScreen = (FrameLayout) findViewById(R.id.gameFrame);
        screen = findViewById(R.id.frameLayout);

        scoreText.setText("SCORE:" + score);
        time_text.setText(time + "sec");


    }

    /*
     * ひよこをランダムに出現させる
     */
    public void appearHiyokoRandom(int i) {
        hiyoko.get(i).setX(random.nextInt((int) (gameFrameWidth - hiyokoWidth)));
        hiyoko.get(i).setY(random.nextInt((int) (gameFrameHeight - hiyokoHeight)));
    }

    /*
     * にわとりViewを横に移動させる
     */
    public void slideNiwatori(boolean direction) {
        if (direction == DIRECTION_LEFT) {
            niwatori.setImageResource(R.drawable.niwatori_left);
            niwatori.setX(niwatori.getX() - niwatoriSpeedX);
        } else if (direction == DIRECTION_RIGHT) {
            niwatori.setImageResource(R.drawable.niwatori_right);
            niwatori.setX(niwatori.getX() + niwatoriSpeedX);
        }
    }

    /*
     * 時間経過でゲージを減らす
     */
    public void decreaseGauge() {
        gaugePoint -= 25;
        if (gaugePoint <= 0) {
            gaugePoint = 0;
        }
        progressBar.setProgress(gaugePoint);
    }

    /*
     * ゲージが満タンになると残り時間を延長する
     */
    public void getGaugePoint() {
        gaugePoint += (int) (5000 / (hiyokoNum - 1));
        if (gaugePoint >= 5000) {
            progressBar.setProgress(5000);
            gaugePoint -= 5000;
            time += 2;
            playSe.play(seId[2], 1, 1, 0, 0, 1);
        } else {
            progressBar.setProgress(gaugePoint);
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
    public boolean checkCollision(int b) {
        return checkCollisionX(b) && checkCollisionY(b);
    }

    /*
     * X軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionX(int b) {
        return niwatori.getX() + niwatoriWidth - (int) (gameFrameHeight / 55) > hiyoko.get(b).getX() && niwatori.getX() + (int) (gameFrameHeight / 55) < hiyoko.get(b).getX() + hiyokoWidth;
    }

    /*
     * Y軸方向にひよことにわとりが衝突しているかチェック
     */
    public boolean checkCollisionY(int b) {
        return niwatori.getY() + niwatoriHeight - (int) (gameFrameHeight / 55) > hiyoko.get(b).getY() && niwatori.getY() + (int) (gameFrameHeight / 55) < hiyoko.get(b).getY() + hiyokoHeight;
    }

    /*
     * にわとりがジャンプ中かどうかを確かめる
     */
    public boolean isNiwatoriJumping() {
        return niwatoriY < shokiY && niwatoriY >= 0;
    }

    /*
     * スコアを1足してviewに表示する
     */
    public void addScore() {
        score++;
        scoreText.setText("SCORE:" + score);
    }

    /*
     * 新しいひよこの初期化
     */
    public void newHiyoko(int i) {
        hiyoko.add(new ImageView(this));
        hiyoko.get(i).setImageResource(R.drawable.hiyoko);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (hiyokoWidth), (int) (hiyokoHeight));
        gameScreen.addView(hiyoko.get(i), params);
    }


    /*
     * 各SNSでのシェア
     */
    public void twitterShare(View v) {
        if (isShareAppInstall(TWITTER_ID)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setPackage(sharePackages[TWITTER_ID]);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_TEXT, "スコアは" + score + "点でした。　#Hiyocollect #ひよこれくと");
            startActivity(intent);
        } else {
            shareAppDl(TWITTER_ID);
        }
    }

    public void lineShare(View v) {
        if (isShareAppInstall(LINE_ID)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("line://msg/text/" + "スコアは" + score + "点でした。 ひよこれくと"));
            startActivity(intent);
        } else {
            shareAppDl(LINE_ID);
        }
    }

    // アプリがインストールされているかチェック
    private Boolean isShareAppInstall(int shareId) {
        try {
            PackageManager pm = getPackageManager();
            pm.getApplicationInfo(sharePackages[shareId], PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // アプリが無かったのでGooglePalyに飛ばす
    private void shareAppDl(int shareId) {
        Uri uri = Uri.parse("market://details?id=" + sharePackages[shareId]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gameBGM.release();
        kokekokko.release();
        hiyokoGetEffect.cancel();
        if (translateTimer != null) {
            translateTimer.cancel();
            translateTimer = null;
        }
        if (collisionTimer != null) {
            collisionTimer.cancel();
            collisionTimer = null;
        }
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }

}
