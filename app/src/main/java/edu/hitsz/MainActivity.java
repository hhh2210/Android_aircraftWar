package edu.hitsz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.EasyGame;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new EasyGame(this));
    }
}
