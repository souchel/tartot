package team23.tartot;

import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import team23.tartot.core.Player;

public class GameActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
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
                //We get the layout created in xml, a LinearLayout for the player's deck
                LinearLayout playerCardLayout = findViewById(R.id.player_card_layout);

                //We create a (in the future several) FrameLayout for one Card
                FrameLayout cardFL = new FrameLayout(getApplicationContext());

                //We create an ImageView for the background
                ImageView cardBackgroundIV = new ImageView(getApplicationContext());
                cardBackgroundIV.setImageResource(R.drawable.card_blank_card);

                //We create an int for the card value (FOR THE TEST)
                Random rand = new Random();
                int n = rand.nextInt(9)+1;

                TextView cardValueUpTV = new TextView(getApplicationContext());
                cardValueUpTV.setText(Integer.toString(n));
                cardValueUpTV.setTextColor(getResources().getColor(R.color.red));
                cardValueUpTV.setPadding(3,-2,0,0);

                TextView cardValueDownTV = new TextView(getApplicationContext());
                cardValueDownTV.setText(Integer.toString(n));
                cardValueDownTV.setTextColor(getResources().getColor(R.color.red));
                cardValueDownTV.setPadding(3,-2,0,0);
                cardValueDownTV.setRotation(180);


                // an image button for the color
                ImageButton cardColorIB = new ImageButton(getApplicationContext());
                cardColorIB.setImageResource(R.drawable.card_color_diamonds);
                cardColorIB.setBackgroundColor(getResources().getColor(R.color.transparent));

                // we add the image view and button
                cardFL.addView(cardBackgroundIV);
                cardFL.addView(cardValueUpTV);
                cardFL.addView(cardValueDownTV);
                cardFL.addView(cardColorIB);

                //we set Layout Parameters
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(80,120);
                cardFL.setLayoutParams(lp);

                //we add the frameLayout to the LinearLayout
                playerCardLayout.addView(cardFL);
            }
        });
    }
}