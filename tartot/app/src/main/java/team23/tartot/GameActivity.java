package team23.tartot;

import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import team23.tartot.core.Player;

public class GameActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ImageButton imageButtonColor = findViewById(R.id.imageButtonColor);
        //ConstraintLayout activity_game = findViewById(R.id.activity_game);

        imageButtonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tvTest = findViewById(R.id.textViewTest);
                tvTest.setText("couleur");
                Log.i("onCLick", "carreau");
            }
        });

        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout cardFL = new FrameLayout(getApplicationContext());
                ImageView cardBackgroundIB = new ImageView(getApplicationContext());
                cardBackgroundIB.setImageResource(R.drawable.card_blank_card);
                ImageButton cardColorIB = new ImageButton(getApplicationContext());
                cardColorIB.setImageResource(R.drawable.card_color_diamonds);
                cardFL.addView(cardBackgroundIB);
                cardFL.addView(cardColorIB);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(80,120);
                cardFL.setLayoutParams(lp);
            }
        });
    }
}
