package team23.tartot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We get the layout created in xml, a LinearLayout for the player's deck
                LinearLayout playerCardLayout = findViewById(R.id.player_card_layout);
                //TODO add a LinearLayout vertical in the xml and then manage the card positionning

                //We create a (in the future several) FrameLayout for one Card
                FrameLayout cardFL = new FrameLayout(getApplicationContext());

                //WE DEAL WITH THE BACKGROUND
                Bitmap cardBGBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_border);
                Bitmap cardBGResizedBP = getResizedBitmap(cardBGBP, 80, 160);
                ImageView cardBackgroundIV = new ImageView(getApplicationContext());
                cardBackgroundIV.setImageBitmap(cardBGResizedBP);


                //WE DEAL WITH THE COLOR
                Bitmap cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_diamonds);
                Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, 80, 160);
                ImageView cardColorIV = new ImageView(getApplicationContext());
                cardColorIV.setImageBitmap(cardColorResizedBP);


                ///WE DEAL WITH THE VALUE
                //We create an int for the card value (FOR THE TEST)
                Random rand = new Random();
                int n = rand.nextInt(9)+1;

                //We create the 2 textViews, one for the value up and one for the one down
                TextView cardValueUpTV = createTVforValue(n, true, true);
                TextView cardValueDownTV = createTVforValue(n, true, false);

                // we add the image view
                cardFL.addView(cardBackgroundIV);
                cardFL.addView(cardColorIV);
                cardFL.addView(cardValueUpTV);
                cardFL.addView(cardValueDownTV);
                //TODO add a transparent button in the framelayout to make the card clickable


                //we set Layout Parameters
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(80,160);
                cardFL.setLayoutParams(lp);

                //we add the frameLayout to the LinearLayout
                playerCardLayout.addView(cardFL);
            }
        });
    }

    public Bitmap getResizedBitmap (Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public TextView createTVforValue(int value, boolean color, boolean position) {
        TextView cardValueTV = new TextView(getApplicationContext());

        //we set the card value
        cardValueTV.setText(Integer.toString(value));

        //we set the value color (red or black)
        if (color == true) { //true means red, false means black
            cardValueTV.setTextColor(getResources().getColor(R.color.red));
        } else {
            cardValueTV.setTextColor(getResources().getColor(R.color.black));
        }

        //we set the value position (up or down)
        if (position == false) {//true means up, false means down
            cardValueTV.setRotation(180);
        }

        //we set padding
        cardValueTV.setPadding(6,-3,0,0);
        cardValueTV.setTextSize(10);
        return cardValueTV;
    }
}

