package com.yanbin.sticky;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yanbin.sticky.utils.Utils;
import com.yanbin.sticky.widgest.GooView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GooView view=new GooView(this);
//        setContentView(R.layout.activity_main);
        setContentView(view);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

       view.setOnDragChangedListener(new GooView.OnDragChangedListener() {
           @Override
           public void onDisappear() {
               Utils.showToast(MainActivity.this,"消失了");
           }

           @Override
           public void onReset(boolean isOutofRange) {
               Utils.showToast(MainActivity.this,"恢复了"+isOutofRange);
           }
       });
    }

}
