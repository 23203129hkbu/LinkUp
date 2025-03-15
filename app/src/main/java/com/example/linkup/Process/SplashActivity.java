package com.example.linkup.Process;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.R;

public class SplashActivity extends AppCompatActivity {
    // layout object
    ImageView icon;
    TextView appName, slogan;
    // animation
    Animation anim_1, anim_2, anim_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // [START gain layout (activity_splash.xml)_objects]
        icon = findViewById(R.id.splashicon);
        appName = findViewById(R.id.appname);
        slogan = findViewById(R.id.slogan);
        // [END gain]

        // [START gain animation]
        anim_1 = AnimationUtils.loadAnimation(this, R.anim.icon_animation);
        anim_2 = AnimationUtils.loadAnimation(this, R.anim.appname_animation);
        anim_3 = AnimationUtils.loadAnimation(this, R.anim.slogan_animation);
        // [END gain]

        // [START assign animation to layout object]
        icon.setAnimation(anim_1);
        appName.setAnimation(anim_2);
        slogan.setAnimation(anim_3);
        // [END assign]


        // Delay action
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update UI to Main Activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000); // 4 second
    }
}