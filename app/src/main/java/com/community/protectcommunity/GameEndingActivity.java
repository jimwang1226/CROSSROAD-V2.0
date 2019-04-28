package com.community.protectcommunity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by jingewang on 28/3/19.
 */

public class GameEndingActivity extends Activity implements View.OnClickListener{
    //Execute when the page is created
    Button backToMainScreenButton;
    View score;
    RelativeLayout endingActivityLayout;
    private SoundPool soundPool;//declare a SoundPool
    private int soundID;//Create an audio ID for a sound

    private HomeWatcher mHomeWatcher;

    private boolean mIsBound = false;
    private MusicService_End mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService_End.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ending);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        backToMainScreenButton = (Button)findViewById(R.id.return_to_mainscreen_button_ending_activity);
        endingActivityLayout = (RelativeLayout)findViewById(R.id.ending_activity_layout);

        score = findViewById(R.id.ending_score);

        SoundUtil.initSound(this);

        //calculate the score from shared preference
        SharedPreferences sharedPref = getSharedPreferences("username_gender_choice", Context.MODE_PRIVATE);
        String question1 = sharedPref.getString("question1", null);
        String question2 = sharedPref.getString("question2", null);
        String question3 = sharedPref.getString("question3", null);
        String question4 = sharedPref.getString("question4", null);
        int finalScoreInt = 0;
        //I wrote it in a reverse order, so yes will plus 50 points
        if ("YES".equals(question1)) {
            finalScoreInt += 25;
        }
        if ("NO".equals(question2)) {
            finalScoreInt += 25;
        }
        if ("YES".equals(question3) || "NO".equals(question3)) {
            finalScoreInt += 25;
        }
        if ("YES".equals(question4)) {
            finalScoreInt += 25;
        }

        System.out.println(finalScoreInt);
        Drawable drawable;
        Resources res = getResources();
        if(finalScoreInt == 100) {
            //set the score view background to 100
            drawable = res.getDrawable(R.drawable.number_100, null);
            score.setBackground(drawable);
            drawable = res.getDrawable(R.drawable.ending_activity_100_background, null);
            endingActivityLayout.setBackground(drawable);
        }
        if(finalScoreInt == 75) {
            //set the score view background to 75
            drawable = res.getDrawable(R.drawable.number_75, null);
            score.setBackground(drawable);
            drawable = res.getDrawable(R.drawable.ending_activity_100_background, null);
            endingActivityLayout.setBackground(drawable);
        }
        if(finalScoreInt == 50) {
            //set the score view background to 50
            drawable = res.getDrawable(R.drawable.number_50, null);
            score.setBackground(drawable);
            drawable = res.getDrawable(R.drawable.ending_activity_50_background, null);
            endingActivityLayout.setBackground(drawable);
        }
        if(finalScoreInt == 25) {
            //set the score view background to 25
            drawable = res.getDrawable(R.drawable.number_25, null);
            score.setBackground(drawable);
            drawable = res.getDrawable(R.drawable.ending_activity_0_background, null);
            endingActivityLayout.setBackground(drawable);
        }
        if(finalScoreInt == 0) {
            //set the score view background to 0
            drawable = res.getDrawable(R.drawable.number_0, null);
            score.setBackground(drawable);
            drawable = res.getDrawable(R.drawable.ending_activity_0_background, null);
            endingActivityLayout.setBackground(drawable);
        }

        //set up and start animation
        ObjectAnimator scoreViewAnimOn = AnimUtil.getAnimatorOn(score, this);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scoreViewAnimOn);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animatorSet.start();
            }
        });
        backToMainScreenButton.setOnClickListener(this);


        //bind music service
        mIsBound = SoundUtil.bindMusicService(this, MusicService_End.class, Scon, mIsBound);

        //HomeWatcher Settings
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null){
                    mServ.pauseMusic();
                }
            }
        });

        mHomeWatcher.startWatch();

    }

    public void getHome(){
        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.return_to_mainscreen_button_ending_activity:
                SoundUtil.playSound(soundID);
                GameProgressUtil.setGameProgressToNull(this);
                mServ.stopMusic();
                SoundUtil.playSound(soundID);
                //might cause exception
                //mHomeWatcher.stopWatch();
                getHome();
                break;
            default:
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unbind music service
        mIsBound = SoundUtil.unbindMusicService(this, MusicService_S1.class, Scon, mIsBound);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mServ != null){
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }

    }
}
