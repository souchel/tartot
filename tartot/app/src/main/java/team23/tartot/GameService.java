package team23.tartot;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import team23.tartot.core.Announces;
import team23.tartot.core.Bid;
import team23.tartot.core.Card;
import team23.tartot.core.Deck;
import team23.tartot.core.OnGoingFold;
import team23.tartot.core.Player;
import team23.tartot.core.Points;
import team23.tartot.core.Suit;
import team23.tartot.core.Team;
import team23.tartot.core.callbackGameManager;
import team23.tartot.core.iAnnounces;
import team23.tartot.core.iBids;
import team23.tartot.core.iCard;
import team23.tartot.core.iDeck;
import team23.tartot.core.iDog;
import team23.tartot.core.iEcart;
import team23.tartot.core.iFullDeck;
import team23.tartot.core.States;
import team23.tartot.core.GameState;
import team23.tartot.network.iNetworkToCore;

public class GameService extends Service implements iNetworkToCore, callbackGameManager {

    ////////////////
    //Network code//
    ////////////////

    //TODO: Pour Guillaume, c'est pas normal que certains attributs  du binding ne soient pas utilisés
    // Binder given to clients
    private final IBinder mBinder = new LocalBinderGame();
    private boolean mInGame = false;
    private boolean mBoundToNetwork = false;
    private ApiManagerService mApiManagerService;
    private States mState; //keep track of what we are doing
    private GameState mGameState = GameState.PRE_START;

    private HashMap<String,States> mPlayersState = new HashMap<String,States>();
    private Bid mTopBid;
    public GameService() {
    }

    //called at service creation
    @Override
    public void onCreate() {
    }

    //called at each request
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //check if the game service is running
        boolean running = isServiceRunning(ApiManagerService.class);
        Log.i("debug", "GameService, APIManagerService running ? " + running);
        if(running == false){
            Log.e("GameServiceError", "le service réseau ne tourne pas");
            stopSelf();
        }

        //register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter("apiManagerService");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

        // Bind to APIManagerService
        Intent networkIntent = new Intent(this, ApiManagerService.class);
        intent.putExtra("origin", "gameService");
        Log.i("debug", "act bindService");
        bindService(networkIntent, mNetworkConnection, Context.BIND_AUTO_CREATE);

        //initialize the game state:
        setState(States.PRE_START);

