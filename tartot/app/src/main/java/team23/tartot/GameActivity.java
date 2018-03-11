package team23.tartot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

import java.util.ArrayList;

import team23.tartot.core.Card;
import team23.tartot.core.Deck;
import team23.tartot.core.GameManager;
import team23.tartot.core.Player;
import team23.tartot.core.Suit;

public class GameActivity extends AppCompatActivity {
    final private static int CARD_WIDTH = 80;
    final private static int CARD_HEIGHT = 160;
    final private static int TEXT_SIZE_NORMAL = 16;
    final private static int TEXT_SIZE_TRUMP = 10;

    final private static int NUMBER_OF_CARDS = 18;
    int playersAmount = 2; //should be initialized with GameManager.players.size()

    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT);

    protected Deck deck = new Deck();
    private int cardNumber = 0;
    protected ArrayList<Card> hand = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



        initializeDeck(); //initialize the whole deck of 78 Cards
        initializeGameBoard(playersAmount); //initialize the centered zone where, where the cards played will be shown and the places where the player wil be. There is a ConstraintLayout in the xml and x FrameLayout will be created to place the played cards correctly in front of each player.

        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deck.shuffle();
                hand = new ArrayList<>();
                Log.i("deck",deck.toString());

                for(int i = 0; i < NUMBER_OF_CARDS-10; i++) {
                    hand.add(deck.getCardList().get(i));
                }

                onCardsAddedToPlayersDeck(hand);
            }
        });
    }

    //FOR TEST ONLY, WE SHOULD USE A GAMEMANAGER
    public void initializeDeck() {
        for (Suit suit : Suit.values()) {
            int bound ;
            if (suit ==  Suit.TRUMP) {
                bound = 22;
            } else {
                bound = 14;
            }

            for (int i = 1; i <= bound; i++)
            {
                deck.addCard(new Card(suit, i));
            }
        }
    }

    //At the creation of the layout to manage the placement of every graphical component
    public void initializeGameBoard(int playersAmount) {
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);

        if (playersAmount == 2 || playersAmount == 4) {
            FrameLayout cardDown = new FrameLayout(getApplicationContext());
            cardDown.setLayoutParams(lp);
            FrameLayout cardUp = new FrameLayout(getApplicationContext());
            cardUp.setLayoutParams(lp);

            //TODO USE VERTICAL LINEARLAYOUTS INSTEAD OF SHITTY CONSTRAINT LAYOUT

            middleGameZone.addView(cardDown);
            middleGameZone.addView(cardUp);
        }
    }


    public void onCardsAddedToPlayersDeck (ArrayList<Card> hand) { //it could be the distribution or the addition of the dog into the player's deck
        LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
        LinearLayout cardsDownLayout = findViewById(R.id.cards_down_layout);
        cardsDownLayout.setPadding(0,0,0,-CARD_HEIGHT/2);

        for (int j = 0; j < hand.size(); j++) {
            Card currentCard = hand.get(j);
            final String value = currentCard.valueToString();
            final String suit = currentCard.getSuit().toString();

            Log.i("deck", value + " " + suit);

            //We create a (in the future several) FrameLayout for one Card
            FrameLayout cardFL = new FrameLayout(getApplicationContext());

            //WE DEAL WITH THE BACKGROUND, deprecated: to make the card displaying faster, we merged the card background and color
            //ImageView cardBackgroundIV = createCardBackground();

            //WE DEAL WITH THE COLOR
            ImageView cardColorIV = createCardColor(suit);

            //WE DEAL WITH THE VALUE
            TextView cardValueUpTV = new TextView(getApplicationContext());
            //TextView cardValueDownTV = new TextView(getApplicationContext());
            //We create the 2 textViews, one for the value up and one for the one down
            if (suit != "t") {
                cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, TEXT_SIZE_NORMAL);
                //cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_NORMAL);
            } else if (suit == "t") {
                cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, TEXT_SIZE_TRUMP);
                //cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_TRUMP);
            }
            //We create a button for the action
            Button cardButton = new Button(getApplicationContext());
            cardButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView testTV = findViewById(R.id.textViewTest);
                    testTV.setText(value+" de "+ suit);
                    //TODO REMOVE THE CARD FROM THE PLAYER'S HAND
                    onCardClicked(value, suit);
                }
            });

            // we add the image view
            //cardFL.addView(cardBackgroundIV); optimization of the cards loading !
            cardFL.addView(cardColorIV);
            cardFL.addView(cardValueUpTV);
            //cardFL.addView(cardValueDownTV);
            cardFL.addView(cardButton);

            //we set Layout Parameters
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

    }

    public void onCardClicked(String value, String suit) {
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);

        View playedCardView = middleGameZone.getChildAt(1);
        FrameLayout playedCardLayout = (FrameLayout) playedCardView;

        //ImageView cardBackgroundIV = createCardBackground();
        ImageView cardColorIV = createCardColor(suit);
        TextView cardValueUpTV = new TextView(getApplicationContext());
        TextView cardValueDownTV = new TextView(getApplicationContext());
        //We create the 2 textViews, one for the value up and one for the one down
        if (suit != "t") {
            cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, TEXT_SIZE_NORMAL);
            cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_NORMAL);
        } else if (suit == "t") {
            cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, TEXT_SIZE_TRUMP);
            cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_TRUMP);
        }

        //playedCardLayout.addView(cardBackgroundIV);
        playedCardLayout.addView(cardColorIV);
        playedCardLayout.addView(cardValueUpTV);
        playedCardLayout.addView(cardValueDownTV);
    }

    public boolean suitIntoColor (String suit) {
        boolean color = false; //true corresponds to red, false is black
        if (suit == "d" || suit == "h") {
            color = true;
        }
        return color;
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

    public TextView createTVforValue(String value, boolean color, boolean position, int textSize) {
        TextView cardValueTV = new TextView(getApplicationContext());

        //we set the card value
        cardValueTV.setText(value);

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
        cardValueTV.setTextSize(Math.round(CARD_HEIGHT/textSize));
        if (textSize == 10) {
            cardValueTV.setTypeface(Typeface.DEFAULT_BOLD);
            //cardValueTV.setHintTextColor(getResources().getColor(R.color.highlight));

        }
        return cardValueTV;
    }

    public ImageView createCardBackground () {
        Bitmap cardBGBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_border);
        Bitmap cardBGResizedBP = getResizedBitmap(cardBGBP, CARD_WIDTH, CARD_HEIGHT);
        ImageView cardBackgroundIV = new ImageView(getApplicationContext());
        cardBackgroundIV.setImageBitmap(cardBGResizedBP);
        return cardBackgroundIV;
    }

    public ImageView createCardColor (String suit) {
        Bitmap cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_spades);
        ;
        if (suit == "s") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_spades);
        } else if (suit == "h") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_hearts);
        } else if (suit == "d") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_diamonds);
        } else if (suit == "c") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_clubs);
        } else if (suit == "t") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_border);
        }

        Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, CARD_WIDTH, CARD_HEIGHT);
        ImageView cardColorIV = new ImageView(getApplicationContext());
        cardColorIV.setImageBitmap(cardColorResizedBP);

        return cardColorIV;
    }
}

