package team23.tartot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import team23.tartot.core.Player;

public class GameActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ImageButton imageButtonColor = findViewById(R.id.imageButtonColor);
        imageButtonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tvTest = findViewById(R.id.textViewTest);
                tvTest.setText("couleur");
                Log.i("onCLick", "carreau");
            }
        });
    }
}
