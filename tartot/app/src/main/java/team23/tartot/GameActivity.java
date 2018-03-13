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
import android.view.Gravity;
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

import team23.tartot.core.Bid;
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
    int playersAmount = 4; //should be initialized with GameManager.players.size()

    FrameLayout.LayoutParams normalLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT);
    FrameLayout.LayoutParams halfLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT/2);



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
        initializePlayersPlacement();

        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deck.shuffle();
                hand = new ArrayList<>();
                Log.i("deck",deck.toString());

                for(int i = 0; i < NUMBER_OF_CARDS; i++) {
                    hand.add(deck.getCardList().get(i));
                }

                addCardsToDeck(hand);
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

    public void initializePlayersPlacement() {
        //TODO PLACE CORRECTLY PEOPLE.
    }


    /**
     * method called at the creation of the layout to manage the graphical components (LinearLayouts and FrameLayouts) in the game zone
     * @param playersAmount an int that corresponds to GameManager.players.size()
     */
    public void initializeGameBoard(int playersAmount) {
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);

        //If there is 4 players
        if (playersAmount == 4) {
            //we create 3 vertical LinearLayout, 1 for the card left, 1 for the cards up and down and 1 for the card right
            LinearLayout leftLL = new LinearLayout(getApplicationContext());
            leftLL.setOrientation(LinearLayout.VERTICAL);
            FrameLayout emptyLeftHalfFL = new FrameLayout(getApplicationContext());
            emptyLeftHalfFL.setLayoutParams(halfLayoutParams);
            FrameLayout cardLeft = new FrameLayout(getApplicationContext());
            cardLeft.setLayoutParams(normalLayoutParams);
            cardLeft.setBackgroundColor(getResources().getColor(R.color.highlight));
            leftLL.addView(emptyLeftHalfFL);
            leftLL.addView(cardLeft);
            leftLL.setPadding(8,0,8,0);

            LinearLayout middleLL = new LinearLayout(getApplicationContext());
            middleLL.setOrientation(LinearLayout.VERTICAL);
            FrameLayout cardUp = new FrameLayout(getApplicationContext());
            //cardUp.setPadding(0,0,0,8);
            cardUp.setLayoutParams(normalLayoutParams);
            cardUp.setBackgroundColor(getResources().getColor(R.color.highlight));
            FrameLayout cardDown = new FrameLayout(getApplicationContext());
            cardDown.setLayoutParams(normalLayoutParams);
            cardDown.setBackgroundColor(getResources().getColor(R.color.highlight));
            //cardDown.setPadding(0,8,0,0);
            middleLL.addView(cardUp);
            middleLL.addView(cardDown);

            LinearLayout rightLL = new LinearLayout(getApplicationContext());
            rightLL.setOrientation(LinearLayout.VERTICAL);
            FrameLayout emptyRightHalfLL = new FrameLayout(getApplicationContext());
            emptyRightHalfLL.setLayoutParams(halfLayoutParams);
            FrameLayout cardRight = new FrameLayout(getApplicationContext());
            cardRight.setLayoutParams(normalLayoutParams);
            cardRight.setBackgroundColor(getResources().getColor(R.color.highlight));
            rightLL.addView(emptyRightHalfLL);
            rightLL.addView(cardRight);
            rightLL.setPadding(8,0,8,0);

            middleGameZone.addView(leftLL);
            middleGameZone.addView(middleLL);
            middleGameZone.addView(rightLL);
        }
    }


    /**
     * public method to trigger when the cards are distributed.All the Cards in the player's hand are graphically created:
     * in a FrameLayout, that will be add in one of the horizontal LinearLayout, we add one ImageView (for color and background),
     * one TextView (for the value) and one Button (to make the card clickable)
     * @param hand an ArrayList of Cards.
     */
    public void addCardsToDeck (ArrayList<Card> hand) { //it could be the distribution or the addition of the dog into the player's deck
        LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
        LinearLayout cardsDownLayout = findViewById(R.id.cards_down_layout);
        cardsDownLayout.setPadding(0,0,0,-CARD_HEIGHT/2);

        for (int j = 0; j < hand.size(); j++) {
            Card currentCard = hand.get(j);
            final String value = currentCard.valueToString();
            final String suit = currentCard.getSuit().toString();

            Log.i("deck", value + " " + suit);

            //We create a (in the future several) FrameLayout for one Card
            final FrameLayout cardFL = new FrameLayout(getApplicationContext());

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
                    onCardClicked(value, suit);
                    //TODO REMOVE THE CARD FROM THE PLAYER'S HAND
                    LinearLayout ll = (LinearLayout) cardFL.getParent();
                    //ll.removeViewAt(3); //TODO FIND THE GOOD VIEW INDEX
                    //TODO MANAGE BETTER THE CARD DISPLAYING
                }
            });

            // we add the image view
            //cardFL.addView(cardBackgroundIV); optimization of the cards loading !
            cardFL.addView(cardColorIV);
            cardFL.addView(cardValueUpTV);
            //cardFL.addView(cardValueDownTV);
            cardFL.addView(cardButton);

            //we set Layout Parameters
            cardFL.setLayoutParams(normalLayoutParams);

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
        LinearLayout subLinearLayout = (LinearLayout) middleGameZone.getChildAt(1);
        FrameLayout playedCardLayout = (FrameLayout) subLinearLayout.getChildAt(1);

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

    /**
     * this protected method is to transform the suit into a color
     * @param suit a String which can be (s, h, d, c, t) and comes from the method toString() of the Card enum
     * @return the boolean color : true = red & false = black
     */
    protected boolean suitIntoColor (String suit) {
        boolean color = false; //true corresponds to red, false is black
        if (suit == "d" || suit == "h") {
            color = true;
        }
        return color;
    }

    /**
     * this private method is to resize the Bitmap and has been found here https://stackoverflow.com/questions/4837715/how-to-resize-a-bitmap-in-android
     * @param bm the Bitmap to resize
     * @param newWidth the new width of the Bitmap
     * @param newHeight the new height of the Bitmap
     * @return the resized Bitmap
     */
    private Bitmap getResizedBitmap (Bitmap bm, int newWidth, int newHeight) {
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

    /**
     * specific method for this Activity to create TextView dynamically to display card value
     * @param value String resulting of valueToString() from the class Card
     * @param color boolean which corresponds to the textColor: red if true and black if false
     * @param position boolean which corresponds to the textRotation: up if true and down if false
     * @param textSize int which corresponds to the textSize (a small one for normal cards and a big one for Trumps)
     * @return the TextView that corresponds to the value and that will be add to the FrameLayout
     */
    protected TextView createTVforValue(String value, boolean color, boolean position, int textSize) {
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


    /**
     * specific method for this Activity to create ImageView to display card color with background
     * @param suit is a String that results for the toString() of the Suit enum and is the initial letter of the suit : s, h, d, c, t
     * @return the ImageView that corresponds to the color and that will be add to the FrameLayout
     */
    protected ImageView createCardColor (String suit) {
        //we initialize the Bitmap with the image of spades
        Bitmap cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_spades);

        //we set the good image that corresponds to our suit
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

        //we resize the Bitmap with the private class and we add it to the ImageView
        Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, CARD_WIDTH, CARD_HEIGHT);
        ImageView cardColorIV = new ImageView(getApplicationContext());
        cardColorIV.setImageBitmap(cardColorResizedBP);

        return cardColorIV;
    }

    public void onTurnEnded () {
        //TODO CLEAR THE GAMEZONE
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);
        for (int i=0; i<middleGameZone.getChildCount(); i++) {
            LinearLayout subLL = (LinearLayout) middleGameZone.getChildAt(i);
            for (int j=0; j<subLL.getChildCount(); j++) {
                FrameLayout cardFrame = (FrameLayout) subLL.getChildAt(j);
                cardFrame.removeAllViews();
            }
        }
    }

    protected int findVerticalPositionByPlayer(Player player) {
        return 1;
    }

    protected int findHorizontalPositionByPlayer (Player player) {
        return 0;
    }

    /**
     * public method triggered when the player is asked to chose a bid
     * @param possibleBids ArrayList of Bids that the player may choose
     * @return the Bid chosen
     */
    public Bid onBidAsked(ArrayList<Bid> possibleBids) {
        return Bid.GUARD;
    }

    public void onShowCard(Card card, Player player) {

    }
}

