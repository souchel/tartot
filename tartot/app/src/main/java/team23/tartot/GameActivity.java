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
    final private static int CARD_WIDTH = 80;
    final private static int CARD_HEIGHT = 160;
    final private static int NUMBER_OF_CARDS = 30;

    private int cardNumber = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
                LinearLayout cardsDownLayout = findViewById(R.id.cards_down_layout);
                cardsDownLayout.setPadding(0,0,0,-60);

                //We create a (in the future several) FrameLayout for one Card
                FrameLayout cardFL = new FrameLayout(getApplicationContext());

                //WE DEAL WITH THE BACKGROUND
                Bitmap cardBGBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_border);
                Bitmap cardBGResizedBP = getResizedBitmap(cardBGBP, CARD_WIDTH, CARD_HEIGHT);
                ImageView cardBackgroundIV = new ImageView(getApplicationContext());
                cardBackgroundIV.setImageBitmap(cardBGResizedBP);


                ///WE DEAL WITH THE VALUE
                //We create an int for the card value (FOR THE TEST)
                Random rand = new Random();
                final int value = rand.nextInt(9)+1;
                final String color = "coeur";

                //WE DEAL WITH THE COLOR
                Bitmap cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_hearts);
                Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, CARD_WIDTH, CARD_HEIGHT);
                ImageView cardColorIV = new ImageView(getApplicationContext());
                cardColorIV.setImageBitmap(cardColorResizedBP);


                //We create the 2 textViews, one for the value up and one for the one down
                TextView cardValueUpTV = createTVforValue(value, true, true);
                TextView cardValueDownTV = createTVforValue(value, true, false);

                //We create a button for the action
                Button cardButton = new Button(getApplicationContext());
                cardButton.setBackgroundColor(getResources().getColor(R.color.transparent));
                cardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView testTV = findViewById(R.id.textViewTest);
                        testTV.setText(Integer.toString(value)+" de "+ color);
                    }
                });

                // we add the image view
                cardFL.addView(cardBackgroundIV);
                cardFL.addView(cardColorIV);
                cardFL.addView(cardValueUpTV);
                cardFL.addView(cardValueDownTV);
                cardFL.addView(cardButton);

                //we set Layout Parameters
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT);
                cardFL.setLayoutParams(lp);

                //we add the frameLayout to the horizontal LinearLayout depending on the number of card already displayed
                if (cardNumber < NUMBER_OF_CARDS/2) {
                    cardsDownLayout.addView(cardFL);
                } else if (cardNumber < NUMBER_OF_CARDS){
                    cardsUpLayout.addView(cardFL);
                } else {

                }

                cardNumber++;
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