        return START_NOT_STICKY;
    }

    /** Defines callbacks for service binding, passed to bindService() for the game service*/
    private ServiceConnection mNetworkConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ApiManagerService.LocalBinder binder = (ApiManagerService.LocalBinder) service;
            mApiManagerService = binder.getService();
            mBoundToNetwork = true;
            initialize();
            mApiManagerService.forwardBuffer();

            //initialize player states
            ArrayList<String> ids = mApiManagerService.getIds();
            //if we try to connect directly offline (for test purpose) ids will be null
            if (ids != null) {
                for (String id : ids) {
                    if (!mPlayersState.containsKey(id))
                        mPlayersState.put(id, States.PRE_START);
                }

                //notify all players ready to deal
                setState(States.DEAL);
                localBroadcast(BroadcastCode.READY_TO_START);
            }

        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundToNetwork = false;
        }
    };

    //used to push actions to the GameActivity (on events coming from the ApiManagerService for example)
    private void localBroadcast(BroadcastCode value){
        Intent intent = new Intent();
        intent.setAction("GameService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void localBroadcast(BroadcastCode value, Intent intent) {
        Log.d("GameService", "localBroadcast " + value.toString());
        intent.setAction("GameService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    /**
     * Class used for the client Binder.  The Binder is used to create a connection between the service and the activities
     */
    public class LocalBinderGame extends Binder {
        GameService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GameService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(!mInGame){
            stopSelf();
        }
        return false;
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

    //broadcast receiver to receive broadcast from the APIManagerService
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BroadcastCode code = (BroadcastCode) intent.getSerializableExtra("value");
            String senderId = null;
            switch (code){
                //TODO utility of localBroadcast here?
                //TODO ECART_RECEIVED need to be added
                case FULL_DECK_RECEIVED:
                    Log.i("GameServBroadcastRcv", "FULL_DECK_RECEIVED");
                    iFullDeck ifd = (iFullDeck) intent.getSerializableExtra("fulldeck");
                    Deck fullDeck = ifd.getDeck();
                    String pfd = ifd.getPlayer();
                    //we trigger only if the local player is the dealer (he will distribute)
                    if (getPlayerWithUsername(pfd).getPosition() == indexDealer){
                        onDeckReceived(fullDeck);
                    }
                    /*
                    Intent j1 = new Intent();
                    j1.putExtra("text", "fulldeck should be distributed");
                    localBroadcast(BroadcastCode.EXAMPLE, j1);
                    */
                    break;
                case DECK_RECEIVED:
                    Log.i("GameServBroadcastRcv", "DECK_RECEIVED");
                    iDeck id = (iDeck) intent.getSerializableExtra("hand");
                    ArrayList<Card> h = id.getDeck();
                    String ownerId = id.getmParticipantId();
                    if (!mMyParticipantId.equals(ownerId)) {
                        Log.i("GameService", "hand from other player ignored");
                        break;
                    }
                    String p = id.getPlayer();
                    //update the deck in the Player attribute
                    onCardsDealt(h, p);
                    setState(States.REVEAL_DOG);
                    break;
                case BID_RECEIVED:
                    Log.i("GameServBroadcastRcv", "BID_RECEIVED");
                    Bid b = (Bid) intent.getSerializableExtra("bid");
                    onBid(b);
                    Intent j3 = new Intent();
                    j3.putExtra("text", "bid has been received");
                    localBroadcast(BroadcastCode.EXAMPLE, j3);
                    break;
                case DOG_RECEIVED:
                    Log.i("GameServBroadcastRcv", "DOG_RECEIVED");
                    iDog idog = (iDog) intent.getSerializableExtra("dog");
                    ArrayList<Card> dog = idog.getDog();
                    onDog(dog);
                    Intent j4 = new Intent();
                    j4.putExtra("text", "dog has been received");
                    localBroadcast(BroadcastCode.EXAMPLE, j4);
                    break;
                case ECART_RECEIVED:
                    iEcart iecart = (iEcart) intent.getSerializableExtra("ecart");
                    ArrayList<Card> ecart = iecart.getEcart();
                    onEcart(ecart);
                    Intent j0 = new Intent();
                    j0.putExtra("text", "ecart has been received");
                    localBroadcast(BroadcastCode.EXAMPLE, j0);
                    break;
                case ANNOUNCE_RECEIVED:
                    iAnnounces ia = (iAnnounces) intent.getSerializableExtra("announces");
                    ArrayList<Announces> a = ia.getAnnounces();
                    String p2 = ia.getPlayer();
                    onAnnounce(p2, a);
                    Intent j5 = new Intent();
                    j5.putExtra("text", "announces has been received for player " + p2);
                    localBroadcast(BroadcastCode.EXAMPLE, j5);
                    break;
                case CARD_RECEIVED:
                    iCard ic = (iCard) intent.getSerializableExtra("card");
                    String p3 = ic.getPlayer();
                    Card c = ic.getCard();
                    senderId = intent.getStringExtra("participantId");
                    onPlayCard(p3, c);
                    Log.i("senderId",senderId+"");
                    Intent j6 = new Intent();
                    j6.putExtra("text", "value: " + c.getValue() + ""+c.getSuit().toString() +" from " + senderId);
                    localBroadcast(BroadcastCode.EXAMPLE, j6);
                    break;
                case PLAYER_STATE_UPDATE:
                    States s = (States) intent.getSerializableExtra("state");
                    senderId = intent.getStringExtra("participantId");
                    Log.i("GameServBroadcastRcv", "PLAYER_STATE_UPDATE "+senderId+" "+s);
                    setState(s, senderId);
            }
        }
    };

    /**
     * example method to explain the sending mecanisms
     */
    public void exampleMessage(){
        //this method is called by the GameActivity
        //we do some treatment to update the state of the game (play a card, update score etc)

        //then we send info to the others players in the room by calling the ApiManagerService

        //mApiManagerService.sendToAllReliably();

        mApiManagerService.sendObjectToAll(new Card(Suit.SPADE, 5));

    }



    ////////////////////////////////////
    //game manager implementation code//
    ////////////////////////////////////
    private Player[] players;
    private Points stats ;
    int points;
    int oudlersNumber ;
    private Deck deck;
    OnGoingFold onGoingFold ;
    int indexDealer ; //index dans le tableau des players ?
    public Deck chien;
    private Bid bid;
    private ArrayList<Announces> playersAnnounces;
    private boolean[] gotAnnounces = new boolean[4];
    private boolean[] saidBid = new boolean[4];
    //position of the local player
    private int position;
    private String mMyParticipantId;

    //position of the next player to act
    private int playerTurn;
    //nb of round already over
    private int nbDone = 0;
    private Deck attackDeck ;
    private Deck defenseDeck ;


    //deprecated i think
    /*
    public void initialize(String[] usernames) {
        deck = new Deck();
        players = new Player[usernames.length];
        for (int i = 0 ; i < usernames.length; i++)
        {

            players[i] = new Player(usernames[i],i, ""+i);
            gotAnnounces[i] = false;
            saidBid[i] = false;
        }
        indexDealer = 0 ;
        chien = new Deck();
        stats = new Points(players);
        bid = null;
        attackDeck = new Deck();
        defenseDeck = new Deck();
    }*/


    /**
     * Called when we are ready to begin. i.e. when API Service is bound
     * In charge of :
     * initialize player array. TODO:Check that the player order is the same everywhere because it iterate through HashMap which is an unordered structure
     * mMyparticipantId
     * position (our position)
     * indexDealer (=first player in player array)
     *
     *public
     */
    public void initialize() {
        //{ [String username ; int status], ... }
        ArrayList<HashMap<String, String>> rawPlayersInfos = mApiManagerService.getPlayersInRoomInfos();
        deck = new Deck();
        players = new Player[rawPlayersInfos.size()];
        mMyParticipantId = mApiManagerService.getMyParticipantId();
        for (int i = 0; i < rawPlayersInfos.size(); i++) {
            HashMap<String, String> rawInfos = rawPlayersInfos.get(i);
            players[i] = new Player(rawInfos.get("username"), i, rawInfos.get("participantId"));
            //initialize the local player position
            if (rawInfos.get("participantId").equals(mMyParticipantId)) {
                position = i;
            }
            gotAnnounces[i] = false;
            saidBid[i] = false;
        }
        indexDealer = 0;
        chien = new Deck();
        stats = new Points(players);
        bid = null;

        //set up the first player to play (dealer+1)
        playerTurn = indexDealer;
        nextPlayer();
    }

    //state management functions
    /////////////////////////////
    /**
     * rename of startGame
     * deal the cards if we are the dealer
     * else wait
     */
    private void onDealState(){
        Log.i("deal state", "GameService.onDealState "+position + " " + indexDealer);

        if (position == indexDealer && mPlayersState.get(mMyParticipantId) == States.DEAL) {
            Log.i("comment","We are the dealer");

            //If it's the first turn, we shuffle
            if (nbDone == 0) {
                initializeDeck();
                deck.shuffle();
            }
            distribute(getPositionDistribution());
            setState(States.REVEAL_DOG);
        } //else need to receive card before all ie use onCardsDelt method
    }

    //called game state is BID and at each player turn
    private void onBidState() {
        if (position == playerTurn) {
            askBid();
        }
    }

    /**
     * when we reveal the dog and the caller makes his ecart *lol*
     */
    private void onRevealDogState(){
        //TODO: to code
        //condition depending on the bid
        //if small or guard
            // the dealer sends the dog to everyone
            // each player displays the dog
            // the guy who bet the most makes his ecart through the activity and keeps it
    }
    private void onAnnounceState(){
        //TODO: to code
    }

    private void onPlayingState(){
        //TODO:code
    }

    private void onRoundEndState(){
        //TODO:to code
    }


    /**
     Update player state.
     Check and update game state
     */
    private void setState(States s, String participantId){
        //update player state
        if (mApiManagerService == null){
            Log.i("debug", "can't set state "+s+", null reference on apimanagerService");
            return;
        }

        if (mMyParticipantId == null){
            Log.i("debug", "can't set state "+s+", null participant id.");
            return;
        }

        //if WE are changing state, we notify
        if (participantId.equals(mMyParticipantId)) {
            //TODO: On pourrait diffuser le state en même temps que la carte jouée ou le bid !
            //=> diminution du traffic réseau
            mApiManagerService.setState(s);
        }

        mPlayersState.put(participantId, s);

        //update game state if needed

        //check if we are ready to deal
        Set<String> ids = mPlayersState.keySet();
        boolean changeState = true;
        switch (s){
            case DEAL:
                for (String id : ids){
                    Log.i("ids",ids.toString());
                    if (mPlayersState.get(id) != States.DEAL){
                        changeState = false;
                        break;
                    }
                }
                if (changeState) {
                    mGameState = GameState.DEAL;
                    Log.i("log", "GAME_STATE DEAL");
                }
                break;

            case BID:
                for (String id : ids){
                    if (mPlayersState.get(id) != States.BID){
                        changeState = false;
                        break;
                    }
                }
                if (changeState) {
                    //if everyone is in BID state, we switch game state to bid and call the nextState
                    mGameState = GameState.BID;
                    Log.i("log", "GAME_STATE BID");
                }
                break;

            case REVEAL_DOG:
                for (String id : ids){
                    if (mPlayersState.get(id) != States.REVEAL_DOG){
                        changeState = false;
                        break;
                    }
                }
                if (changeState) {
                    //if everyone is in BID state, we switch game state to bid and call the nextState
                    mGameState = GameState.REVEAL_DOG;
                    Log.i("log", "GAME_STATE REVEAL_DOG");
                }
                break;
        }
        //TODO: continue the states
        onState();
    }

    private void setState(States s){
        setState(s,mMyParticipantId);
    }


    /**
     * base state function.
     * call the correct state function depending on the state we are in.
     * SHOULD ONLY BE CALLED IN THE SETSTATE method ! (otherwise it may be called several times
     * in a row and do strange things).
     */
    private void onState(){
        switch (mGameState){
            case PRE_START:
                return;
            case DEAL:
                onDealState();
                break;
            case BID:
                onBidState();
                break;
            case REVEAL_DOG:
                onRevealDogState();
                break;
            case PLAYING:
                onPlayingState();
                break;
            case ROUND_END:
                onRoundEndState();
                break;
            default:
                return;
        }
    }

    //all the rest that should be tidied
    /////////////////////////////////////

    public Player getSelfPlayer(){
        return players[position];
    }

    /*
    public void startGame() {
        playerTurn = indexDealer;
        nextPlayer(); //don't need this for distribution phase, so set up for first player to play
        if (position == indexDealer) {
            if (nbDone == 0) {
                initializeDeck();
                initializeGame();
            }
            distribute(getPositionDistribution());
        } //else need to receive card before all ie use onCardsDelt method
    }
    */



    public void initializeDeck()
    {
        for (Suit suit : Suit.values())
        {
            int bound ;
            if (suit ==  Suit.TRUMP)
            {
                bound = 22;
            }
            else
            {
                bound = 14;
            }
            for (int i = 1; i<=bound; i++)
            {
                deck.addCard(new Card(suit, i));
            }
        }
    }




    public void distribute(int[] indexes)
    {
        //On distribue les cartes 3 par 3 aux joueurs
        for (int i = 1; i <= 6 ; i++)
        {
            //on donne 3 cartes au joueur i modulo 4
            for (int j = 1 ; j <= 3 ; j++)
            {
                Card card = deck.removeCardByIndex(0);
                players[(i + indexDealer )% (players.length)].getHand().addCard(card);

            }
            //si c est l un des tours ou on est cense mettre dans le chien, on met une carte dans le chien
            if (alreadyInArray(indexes, i - 1))
            {
                chien.addCard(deck.removeCardByIndex(0));
            }
        }
        //sending cards to player via network
        for (Player player : players) {
            Log.i("GameService","making hand for " + player.getUsername() + player.getParticipantId());
            sendDeck(player.getHand().getCardList(), player.getUsername(), player.getParticipantId());
            sendDog(chien.getCardList());
        }
        addCardsActivity(players[position].getHand().getCardList());
    }

    public int[] getPositionDistribution() {
        //on cherche a savoir a quel moment on va mettre des cartes dans le chien
        //On cree une liste de moments qui ne peuvent pas exister

        //Partie qui sert � rien
        int[] indexes = {25,25,25,25,25,25};
        Random rndGenerator = new Random();
        //on pioche 6 entiers entre 0 et 22 sachant qu'on distribue 3 fois 24 cartes mais qu on ne
        //peut pas mettre la derniere carte dans le chien
        for (int j = 0 ; j < 6 ; j ++)
        {
            int index = rndGenerator.nextInt(23);
            //On pioche jusqu a avoir un index different de ceux deja dans la liste
            while (alreadyInArray(indexes, index))
                index = rndGenerator.nextInt(23);
            indexes[j] = index ;
        }
        return indexes;
    }






    @Override
    public void askBid() {
        Intent possibleBids = new Intent();
        ArrayList<Bid> bids = getPossibleBids();
        possibleBids.putExtra("bids", new iBids(bids));
        localBroadcast(BroadcastCode.ASK_BID, possibleBids);
    }

    //called by the activity when the local player choses
    public void BidChosen(Bid b){

        if (checkBid(b)){
            bid = b;
        }
        else
            bid=Bid.PASS;
        sendBid(b);
        setState(States.REVEAL_DOG);
        //checkBidProgress();

    }
    private void checkBidProgress() {
        boolean check = true;
        for (boolean pos : saidBid) {
            if (!pos) {
                check = false;
            }
        }
        if (check) {
            if (bid == bid.PASS){
                setDogActivity();
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                prepareNextRound();
                hideActivity();
                startRoundActivity(false, -1, null);
                //TODO faire compter la pass dans les stats
            } else {
                startRoundActivity(true, bid.getPlayerPosition(), bid);
                if (bid == bid.SMALL || bid == bid.GUARD) {
                    setDogActivity();
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (position == bid.getPlayerPosition()){
                    addCardsActivity(chien.getCardList());
                }
                for (Player player : players) {
                    if (player.getPosition() == bid.getPlayerPosition()) {
                        player.setTeam(Team.ATTACK);
                        for (Card card : chien.getCardList()) {
                            player.getHand().addCard(card);
                        }
                    } else player.setTeam(Team.DEFENSE);
                }
                hideActivity();
                //TODO demander au joueur de faire son ecart via l activite correspondante
                //ecarter(cards);
            }
        }
    }
    @Override
    public void ecarter(Card[] cards)
    {
        chien.empty();
        //on verifie que les cartes dans l ecart sont valides
        if (checkEcart(cards))
        {
            for (Card card : cards)
            {
                chien.addCard(card);
            }
            sendEcart(chien.getCardList());
            startAnnounce();
        }
        else
        {
            //TODO reappeler l activite pour que le joueur choisisse qqch de correct
        }
    }

    public boolean checkEcart(Card[] cards)
    {
        if ((players.length == 4 || players.length == 3) && cards.length != 6)
        {
            return false ;
        }
        else if (players.length == 5 && cards.length != 3)
        {
            return false ;
        }
        else
        {
            for (Card card : cards)
            {
                if (card.getValue()==14)
                {
                    return false;
                }
                else if (card.getSuit() == Suit.TRUMP)
                {
                    return false;
                }
            }
        }
        return true ;
    }

    //announce phase
    private void startAnnounce() {
        //TODO on appelle l'activité qui gére ça: ArrayList<Announces> announces = ......
        //TODO c'est l'activité qui gère quelles annonces sont possibles? on devrait plutôt le faire dans le service non?
        //TODO askAnnounce(announces)
    }
    @Override
    //méthode appelée quand le choix d'annonce est fait
    public void askAnnounce(ArrayList<Announces> announces) { //TODO nom de méthode pas adpaté
        if (announces != null)
        {
            for (Announces announce : announces){
                playersAnnounces.add(announce);
            }
        }
        setAnnouncesActivity(position, announces);
        sendAnnounces(announces);
        checkAnnounceProgress();
    }
    private void checkAnnounceProgress() {
        boolean check = true;
        for (boolean pos : gotAnnounces) {
            if (!pos) {
                check = false;
            }
        }
        if (check) {
            startNextFold();
        }
    }



    //round phase
    public void startNextFold() {
        onGoingFold = new OnGoingFold();
        if (position == playerTurn) {
            //TODO appeler l activite qui fait choisir la carte a mettre au joueur
            //sendCard(c);
        }
    }

    @Override
    //TODO no need callback method anymore
    public void startNextFoldCalledBack(Card card)
    {
        if (checkCard(card, players[position]))
        {
            onGoingFold.addCard(card);
            nextPlayer();
            checkFoldComplet();
        }
        else
        {
            //TODO reappeler l activite
            //sendCard(c);
        }
    }


    public void checkFoldComplet() {
        if (onGoingFold.getCardList().size() == 4) {
            if (players[0].getHand().getCardList().size() == 0) {
                prepareNextFold();
                startPoints();
            } else {
                prepareNextFold();
                startNextFold();
            }
        } else continuFold();
    }
    public void prepareNextFold() {
        //TODO on retire pas encore une carte du paquet de celui qui a mit l'excuse mais bon �a on verra plus tard
        if (onGoingFold.getExcused() != null) {
            Team excusedTeam = onGoingFold.getExcused().getTeam();
            if (excusedTeam != onGoingFold.getWiningPlayer().getTeam()) {
                if (onGoingFold.getExcused().getTeam() == Team.ATTACK) {
                    attackDeck.addCard(new Card(Suit.TRUMP, 22));
                } else defenseDeck.addCard(new Card(Suit.TRUMP, 22));
                for (Card card : onGoingFold.getCardList()) {
                    if (card.getSuit() == Suit.TRUMP && card.getValue() == 22) {
                        onGoingFold.getCardList().remove(card);
                    }
                }
            }
        }
        Player winingPlayer = onGoingFold.getWiningPlayer();
        //TODO for 4 player only
        if (winingPlayer.getPosition() == bid.getPlayerPosition()) {
            for (Card card : onGoingFold.getCardList()) {
                attackDeck.addCard(card);
            }
        }  else {
            for (Card card : onGoingFold.getCardList()) {
                defenseDeck.addCard(card);
            }
        }
        playerTurn = winingPlayer.getPosition();
    }
    public void continuFold() {
        if (position == playerTurn) {
            //TODO appeler l activite qui fait choisir
            //sendCard(c);
        }
    }

    @Override
    //TODO no need callback method anymore?
    public void continuFoldCalledBack(Card card) {
        if (checkCard(card, players[position]))
        {
            onGoingFold.addCard(card);
            nextPlayer();
            checkFoldComplet();
        }
        else
        {
            //TODO appeler l activite de nouveau
            //sendCard(c);
        }
    }

    //count points section
    public static double oudlerNumberIntoPointsNeeded(int oudlerNumber)
    {
        switch (oudlerNumber)
        {
            case 1: return 51;
            case 2: return 41;
            case 3: return 36;
            case 0: return 56;
            default : return 0;
        }
    }
    public static boolean theAttackWins(double pointsNeeded, double pointsWon)
    {
        return pointsNeeded <= pointsWon ;
    }
    public Points getStats()
    {
        return stats ;
    }
    public void startPoints()
    {
        double pointsAttack = attackDeck.countPoints();
        int attackOudlerNumber = attackDeck.countOudlers();
        checkAnnouncesEnd();
        //on suppose que bid contient le bid gagnant et pas juste le bid local
        stats.updatePointsAndBid(bid, attackOudlerNumber, playersAnnounces, pointsAttack);
        setWinnerActivity(attackOudlerNumber, pointsAttack);
        prepareNextRound();
    }



    //prepare next round methods
    public void reinitializeForNextRound() {
        //le deck se réinitialise pas mais se partage par des méthodes network
        chien = new Deck();
        bid = null;
        playersAnnounces = null;
        gotAnnounces = new boolean[4];
        saidBid = new boolean[4];
        for (int i = 0 ; i < players.length; i++)
        {
            gotAnnounces[i] = false;
            saidBid[i] = false;
            players[i].setTeam(Team.NONE);
        }
        nbDone += 1;
        playerTurn = indexDealer;
        nextPlayer();
    }
    public void cutTheDeck(int position)
    {
		/*
		Random rndGenerator = new Random();
		int position = rndGenerator.nextInt(73)+3;
		*/


        for (int i = 1; i<=position; i++)
        {
            Card card = deck.removeCardByIndex(0);
            deck.addCard(card);
        }
    }
    public void setNextDealer(){
        if (indexDealer < 3) {
            indexDealer += 1;
        } else indexDealer = 0;
        setDealerActivity(); //to change the dealer on the view in the activity
    }
    public void prepareNextRound() {
        reinitializeForNextRound();
        if (position == indexDealer) {
            //TODO call the right methode in the activities so that the player chooses where to
            onReceivedPosition(35);
            //cut the deck
            //send the full deck to next dealer


        }
        else setNextDealer();
    }

    @Override
    public void onReceivedPosition(int position)
    {
        cutTheDeck(position);
        setNextDealer();
        for (Player player : players) {
            if (player.getPosition() == position) {
                sendFullDeck(player.getUsername());
            }
        }
    }


    //check rules section

    //return if the card can be played, unless false
    public boolean checkCard(Card card, Player player) {
        Card winingCard = onGoingFold.getWiningCard();
        Suit askedSuit = onGoingFold.getSuit();
        Suit cardSuit = card.getSuit();
        int cardValue = card.getValue();
        Suit winingSuit = winingCard.getSuit();
        int winingValue = winingCard.getValue();
        //if excuse played ok
        if (cardSuit == Suit.TRUMP && cardValue == 22) {
            return true;
        }
        //if no suit asked ok
        if (askedSuit == null) {
            return true;
            //in the case askedsuit isn't trump
        } else if (askedSuit != Suit.TRUMP) {
            //if both card have same suit ok
            if (askedSuit == cardSuit) {
                return true;
                //if not but player have asked suit, NOT ok
            } else if (player.haveSuit(askedSuit)) {
                return false;
                //if player don't have asked suit but play trump
            } else if (cardSuit == Suit.TRUMP) {
                //if value higher than wining card, ok
                if (cardValue > winingValue) {
                    return true;
                    //if wining card isn't trump, ok
                } else if (winingSuit != Suit.TRUMP) {
                    return true;
                    //if wining card is trump and higher value
                } else
                    //if player don't have higher value, ok
                    if (!player.haveHigherTrump(winingValue)) {
                        return true;
                        //if player have higher value
                    } else return false;
                //if player don't have asked suit and isn't playing trump while he have one, Not ok
            } else if (player.haveSuit(Suit.TRUMP)) {
                return false;
                //if player don't have neither asked nor trump suit, ok
            } else return true;
            //in the case asked suit is trump
            //if trump card is played with a higher value ok
        } else if (cardSuit == Suit.TRUMP && cardValue > winingValue) {
            return true;
            //if player don't have trump, ok
        } else if (!player.haveTrump()) {
            return true;
            // have trump but not higher, ok
        } else if (!player.haveHigherTrump(winingValue)) {
            return true;
            //have trump higher but don't play it
        } else if (cardValue < winingValue) {
            return false;
            //have trump card and play it
        } else return true;
    }
    //default true
    public boolean checkAnnouncesBegining(List<Announces> announces, Player player) {
        boolean res = true;
        int nbPlayers = players.length;
        for (Announces announce : announces) {
            switch (announce) {
                case MISERY:
                    if (player.haveTrump() && player.haveHead()) {
                        res = false;
                    }
                    break;
                case SIMPLE_HANDFUL:
                    switch(nbPlayers) {
                        case 3:
                            if (player.countTrump() < 13) {
                                res = false;
                            }
                        case 4:
                            if (player.countTrump() < 10) {
                                res = false;
                            }
                        case 5:
                            if (player.countTrump() < 8) {
                                res = false;
                            }
                    }
                    break;
                case DOUBLE_HANDFUL:
                    switch(nbPlayers) {
                        case 3:
                            if (player.countTrump() < 15) {
                                res = false;
                            }
                        case 4:
                            if (player.countTrump() < 13) {
                                res = false;
                            }
                        case 5:
                            if (player.countTrump() < 10) {
                                res = false;
                            }
                    }
                    break;
                case TRIPLE_HANDFUL:
                    switch(nbPlayers) {
                        case 3:
                            if (player.countTrump() < 18) {
                                res = false;
                            }
                        case 4:
                            if (player.countTrump() < 15) {
                                res = false;
                            }
                        case 5:
                            if (player.countTrump() < 13) {
                                res = false;
                            }
                    }
                    break;
            }
        }
        return res;
    }
    public void checkAnnouncesEnd() {
        //TODO all method for 4 players
        //check if attack made a slam
        int lengthDefDeck = defenseDeck.getCardList().size();
        if (lengthDefDeck == 0 || lengthDefDeck == 6){
            Announces announceToAdd = Announces.SLAM;
            announceToAdd.setTeam(Team.ATTACK);
            playersAnnounces.add(announceToAdd);
        }
        //check if defense made a slam
        int lengthAttDeck = attackDeck.getCardList().size();
        if (lengthAttDeck == 0 || lengthAttDeck == 6){
            Announces announceToAdd = Announces.SLAM;
            announceToAdd.setTeam(Team.DEFENSE);
            playersAnnounces.add(announceToAdd);
        }
        //check if the attack get the petit au bout
        ArrayList<Card> attackLastFold = new ArrayList<Card>();
        for (int i = -4; i < 0 ; i++){
            attackLastFold.add(attackDeck.getCardList().get(i));
        }
        if (checkPetit(attackLastFold)){
            Announces announceToAdd = Announces.PETIT_AU_BOUT;
            announceToAdd.setTeam(Team.ATTACK);
            playersAnnounces.add(announceToAdd);
        }
        //check if the defense get the petit au bout
        ArrayList<Card> defenseLastFold = new ArrayList<Card>();
        for (int i = -4; i < 0 ; i++){
            defenseLastFold.add(defenseDeck.getCardList().get(i));
        }
        if (checkPetit(defenseLastFold)){
            Announces announceToAdd = Announces.PETIT_AU_BOUT;
            announceToAdd.setTeam(Team.DEFENSE);
            playersAnnounces.add(announceToAdd);
        }
    }
    //method to check if the petit is in a deck
    public Boolean checkPetit(ArrayList<Card> d){
        Boolean res = false;
        for (Card card : d){
            if (card.getSuit() == Suit.TRUMP && card.getValue() == 1){
                res = true;
            }
        }
        return res;
    }
    //get the possible bid to say during the bid phase
    public ArrayList<Bid> getPossibleBids(){
        ArrayList<Bid> bids = new ArrayList<Bid>();
        for (Bid b : Bid.values()){
            if (checkBid(b)){
                bids.add(b);
            }
        }
        return bids;
    }

    //que fait cette méthode ? On dirait qu'elle vérifie quand un joueur fait son bid qu'il a le droit
    //default (if bid null?) return false
    public boolean checkBid(Bid bid) {
        switch (bid) {
            case PASS:
                return false;
            case SMALL :
                if (mTopBid == Bid.SMALL) {
                    return false;
                } break;
            case GUARD :
                if (mTopBid == Bid.SMALL || mTopBid == Bid.GUARD) {
                    return false;
                } break;
            case GUARD_WITHOUT :
                if (mTopBid == Bid.SMALL || mTopBid == Bid.GUARD || mTopBid == Bid.GUARD_WITHOUT) {
                    return false;
                } break;
            case GUARD_AGAINST :
                return false;
            default :
                return false;
        }
        return true;
    }

    //check if the next to play is supposed to be you
    public boolean checkMyTurn(int i) {
        if (position == i+1 || (position == 0 && i == 3)) {
            return true;
        } else return false;
    }





    private boolean alreadyInArray(int[] array, int number)
    //return true if number is already in the array of int called array and false if not
    {
        for (int i = 0 ; i < array.length ; i ++)
        {
            if (array[i]==number)
                return true ;
        }
        return false;
    }
    private void nextPlayer() {
        playerTurn += 1;
        if (playerTurn > players.length-1) {
            playerTurn -= players.length;
        }
    }

    //getter setter
    public Player[] getPlayers()
    {
        return players;
    }
    public Deck getDeck()
    {
        return deck ;
    }
    public String getUsernameWithPlayer(Player p){
        return p.getUsername();
    }

    //TODO: remplacer par getPlayerWithid
    public Player getPlayerWithUsername(String username){
        for (Player player : players){
            if (player.getUsername().equals(username)){
                return player;
            }
        }
        //TODO heu je suis obligé de mettre ça mais c'est sensé ne jamais arriver...
        return players[0];
    }





    //sending methods to API
    //TODO useful? only one line each time
    public void sendFullDeck(String username){
        mApiManagerService.sendObjectToAll(new iFullDeck(deck, username));
    }

    //TODO: send only to one player and not to everyone
    public void sendDeck(ArrayList<Card> d, String username, String id){
        mApiManagerService.sendObjectToAll(new iDeck(d, username, id));
    }
    public void sendBid(Bid b){
        mApiManagerService.sendObjectToAll(b);
    }
    public void sendDog(ArrayList<Card> dog){
        mApiManagerService.sendObjectToAll(dog);
    }
    public void sendEcart(ArrayList<Card> ecart){
        mApiManagerService.sendObjectToAll(ecart);
    }
    public void sendAnnounces(ArrayList<Announces> announces){
        mApiManagerService.sendObjectToAll(new iAnnounces(announces, players[position].getUsername()));
    }
    public void sendCard(Card c){
        mApiManagerService.sendObjectToAll(new iCard(c, players[position].getUsername()));
    }

    //sending to local activity
    public void setWinnerActivity(int attackOudlerNumber, double pointsAttack){
        if (theAttackWins(oudlerNumberIntoPointsNeeded(attackOudlerNumber), pointsAttack)){
            for (Player winner : players){
                if (winner.getTeam() == Team.ATTACK){
                    Intent intent = new Intent();
                    intent.putExtra("winner", winner.getPosition());
                    localBroadcast(BroadcastCode.SHOW_WINNER, intent);
                }
            }
        } else{ //TODO c'est chelou d'afficher un winner pour la défense
            if (players[0].getTeam() == Team.DEFENSE){
                Intent intent = new Intent();
                intent.putExtra("winner", 0);
                localBroadcast(BroadcastCode.SHOW_WINNER, intent);
            } else {
                Intent intent = new Intent();
                intent.putExtra("winner", 1);
                localBroadcast(BroadcastCode.SHOW_WINNER, intent);
            }
        }
    }
    public void addCardsActivity(ArrayList<Card> cards){
        Intent intent = new Intent();
        ArrayList<Card> h = players[position].getHand().getCardList();
        intent.putExtra("hand", cards);
        localBroadcast(BroadcastCode.ADD_TO_HAND, intent);
    }
    public void setAnnouncesActivity(int playerID, ArrayList<Announces> announces){
        Intent a = new Intent();
        a.putExtra("player", playerID);
        a.putExtra("announces", announces);
        localBroadcast(BroadcastCode.ANNOUNCES, a);
    }
    public void setBidActivity(Bid b){
        Intent intent = new Intent();
        intent.putExtra("player", b.getPlayerPosition());
        intent.putExtra("bid", b);
        localBroadcast(BroadcastCode.SHOW_BID, intent);
    }
    public void setDogActivity(){
        Intent dog = new Intent();
        dog.putExtra("dog", chien.getCardList());
        localBroadcast(BroadcastCode.SHOW_DOG, dog);
    }
    public void hideActivity(){
        localBroadcast(BroadcastCode.HIDE);
    }
    //TODO 2nd arg useless?
    public void startRoundActivity(Boolean start, int taker, Bid chosenBid){
        Intent intent = new Intent();
        intent.putExtra("start", start);
        intent.putExtra("taker", taker);
        intent.putExtra("bid", chosenBid);
        localBroadcast(BroadcastCode.START_ROUND, intent);
    }
    public void setDealerActivity(){
        Intent dealer = new Intent();
        dealer.putExtra("dealer", indexDealer);
        localBroadcast(BroadcastCode.SET_DEALER, dealer);
    }


    //receiving methods
    @Override
    public void onInvitationReceived() {
        // TODO Auto-generated method stub
    }
    @Override
    public void onPlayCard(String username, Card card) {
        //TODO warning onGoingFold a 4 emplacements de cartes de base, mais checkfoldcomplete considère qu'on en ajoute à chaque fois qu'on joue une carte!!!
        Player player = getPlayerWithUsername(username);
        onGoingFold.addCard(card, player);
        checkFoldComplet();
    }

    //TODO: refactor with participantId addressing
    @Override
    public void onCardsDealt(ArrayList<Card> cards, String username) {
        Player concernedPlayer = getPlayerWithUsername(username);
        for (Player player : players) {
            if (player == concernedPlayer) {
                player.setHand(cards);
            }
        }

        //if it's our cards
        if (concernedPlayer.getPosition() == position){
            addCardsActivity(players[position].getHand().getCardList());
            setState(States.BID);
        }
    }
    @Override
    public void onAnnounce(String username, ArrayList<Announces> announces) {
        Player player = getPlayerWithUsername(username);
        for (int i = 0 ; i < announces.size(); i++) {
            if (checkAnnouncesBegining(announces, player)) { //TODO vérifier que tous ces check sont utils ils devraient être fait dans l'activité normalement
                playersAnnounces.add(announces.get(i));
            }
        }
        setAnnouncesActivity(player.getPosition(), announces);
        gotAnnounces[player.getPosition()] = true;
        checkAnnounceProgress();
    }


    //called by the broadcast from APIManager when another player announce a bid
    @Override
    public void onBid(Bid newBid) {
        //TODO: check that it's the correct player turn

        //this is done externally in the setState. It would be better if done here
        //update player state
        //setState(States.REVEAL_DOG,newBid.getParticipantId());

        //check if the bid is higher than the current bid
        if (checkBid(newBid)) {
            bid = newBid;
        }
        setBidActivity(newBid);


        //TODO: je pense que la ligne qui suit est maladroite. Il faut utiliser le participantId
        //pour référencer les joueurs (les positions ne sont peut être pas définies pareil par tous
        //les joueurs.
        saidBid[newBid.getPlayerPosition()] = true;



        //mécanisme simplifier par le système de game state
        /*checkBidProgress();
        if (checkMyTurn(newBid.getPlayerPosition()) && !saidBid[position]) {
            askBid();
        }
        */
    }

    @Override
    public void onDeckReceived(Deck deck) {
        this.deck = deck;
        distribute(null);
    }
    @Override
    public void onDog(ArrayList<Card> cards){
        this.chien = new Deck(cards);
    }

    //called by APIManager by broadcasts
    public void onEcart(ArrayList<Card> cards){
        if (bid.getMultiplicant() == 6){
            for (Card card : cards){
                defenseDeck.addCard(card);
            }
        } else for (Card card : cards){
            attackDeck.addCard(card);
        }
        startAnnounce();
    }

}

