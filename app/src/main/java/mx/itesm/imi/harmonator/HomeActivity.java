package mx.itesm.imi.harmonator;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class HomeActivity extends AppCompatActivity {
    private static  long SPLASH_TIME_OUT = 4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent myintent = new Intent( HomeActivity.this, MainActivity.class);
                startActivity(myintent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
