package team23.tartot;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import team23.tartot.core.Announces;
import team23.tartot.core.Bid;
import team23.tartot.core.Card;
import team23.tartot.core.Deck;
import team23.tartot.core.Player;
import team23.tartot.core.Suit;
import team23.tartot.core.iBids;

public class GameActivity extends AppCompatActivity {
    final private static int CARD_WIDTH = 80;
    final private static int CARD_HEIGHT = 180;
    final private static int TEXT_SIZE_NORMAL = 12;
    final private static int TEXT_SIZE_TRUMP = 8;
    final private static int NUMBER_OF_CARDS = 17;
    final private static int BID_BUTTON_HEIGHT = 77;
    final private static int BID_BUTTON_WIDTH = 530;
    float screenMetrixRatio = 1f;

    boolean putOnEcart = false;

    FrameLayout.LayoutParams normalLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT);
    FrameLayout.LayoutParams halfLayoutParams = new FrameLayout.LayoutParams(CARD_WIDTH,CARD_HEIGHT/2);


    protected Deck deck = new Deck();
    private int cardNumber = 0;
    protected ArrayList<Card> hand = new ArrayList<>();
    protected Bid chosenBid = Bid.PASS;
    private GameService mGameService;
    private boolean mGameServiceBound = false;
    private boolean mGameServiceReady = false;

    //the amount of players in the room to initialize GameBoard, PlayerPositioning....
    Player myPlayer = new Player("unitialized");
    Player[] playersList = {myPlayer};
    int playersAmount = 4;

    protected void onCreate(Bundle savedInstanceState) {
        //TODO USE THIS 5 LINES (in CardView)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.i("showMetrix", String.valueOf(height) + ", "+ String.valueOf(width));
        screenMetrixRatio = (float) height / 720;
        Log.i("showMetrix", String.valueOf(screenMetrixRatio));

        normalLayoutParams = new FrameLayout.LayoutParams(Math.round(CARD_WIDTH*screenMetrixRatio), Math.round(CARD_HEIGHT*screenMetrixRatio));
        halfLayoutParams = new FrameLayout.LayoutParams(Math.round(CARD_WIDTH*screenMetrixRatio), Math.round(CARD_HEIGHT*screenMetrixRatio/2));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //we recuperate the amount of players that was connected in the MenuActivity (via ApiManagerService.getActivePlayersInRoom)
        Intent intentFromMenu = getIntent();
        Log.i("playersAmount", "playersAmountOnCreateBeforeIntent : "+String.valueOf(playersAmount));
        playersAmount = intentFromMenu.getIntExtra("playersAmount", 4);
        Log.i("playersAmount", "playersAmountOnCreateAfterIntent : "+String.valueOf(playersAmount));

        initializeDeck(); //initialize the whole deck of 78 Cards JUST FOR TESTS !
        initializeGameBoard(playersAmount); //initialize the centered zone where, where the cards played will be shown and the places where the player wil be. There is a ConstraintLayout in the xml and x FrameLayout will be created to place the played cards correctly in front of each player.
        initializeBidsLayout();

        /*/ ClickListener of a button that should create (graphically) a Card with a FrameLayout with inside it imageViews and button /*/
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deck.shuffle();
                hand = new ArrayList<>();

                for(int i = 0; i < NUMBER_OF_CARDS; i++) {
                    hand.add(deck.getCardList().get(i));
                }

                cleanDeck();

                addCardsToDeck(myPlayer, hand);
                setPutOnEcart(true);

                ArrayList<Bid> possibleBids = new ArrayList<>();
                //possibleBids.add(Bid.SMALL);
                possibleBids.add(Bid.GUARD);
                possibleBids.add(Bid.GUARD_WITHOUT);
                possibleBids.add(Bid.GUARD_AGAINST);
                onBidAsked(possibleBids);

                /*/
                ArrayList<Card> dog = new ArrayList<>();
                dog.add(new Card(Suit.TRUMP, 21));
                dog.add(new Card(Suit.TRUMP, 1));
                dog.add(new Card(Suit.TRUMP, 22));
                onShowDog(dog);
                /*/

                Announces[] announces = {Announces.MISERY};
                onAnnounces(myPlayer, announces);

            }
        });

        findViewById(R.id.show_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player p = myPlayer;
                Card c = new Card(Suit.DIAMOND, 12);
                Log.i("showC", c.toString());
                onShowCard(c,p);
                Log.i("showC", "onShowCard done");
            }
        });
    }

    public void onStart() {
        super.onStart();

        //check if the game service is running
        boolean running = isServiceRunning(GameService.class);
        Log.i("debug", "game activity, service running ? " + running);
        if(running == false){
            startGameService();
        }
        /*
        else {
            Player p1 = new Player("p1", 0, "23");
            Player p2 = new Player("p2", 1, "mamère");
            myPlayer = p1;
            playersAmount = 2;
            playersList = new Player[] {p1, p2};

        }*/

        //register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter("GameService");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

        // Bind to GameService
        Intent intent = new Intent(this, GameService.class);
        intent.putExtra("origin", "game");
        Log.i("debug", "act bindService");
        bindService(intent, mGameConnection, Context.BIND_AUTO_CREATE);

        Log.i("playersAmount", "playersAmountOnStartAfterGameService : "+String.valueOf(playersAmount));
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i("debug", "GameActivity onStop");
        //deconnexion propre
        //TODO : gérer ça et la reprise dans le GameService
        unbindService(mGameConnection);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    private void startGameService(){
        boolean running = isServiceRunning(GameService.class);
        Log.d("debug", "game activity, service running ? " + running);
        if(running == false){
            Intent intent = new Intent(this, GameService.class);
            startService(intent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //broadcast receiver to receive broadcast from the GameService
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BroadcastCode code = (BroadcastCode) intent.getSerializableExtra("value");
            Log.i("GameActivityBroadcast", code.toString());
            switch(code){
                case EXAMPLE:
                    setPlayersTextview(intent.getStringExtra("text"));
                    break;
                case READY_TO_START:
                    mGameServiceReady = true;
                    playersList = mGameService.getPlayers();
                    myPlayer = mGameService.getSelfPlayer();
                    playersAmount = playersList.length;


                    Log.i("playersAmount", "playersAmountBroadcastReceiver : "+String.valueOf(playersAmount));

                    initializePlayersPlacement();
                    //par exemple :
                    //initializeUI();
                    //ou bien
                case ASK_BID:
                    iBids ibids = (iBids) intent.getSerializableExtra("bids");
                    if (ibids != null) {
                        ArrayList<Bid> bids = ibids.getBids();
                        onBidAsked(bids);
                    }

                    break;
                case ADD_TO_HAND:
                    ArrayList<Card> cards = (ArrayList<Card>) intent.getSerializableExtra("hand");
                    addCardsToDeck(myPlayer, cards);
                    break;
            }

        }

    };

    /** Defines callbacks for service binding, passed to bindService() for the game service*/
    private ServiceConnection mGameConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GameService.LocalBinderGame binder = (GameService.LocalBinderGame) service;
            mGameService = binder.getService();
            mGameServiceBound = true;
            onConnectedToGameService();

            // Example button to explain sending mecanism
            Button logBtn = findViewById(R.id.log);
            logBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO : test button to send data
                    mGameService.exampleMessage();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mGameServiceBound = false;
        }
    };

    //called when we have successfully established a connection with the Game service
    private void onConnectedToGameService(){
        //TODO : get all info of the game to render the UI
    }


    public void setPlayersTextview(String text){
        if(!mGameServiceBound){
            Log.e("GameActivityError", "not bound to GameService");
            return;
        }
        EditText et = findViewById(R.id.connectedPlayers);
        et.setText(text);
    }

    //FOR TEST ONLY !!!
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

    /**
     * method called in the onCreate() of this Activity
     */
    protected void initializePlayersPlacement() {
        Log.i("playersAmount", "playersAmountinit : "+String.valueOf(playersAmount));

        //on parcourt tous les joueurs et on ajoute leur pseudo dans le TextView correspondant (llPlayerLeft.getChildAt(0) par exemple)
        for (int i = 0; i < playersAmount; i++) {
            Player player = playersList[i];

            int relativePos = getRelativePositionByPlayer(player);
            TextView playerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, relativePos).getChildAt(0);
            playerTV.setText(player.getUsername());


        }
    }

    /**
     * protected method used to find the linearLayout, which represents the player on the GameActvitity by 2 TextView, with its username and one for its announces
     * @param playersAmount the amount of players around the "table"
     * @param relativePosition the relative position of the player found by getRelativePositionByPlayer
     * @return
     */
    protected LinearLayout findPlayerLayoutByRelativePosition(int playersAmount, int relativePosition) {
        if (playersAmount == 2) {
            if (relativePosition == -1 || relativePosition == 1 ){
                return findViewById(R.id.llPlayerTop);
            } else if (relativePosition == 0) {
                return findViewById(R.id.llPlayerBottom);
            }
        } else if (playersAmount == 4) {
            if (relativePosition == -1 || relativePosition == 3) {
                return findViewById(R.id.llPlayerLeft);
            } else if (relativePosition == 1 || relativePosition == -3) {
                return findViewById(R.id.llPlayerRight);
            } else if (relativePosition == 2 || relativePosition == -2) {
                return findViewById(R.id.llPlayerTop);
            } else if (relativePosition == 0) {
                return findViewById(R.id.llPlayerBottom);
            }
        }
        return findViewById(R.id.llPlayerBottom);
    }

    /**
     * method called in the onCreate() to create the layout to manage the graphical components (LinearLayouts and FrameLayouts) in the game zone
     * @param playersAmount an int that corresponds to the number of ppl in the room
     */
    protected void initializeGameBoard(int playersAmount) {
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);
        LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
        cardsUpLayout.setMinimumHeight(Math.round(Math.round(CARD_HEIGHT*screenMetrixRatio) / 2));
        //TODO CHANGE THIS SHIT, CREATE THE FRAMELAYOUT WITH A FOR AND THEN, PLACE IT ONE BY ONE IN A GRIDLAYOUT ?

        /*
        for (int i=0; i<playersAmount; i++) {

            //If there is 4 players

            if (playersAmount == 4) {
                FrameLayout cardLayout = new FrameLayout(getApplicationContext());
                cardLayout.setLayoutParams(normalLayoutParams);
                cardLayout.setBackgroundColor(getResources().getColor(R.color.highlight));

                GridLayout.Spec rowSpan = GridLayout.spec(1,1);
                GridLayout.Spec colSpan = GridLayout.spec(1,1);
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(rowSpan, colSpan);

                middleGameZone.addView(cardLayout,gridParams);
                }

        }*/
        if (playersAmount == 2) {
            LinearLayout leftLL = new LinearLayout(getApplicationContext());
            leftLL.setOrientation(LinearLayout.VERTICAL);

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

            middleGameZone.addView(leftLL);
            middleGameZone.addView(middleLL);
            middleGameZone.addView(rightLL);
        }
        else if (playersAmount == 4) {


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
            leftLL.setPadding(8, 0, 8, 0);

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
            rightLL.setPadding(8, 0, 8, 0);

            middleGameZone.addView(leftLL);
            middleGameZone.addView(middleLL);
            middleGameZone.addView(rightLL);
        }
    }


    /**
     * method called in the onCreate() of this Activity
     */
    protected void initializeBidsLayout() {
        LinearLayout bidsLayout = findViewById(R.id.bids_layout);
        bidsLayout.setVisibility(View.GONE);
        for (Bid bid : Bid.values()) {
            final Bid thisBid = bid;
            Button bidButton = createButtonForBidLayout(bid);
            bidButton.setOnClickListener(new View.OnClickListener() {
                LinearLayout bidLayout = findViewById(R.id.bids_layout);
                @Override
                public void onClick(View v) {
                    chosenBid = thisBid;
                    Log.i("bid", chosenBid.toString());
                    bidLayout.setVisibility(View.GONE);
                }
            });
            bidsLayout.addView(bidButton);
            //bidsLayout.setBackground(getResources().getDrawable(R.drawable.bids_layout_shadow));
        }
    }


    /**
     * public method SHOULD BE CALLED IN THE ONCREATE() OF THIS ACTIVITY to trigger when the cards are distributed. All the Cards in the player's hand are graphically created:
     * in a FrameLayout, that will be add in one of the horizontal LinearLayout, we add one ImageView (for color and background),
     * one TextView (for the value) and one Button (to make the card clickable)
     * @param player the player
     * @param hand an ArrayList of Cards.
     */
    public void addCardsToDeck (Player player, ArrayList<Card> hand) { //it could be the distribution or the addition of the dog into the player's deck
        //TODO GAMESERVICE: need add dog to?

        if (player.isEqual(myPlayer)) {
            LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
            LinearLayout cardsDownLayout = findViewById(R.id.cards_down_layout);
            cardsDownLayout.setPadding(0, 0, 0, - Math.round( Math.round(CARD_HEIGHT *screenMetrixRatio) / 2));

            for (int j = 0; j < hand.size(); j++) {
                Card currentCard = hand.get(j);

                //We create a (in the future several) FrameLayout for one Card
                final FrameLayout cardFL = new FrameLayout(getApplicationContext());

                addCardToLayout(currentCard, cardFL, false);
                //we add the frameLayout to the horizontal LinearLayout depending on the number of card already displayed
                if (cardNumber <= Math.round(NUMBER_OF_CARDS / 2)) {
                    Log.i("dogToDeck", "card added up");
                    cardsDownLayout.addView(cardFL);
                } else if (cardNumber < NUMBER_OF_CARDS) {
                    Log.i("dogToDeck", "card added down");
                    cardsUpLayout.addView(cardFL);
                } else {
                    //We have to refresh the layouts to have the correct current count of children
                    cardsUpLayout = findViewById(R.id.cards_up_layout);
                    cardsDownLayout = findViewById(R.id.cards_down_layout);
                    Log.i("dogToDeck", "up: " + String.valueOf(cardsUpLayout.getChildCount()) + " ; down: " + String.valueOf(cardsDownLayout.getChildCount()));
                    if (cardsDownLayout.getChildCount() - cardsUpLayout.getChildCount() >= 1) {
                        cardsUpLayout.addView(cardFL);
                    } else {
                        cardsDownLayout.addView(cardFL);
                    }
                }
                cardNumber++;
            }
        }
    }

    /**
     * protected method to visually add a (un)clickable Card in a certain FrameLayout
     * @param card
     * @param fl the FrameLayout containing 3 childes: 0 = ImageView colorIV, 1 = TextView valueTV, 2 = Button button
     * @param unclickable
     */
    protected void addCardToLayout(final Card card, final FrameLayout fl, boolean unclickable) {
        final FrameLayout cardFL = fl;
        final String value = card.valueToString();
        final String suit = card.getSuit().toString();

        final Card thisCard = card;


        //WE DEAL WITH THE BACKGROUND, deprecated: to make the card displaying faster, we merged the card background and color
        //ImageView cardBackgroundIV = createCardBackground();

        //WE DEAL WITH THE COLOR
        ImageView cardColorIV = createCardColor(suit);

        //WE DEAL WITH THE VALUE
        TextView cardValueUpTV = new TextView(getApplicationContext());
        TextView cardValueDownTV = new TextView(getApplicationContext());
        //We create the 2 textViews, one for the value up and one for the one down
        if (suit != "t") {
            cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, Math.round(TEXT_SIZE_NORMAL));
            cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_NORMAL);
        } else if (suit == "t") {
            cardValueUpTV = createTVforValue(value, suitIntoColor(suit), true, Math.round(TEXT_SIZE_TRUMP));
            cardValueDownTV = createTVforValue(value, suitIntoColor(suit), false, TEXT_SIZE_TRUMP);
        }
        //We create a button for the action
        Button cardButton = new Button(getApplicationContext());
        cardButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        if (!unclickable) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView testTV = findViewById(R.id.textViewTest);
                    testTV.setText(value+" de "+ suit);

                    //Si les cartes sont à jouer dans l'écart
                    // /!\ pour la V1, on ne peut cliquer qu'une fois sur une carte pour la mettre dans l'écart. Si on s'est trompé c'est dans le cul
                    if (putOnEcart) {
                        LinearLayout dogAndEcartLayout = findViewById(R.id.dog_and_ecart_layout);
                        dogAndEcartLayout.setVisibility(View.VISIBLE);
                        ((Button) dogAndEcartLayout.getChildAt(1)).setVisibility(View.VISIBLE);
                        FrameLayout cardLayout = new FrameLayout(getApplicationContext());
                        cardLayout.setLayoutParams(normalLayoutParams);
                        //playCardInGameZone(value, suit, cardLayout);

                        //if the Card is in the DogLayout, we add in back to the game
                        if(cardFL.getParent() == findViewById(R.id.dog_and_ecart_layout)) {
                            ArrayList<Card> cardList = new ArrayList<>();
                            cardList.add(card);
                            Log.i("dogToDeck", "method called in addCardToLayout");
                            addCardsToDeck(myPlayer, cardList);

                            Log.i("dogToDeck", "end of call in addCardToLayout");

                            int index = findFLIndexInDeckByCard(card, dogAndEcartLayout);
                            if (index != -1) {
                                ((LinearLayout) dogAndEcartLayout.getChildAt(0)).removeViewAt(index);
                            }
                            //else, we want to add the card in the DogLayout
                        } else {
                            addCardToLayout(card, cardLayout, false);
                            dogAndEcartLayout.addView(cardLayout);
                            deleteCardFromDeck(card, myPlayer);
                        }



                    } else {
                        //we recuperate the FrameLayout with relative pos which is always 0 because it's the card played by us
                        //FOR TEST ONLY: WE SHOULDNT DO THAT HERE AND WE SHOULD WAIT FOR THE GO (OR NO GO) OF THE SERVICE
                        FrameLayout cardFL = getCardLayoutByRelativePosition(0);
                        playCardInGameZone(value, suit, cardFL);
                        deleteCardFromDeck(card, myPlayer);
                    }

                }
            });
        }

        // we add the image view
        //cardFL.addView(cardBackgroundIV); optimization of the cards loading !
        cardFL.addView(cardColorIV);
        cardFL.addView(cardValueUpTV);
        cardFL.addView(cardValueDownTV);
        cardFL.addView(cardButton);

        //we set Layout Parameters
        //TODO SET LAYOUT PARAMS DEPENDING ON THE AMOUNT OF CARDS INITIALLY IN THE PLAYERS' HAND
        cardFL.setLayoutParams(normalLayoutParams);
    }

    /**
     * specific method for this Activity to create ImageView to display card color with background
     * @param suit is a String that results for the toString() of the Suit enum and is the initial letter of the suit : s, h, d, c, t
     * @return the ImageView that corresponds to the color and that will be add to the FrameLayout
     */
    protected ImageView createCardColor (String suit) {
        //we initialize the Bitmap with the image of spades
        Bitmap cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_spades);

        CharSequence contentDescription[] = new String[1];


        //we set the good image that corresponds to our suit
        if (suit == "s") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_spades_big);
            contentDescription[0] = "s";
        } else if (suit == "h") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_hearts_big);
            contentDescription[0] = "h";
        } else if (suit == "d") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_diamonds_big);
            contentDescription[0] = "d";
        } else if (suit == "c") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_color_clubs_big);
            contentDescription[0] = "c";
        } else if (suit == "t") {
            cardColorBP = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.card_border);
            contentDescription[0] = "t";
        }

        //we resize the Bitmap with the private class and we add it to the ImageView
        Bitmap cardColorResizedBP = getResizedBitmap(cardColorBP, CARD_WIDTH, CARD_HEIGHT);
        ImageView cardColorIV = new ImageView(getApplicationContext());
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

    protected void setLayoutParams(FrameLayout.LayoutParams layoutParams, int width, int height) {
        layoutParams = new FrameLayout.LayoutParams(width,height);
    }


    /**
     * public method triggered when the turn is ended to show the winner and to clean the dashboard
     * @param winner
     */
    public void onTurnEnded (Player winner) {
        //TODO GAMESERVICE
        //TODO CLEAN THE MIDDLEGAMEZONE DEPENDING ON THE GAMEZONE INITIALIZATION !
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);
        for (int i=0; i<middleGameZone.getChildCount(); i++) {
            LinearLayout verticalLL = (LinearLayout) middleGameZone.getChildAt(i);
            for (int j=0; j<verticalLL.getChildCount(); j++) {
                FrameLayout cardFrame = (FrameLayout) verticalLL.getChildAt(j);
                cardFrame.removeAllViews();
            }
        }

        for (int k=0; k<playersList.length; k++) {
            TextView playerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, getRelativePositionByPlayer(playersList[k])).getChildAt(0);
            if ((playersList[k].getUsername() == winner.getUsername()) && (playersList[k].getPosition() == winner.getPosition())) {
                playerTV.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                playerTV.setTypeface(Typeface.DEFAULT);
            }
        }

    }

    /**
     * public method triggered when the player is asked to chose a bid
     * @param possibleBids ArrayList of Bids that the player may choose
     * @return the Bid chosen
     */
    public void onBidAsked(ArrayList<Bid> possibleBids) {
        //TODO GAMESERVICE
        LinearLayout bidsLayout = findViewById(R.id.bids_layout);
        //bidsLayout.removeAllViews();
        for (int i =0; i<bidsLayout.getChildCount(); i++) {
            Button bidButton = (Button) bidsLayout.getChildAt(i);
            Bid bid = getBidByButton(bidButton);

            if (possibleBids.contains(bid) || bid == Bid.PASS) {
                bidButton.setBackgroundColor(getResources().getColor(R.color.white));
                bidButton.setTextColor(getResources().getColor(R.color.black));
            } else {
                bidButton.setBackgroundColor(getResources().getColor(R.color.unchosable));
                bidButton.setTextColor(getResources().getColor(R.color.highlight));
                bidButton.setEnabled(false);
            }
        }
        bidsLayout.setVisibility(View.VISIBLE);
    }

    protected Bid getBidByButton(Button button){
        Bid bid = Bid.PASS;
        String stringBid = button.getText().toString();
        Resources res = getApplicationContext().getResources();
        if (stringBid.equals(res.getString(R.string.pass))) {
            bid = Bid.PASS;
        } else if (stringBid.equals(res.getString(R.string.small))) {
            bid = Bid.SMALL;
        } else if (stringBid.equals(res.getString(R.string.guard))) {
            bid = Bid.GUARD;
        } else if (stringBid.equals(res.getString(R.string.guard_without))) {
            bid = Bid.GUARD_WITHOUT;
        } else if (stringBid.equals(res.getString(R.string.guard_against))) {
            bid = Bid.GUARD_AGAINST;
        }
        return bid;
    }


    /**
     * protected method to create a button with his text and his LayoutParams (height and width)
     * @param bid
     * @return
     */
    protected Button createButtonForBidLayout(Bid bid) {
        Button bidButton = new Button(getApplicationContext());
        bidButton.setText(bid.toString(getApplicationContext()));
        bidButton.setLayoutParams(new FrameLayout.LayoutParams(Math.round(BID_BUTTON_WIDTH*screenMetrixRatio), Math.round(BID_BUTTON_HEIGHT*screenMetrixRatio)));
        return bidButton;
    }

    //TODO put this at the right place, what do you send me exactly when someone pass?
    //TODO answer of Hugo: when someone pass, I send you a Bid.PASS...
    public Bid getChosenBid() {
        mGameService.BidChosen(this.chosenBid);
        return this.chosenBid;
    }

    /**
     * Method called when any players have the right to play a card
     * @param card the Card to play
     * @param player the Player which plays
     */
    public void onShowCard(Card card, Player player) {
        //TODO GAMESERVICE : need a Card and a Player
        int relativePosition = getRelativePositionByPlayer(player);

        FrameLayout cardLayout = getCardLayoutByRelativePosition(relativePosition);

        String suit = card.getSuit().toString();
        String value = card.valueToString();

        playCardInGameZone(value, suit, cardLayout);
    }

    /**
     * protected method to get the relative position of a player
     * @param player the player
     * @return
     */
    protected int getRelativePositionByPlayer(Player player) {
        int myPosition = myPlayer.getPosition();

        int playerPosition = player.getPosition();
        int relativePosition = playerPosition - myPosition;

        return relativePosition;
    }

    /**
     * protected method triggered when a Card is played: directly by us when the card is Clicked (undoable in the future) and triggered by gGameService when someone else play
     * @param value String which corresponds to the value of the Card played
     * @param suit String which corresponds to the color of the Card played
     * @param playedCardLayout  FrameLayout where the Player can play his Card: found with getCardLayoutByRelativePosition(
     */
    protected void playCardInGameZone(String value, String suit, FrameLayout playedCardLayout) {

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


    protected FrameLayout getCardLayoutByRelativePosition(int pos) {
        FrameLayout cardLayout = new FrameLayout(getApplicationContext());
        LinearLayout middleGameZone = findViewById(R.id.middle_game_zone);
        if (playersAmount == 4) {
            //if the player is on my left
            if (pos == -1 || pos == 3) {
                LinearLayout subLinearLayout = (LinearLayout) middleGameZone.getChildAt(0);
                cardLayout = (FrameLayout) subLinearLayout.getChildAt(1);
            }
            //if he is in front of me
            else if (pos == -2 || pos == 2) {
                LinearLayout subLinearLayout = (LinearLayout) middleGameZone.getChildAt(1);
                cardLayout = (FrameLayout) subLinearLayout.getChildAt(0);
            }
            //if he is on my right
            else if (pos == 1 || pos == -3) {
                LinearLayout subLinearLayout = (LinearLayout) middleGameZone.getChildAt(2);
                cardLayout = (FrameLayout) subLinearLayout.getChildAt(1);
            }
            //if it's me
            else if (pos == 0) {
                LinearLayout subLinearLayout = (LinearLayout) middleGameZone.getChildAt(1);
                cardLayout = (FrameLayout) subLinearLayout.getChildAt(1);
            }
        } else if (playersAmount == 3) {
            Log.i("getCard", String.valueOf(playersAmount)+" joueurs");
        } else if (playersAmount == 5) {
            Log.i("getCard", String.valueOf(playersAmount)+" joueurs");
        }


        return cardLayout;
    }

    /**
     * protected method to find the index in the layout by the Card (we search only in cards_up_layout or cards_down_layout
     * @param card
     * @return
     */
    protected int findFLIndexInDeckByCard(Card card, LinearLayout cards_layout) {
        int cardIndex = -1;
        for (int i=0; i<cards_layout.getChildCount(); i++) {
            //we recuperate the FrameLayout of the Card
            FrameLayout cardFL = (FrameLayout) cards_layout.getChildAt(i);

            //we recuperate the color of the card from the contentDescription of the colorIV
            ImageView colorIV = (ImageView) cardFL.getChildAt(0);
            String color = colorIV.getContentDescription().toString();

            //we recuperate the value from the valueTV
            TextView valueTV = (TextView) cardFL.getChildAt(1);
            String value = valueTV.getText().toString();

            if ((card.getSuit().toString() == color) && (card.valueToString() == value)) {
                Log.i("deletecard", "trouvé à la position : " + String.valueOf(i));
                cardIndex = i;
            }
        }

        return cardIndex;
    }

    /**
     * public method to delete a Card from the deck of a player
     * @param card
     * @param player
     */
    public void deleteCardFromDeck(Card card, Player player) {
        if (player.isEqual(myPlayer)) {
            LinearLayout up = findViewById(R.id.cards_up_layout);
            int cardFLIndex = findFLIndexInDeckByCard(card, up);
            if (cardFLIndex != -1) {
                up.removeViewAt(cardFLIndex);
            } else {
                LinearLayout down = findViewById(R.id.cards_down_layout);
                cardFLIndex = findFLIndexInDeckByCard(card, down);
                if (cardFLIndex != -1) {
                    down.removeViewAt(cardFLIndex);
                }
            }
        }
    }



    protected void cleanDeck() {
        LinearLayout cardsUpLayout = findViewById(R.id.cards_up_layout);
        LinearLayout cardsDownLayout = findViewById(R.id.cards_down_layout);

        cardsUpLayout.removeAllViews();
        cardsDownLayout.removeAllViews();
    }

    /**
     * method trigerred when someone has a announce
     * @param player
     * @param announces array of announces
     */
    public void onAnnounces(Player player, Announces[] announces) {
        //TODO GAMESERVICE
        String announcStr = "";
        for (int i = 0; i<announces.length; i++) {
            announcStr = announcStr + announces[i].toString(getApplicationContext());
            if (!(i == announces.length-1)) {
                announcStr = announcStr + ", ";
            }
        }
        addStrToPlayerBATextView(player, announcStr);
    }

    /**
     * method trigerred when a player has chosen his Bid, uses the same TextView as onAnnounces
     * @param player
     * @param bid
     */
    public void onBids(Player player, Bid bid) {
        //TODO GAMESERVICE
        addStrToPlayerBATextView(player, bid.toString(getApplicationContext()));
    }

    /**
     * protected method used by onAnnounes and onBids to add a String into the second TextView of the playerLayout
     * @param player
     * @param BidOrAnnouces
     */
    protected void addStrToPlayerBATextView (Player player, String BidOrAnnouces) {
        int relativePos = getRelativePositionByPlayer(player);
        TextView playerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, relativePos).getChildAt(1);
        playerTV.setText(BidOrAnnouces);
    }

    /**
     * public method trigerred to show the dog
     * @param dog an ArrayList of (3 or 6) Cards
     */
    public void onShowDog(ArrayList<Card> dog) {
        //TODO GAMESERVICE
        LinearLayout dAndEcartLayout = findViewById(R.id.dog_and_ecart_layout);
        LinearLayout dogAndEcartLayout = (LinearLayout) dAndEcartLayout.getChildAt(0);
        dogAndEcartLayout.removeAllViews();

        for (int i=0; i<dog.size(); i++) {
            FrameLayout cardLayout = new FrameLayout(getApplicationContext());
            playCardInGameZone(dog.get(i).valueToString(), dog.get(i).getSuit().toString(), cardLayout);
            dogAndEcartLayout.addView(cardLayout);
        }

        dAndEcartLayout.getChildAt(1).setVisibility(View.GONE);
        dogAndEcartLayout.setVisibility(View.VISIBLE);
    }

    /**
     * public method to hide the dog or the ecart layout
     */
    public void onHideDogOrEcart() {
        //TODO GAMESERVICE
        LinearLayout dogAndEcartLayout = findViewById(R.id.dog_and_ecart_layout);
        dogAndEcartLayout.setVisibility(View.GONE);
    }

    /**
     * public method to trigger after everyone has bided, will initialize the GameBoard, clear bidsLayout, clear the bids
     * @param start boolean, if false: it's a pass, else it's not
     * @param taker the player who will attack (if start == false: taker == Null)
     * @param bid the bid which has been taken (if start == false: bid == Null)
     */
    public void onDoneStart(boolean start, Player taker, Bid bid) {
        //TODO GAMESERVICE
        //LinearLayout bidsLayout = findViewById(R.id.bids_layout);
        //bidsLayout.removeAllViews();
        for (int i =0; i<playersAmount; i++) {
            Player p = playersList[i];
            int relativePos = getRelativePositionByPlayer(p);
            TextView takerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, relativePos).getChildAt(1);
            takerTV.setText("");
        }
        if (start == true) {
            initializeGameBoard(playersAmount);
            int relativePos = getRelativePositionByPlayer(taker);
            TextView takerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, relativePos).getChildAt(0);
            takerTV.setText(R.string.taker + " " + taker.getUsername());
        }
    }

    /**
     * public method triggered to highlight who is the new dealer (will clear the last one) onNewDealer() should be called before onDoneStart()
     * @param dealer the player who is the dealer
     */
    public void onNewDealer(Player dealer) {
        //TODO GAMESERVICE
        initializeGameBoard(playersAmount);
        int relativePos = getRelativePositionByPlayer(dealer);
        TextView takerTV = (TextView) findPlayerLayoutByRelativePosition(playersAmount, relativePos).getChildAt(0);
        takerTV.setText(dealer.getUsername()+ " D");
    }

    /**
     * public method to trigerred when the taker is asked to make his ecart
     * @param makeEcart true if the cards has to be added into the ecart, false if the card when clicked has to shown in the gameBoard
     */
    public void setPutOnEcart(boolean makeEcart) {
        //TODO GAMESERVICE
        putOnEcart = makeEcart;
        LinearLayout ecartLayout = findViewById(R.id.dog_and_ecart_layout);
        ecartLayout.removeAllViews();
        ecartLayout.setVisibility(View.VISIBLE);
    }
}



