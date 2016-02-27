package com.tobyrich.app.SmartPlane;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.tailortoys.app.PowerUp.R;

public class MainScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        Button btn = (Button)findViewById(R.id.bttnsolo);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainScreen.this, FullscreenActivity.class));
            }
        });
    }


   /* protected void onClickSolo(View v){
        startActivity(new Intent(MainScreen.this, FullscreenActivity.class));
    }

    protected void onClickMultiplayer(View v){

    }
    */

}
