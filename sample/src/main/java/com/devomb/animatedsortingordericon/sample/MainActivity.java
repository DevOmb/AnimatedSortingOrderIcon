package com.devomb.animatedsortingordericon.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.devomb.animatedsortingordericon.AnimatedSortingOrderIcon;

public class MainActivity extends Activity {

    private AnimatedSortingOrderIcon icon1;
    private AnimatedSortingOrderIcon icon2;
    private AnimatedSortingOrderIcon icon3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        icon1 = (AnimatedSortingOrderIcon) findViewById(R.id.icon1);
        icon2 = (AnimatedSortingOrderIcon) findViewById(R.id.icon2);
        icon3 = (AnimatedSortingOrderIcon) findViewById(R.id.icon3);

        icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon1.transform();
            }
        });
        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon2.transform();
            }
        });
        icon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon3.transform();
            }
        });
    }
}
