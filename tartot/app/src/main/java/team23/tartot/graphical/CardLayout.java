package team23.tartot.graphical;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import team23.tartot.R;
import team23.tartot.core.Card;

/**
 * Created by Hugo Selle on 05/08/2018.
 */

public class CardLayout extends LinearLayout {
    final private static int CARD_WIDTH = 80;
    final private static int CARD_HEIGHT = 180;
    final private static int TEXT_SIZE_NORMAL = 12;
    final private static int TEXT_SIZE_TRUMP = 8;

    FrameLayout.LayoutParams normalLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT);
    FrameLayout.LayoutParams halfLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT/2);

    private Card card;
    private String value = "13";
    private String suit = "h";

    ImageView cardColorIV = new ImageView(getContext());
    TextView cardValueUpTV = new TextView(getContext());
    TextView cardValueDownTV = new TextView(getContext());


    public CardLayout(Context context, Card card, float screenMetrixRatio, int maxCardAmount) {
        super(context);
        this.card = card;
        this.value = card.valueToString();
        this.suit = card.getSuit().toString();

        this.cardColorIV = createCardColor();

        this.cardValueUpTV = createTVforValue(true, getTextSize(suit));
        this.cardValueDownTV = createTVforValue(false, getTextSize(suit));

        normalLayoutParams = new FrameLayout.LayoutParams(Math.round(CARD_WIDTH*screenMetrixRatio), Math.round(CARD_HEIGHT*screenMetrixRatio));
        halfLayoutParams = new FrameLayout.LayoutParams(Math.round(CARD_WIDTH*screenMetrixRatio), Math.round(CARD_HEIGHT*screenMetrixRatio/2));
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
     * specific method for this Activity to create ImageView to display card color with background
     * suit is a String that results for the toString() of the Suit enum and is the initial letter of the suit : s, h, d, c, t
     * @return the ImageView that corresponds to the color and that will be add to the FrameLayout
     */
    protected ImageView createCardColor () {

        //we initialize the Bitmap with the image of spades
        Bitmap cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_color_spades);

        CharSequence contentDescription[] = new String[1];


        //we set the good image that corresponds to our suit
        if (suit == "s") {
            cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_color_spades_big);
            contentDescription[0] = "s";
        } else if (suit == "h") {
            cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_color_hearts_big);
            contentDescription[0] = "h";
        } else if (suit == "d") {
            cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_color_diamonds_big);
            contentDescription[0] = "d";
        } else if (suit == "c") {
            cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_color_clubs_big);
            contentDescription[0] = "c";
        } else if (suit == "t") {
            cardColorBP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.card_border_style);
            contentDescription[0] = "t";
        }

        //we resize the Bitmap with the private class and we add it to the ImageView
        Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, CARD_WIDTH, CARD_HEIGHT);
        ImageView cardColorIV = new ImageView(getContext());
        cardColorIV.setImageBitmap(cardColorResizedBP);

        //we set the contentDescription, that will be used to find back the card (for deletion)
        cardColorIV.setContentDescription(contentDescription[0]);
        return cardColorIV;
    }

    /**
     * this protected method is to transform the suit into a color
     * @param suit a String which can be (s, h, d, c, t) and comes from the method toString() of the Card enum
     * @return the boolean color : true = red & false = black
     */
    private boolean suitIntoColor (String suit) {
        boolean color = false; //true corresponds to red, false is black
        if (suit == "d" || suit == "h") {
            color = true;
        }
        return color;
    }

    private int getTextSize(String suit) {
        if (suit == "t") {
            return TEXT_SIZE_TRUMP;
        } else {
            return TEXT_SIZE_NORMAL;
        }
    }

    /**
     * specific method for this Activity to create TextView dynamically to display card value
     * @param position boolean which corresponds to the textRotation: up if true and down if false
     * @param textSize int which corresponds to the textSize (a small one for normal cards and a big one for Trumps)
     * @return the TextView that corresponds to the value and that will be add to the FrameLayout
     */
    protected TextView createTVforValue(boolean position, int textSize) {
        boolean color = suitIntoColor(suit);

        TextView cardValueTV = new TextView(getContext());

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

    public FrameLayout.LayoutParams getNormalLayoutParams() {
        return normalLayoutParams;
    }

    public FrameLayout.LayoutParams getHalfLayoutParams() {
        return halfLayoutParams;
    }
}
