package com.example.nakajie.collecthiyoko;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class ResultActivity extends ActionBarActivity {

    TextView score_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        score_text = (TextView)findViewById(R.id.score_text);
        Intent intent = getIntent();
        // IntentからBundleを取り出す
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int score = bundle.getInt("スコア");
            score_text.setText("" + score + "点");
        }
    }


    public void goToStart(View v){
            finish();
            Intent intent = new Intent(ResultActivity.this, StartActivity.class);
            startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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
