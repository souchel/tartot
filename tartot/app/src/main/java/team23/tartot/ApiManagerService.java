package team23.tartot;

import android.app.Service;
import android.content.Intent;
import android.icu.lang.UCharacter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import team23.tartot.core.Announces;
import team23.tartot.core.Bid;
import team23.tartot.core.Card;
import team23.tartot.core.iPlayer;

import static android.content.ContentValues.TAG;

public class ApiManagerService extends Service {
    //some constants
    private static final String API_LC = "ConnService_lifecycle";
    private static final String CONTAG = "ConnService";
    //request codes
    int RC_SIGN_IN = 23;
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_SELECT_PLAYERS = 9006; //request code for external invitation activity
    private static final int RC_INVITATION_INBOX = 9008;


    private GoogleSignInAccount userAccount;
    private com.google.android.gms.games.Player googlePlayer;
    private RealTimeMultiplayerClient rtmc = null;
    private String playerId="-1";
    private RoomConfig currentRoomConfig = null;
    private Room mCurrentRoom = null; //the room we belong to. Null otherwise
    private ArrayList<String> mBoundComponents = new ArrayList<String>();
    private boolean mInRoom = false;
    // Client used to interact with the Invitation system.
    private InvitationsClient invitationsClient = null;
    // Client used to sign in with Google APIs
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    private String mIncomingInvitationId = null;

    private String mMyParticipantId =null;
    private HashSet<Integer> pendingMessageSet = new HashSet<>(); //queue of some messages waiting to be sent


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    public ApiManagerService() {
    }

    //called at service creation
    @Override
    public void onCreate() {
        Log.i(API_LC, "onCreate");
        //this.mMyParticipantId = mCurrentRoom.getParticipantId(playerId);
        Task<GoogleSignInAccount> task = initialize();
        if (task != null) {
            task.addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                @Override
                public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                }
            });
        }
    }

    //called at each request
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }
    /**
     * Class used for the client Binder.  The Binder is used to create a connection between the service and the activities
     */
    public class LocalBinder extends Binder {
        ApiManagerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ApiManagerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String origin = intent.getStringExtra("origin");
        //keep track of the components we are bound to
        if (origin != null){
            Log.i(CONTAG, "ApiManagerService onBind : " + origin);
            mBoundComponents.add(origin);
            if (mBoundComponents.size() > 1){
                Log.i("alert", "warning : service lié à 2 composants en même temps. A-t-on quitté proprement les activités ?");
            }
            return mBinder;
        }
        else{
            return null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(CONTAG, "ApiManagerService onUnbind");
        //if we are not in game, we can kill the service
        if (mCurrentRoom == null){
            Log.i(CONTAG, "ApiManagerService onUnbind not in room : calling stopSelf ...");
            stopSelf();
        }
        return false;
    }

    @Override
    public void onDestroy(){
        Log.i(CONTAG, "ApiManagerService onDestroy");
        super.onDestroy();
    }


    public Task<GoogleSignInAccount> initialize(){
        userAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        //try to see if the user is already signed in. If null, he is not signed in.
        if (userAccount == null){
            localBroadcast(BroadcastCode.MANUAL_LOG);

            return null;
        }
        this.rtmc = Games.getRealTimeMultiplayerClient(getApplicationContext(), userAccount);
        return signInSilently();
    }

    public void update(){
        //if we are in a room, we don't update everything because we want to keep the connection
        if(mCurrentRoom != null){

        }
        //otherwise we update the accounts
        else{
            initialize();
        }
        return ;
    }

    //tries to log in silently and automatically without prompt. If it succeeds, it connects to the google game account and retrieve google Player object.
    //returns a Task to notify if we have a success
    public Task<GoogleSignInAccount> signInSilently() {
        Log.i(CONTAG, "ApiManagerService.signInSilently()");
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Task<GoogleSignInAccount> silentSignInTask = signInClient.silentSignIn();

        silentSignInTask.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // The signed in account is stored in the task's result.
                    onConnected(task.getResult());

                    //invitationsClient = Games.getInvitationsClient(getApplicationContext(), userAccount);

                    // register listener so we are notified if we receive an invitation to play
                    // while we are in the game
                    invitationsClient.registerInvitationCallback(invitationCallback);
                } else {


                    //automatic log in failed.
                    localBroadcast(BroadcastCode.MANUAL_LOG);
                }
            }
        });
        return silentSignInTask;
    }

    //used to push actions to the activities (on events coming from the network for example)
    private void localBroadcast(BroadcastCode value){
        Intent intent = new Intent();
        intent.setAction("apiManagerService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void localBroadcast(BroadcastCode value, Intent intent) {
        intent.setAction("apiManagerService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.i(CONTAG, "APIManagerService.onConnected()");
        userAccount = googleSignInAccount;
        // update the clients
        rtmc = Games.getRealTimeMultiplayerClient(getApplicationContext(), googleSignInAccount);
        invitationsClient = Games.getInvitationsClient(getApplicationContext(), googleSignInAccount);

        // get the playerId from the PlayersClient
        PlayersClient playersClient = Games.getPlayersClient(getApplicationContext(), googleSignInAccount);
        playersClient.getCurrentPlayer()
                .addOnSuccessListener(new OnSuccessListener<Player>() {
                    @Override
                    public void onSuccess(com.google.android.gms.games.Player player) {
                        googlePlayer = player;
                        playerId = player.getPlayerId();
                        localBroadcast(BroadcastCode.CONNECTED_TO_GOOGLE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "problem with player id");
                    }
                });



        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        invitationsClient.registerInvitationCallback(invitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(getApplicationContext(), googleSignInAccount);
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "There was a problem getting the activation hint!");
                    }
                });
    }

    //returns if an invitation is pending
    public boolean isInvitationPending(){
        return mIncomingInvitationId != null;
    }

    public Room getCurrentRoom(){
        return mCurrentRoom;
    }
    public boolean isConnected(){
        return rtmc != null;
    }

    public String getPLayerName(){
        if(googlePlayer == null){
            return "";
        }
        return googlePlayer.getDisplayName();
    }
    public void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getApplicationContext(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                    }
                });
    }

    public void unregisterListeners() {
        if (invitationsClient != null) {
            invitationsClient.unregisterInvitationCallback(invitationCallback);
        }
    }

    // Accept the given invitation.
    public void acceptInviteToRoom(String invitationId) {
        // accept the invitation

        currentRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .build();

        //TODO: wait screen
        //switchToScreen(R.id.screen_wait);
        localBroadcast(BroadcastCode.KEEP_SCREEN_ON);
        rtmc.join(currentRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
    }

    //called after the player pick some players to invite in the invitation activity
    public void startRoomAfterPlayerPickup(ArrayList<String> invitees, int minAutoPlayers, int maxAutoPlayers){
        // Create the room configuration.
        RoomConfig.Builder roomBuilder = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .addPlayersToInvite(invitees);
        if (minAutoPlayers > 0) {
            roomBuilder.setAutoMatchCriteria(
                    RoomConfig.createAutoMatchCriteria(minAutoPlayers, maxAutoPlayers, 0));
        }

        // Save the roomConfig so we can use it if we call leave().
        currentRoomConfig = roomBuilder.build();
        Games.getRealTimeMultiplayerClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .create(currentRoomConfig);
    }

    public void startRoomFromInvitation(Invitation invitation){
        RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback    (mRoomStatusCallbackHandler)
                .setInvitationIdToAccept(invitation.getInvitationId());
        currentRoomConfig = builder.build();
        Task<Void> joinTask = Games.getRealTimeMultiplayerClient(getApplicationContext(),
                GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .join(currentRoomConfig);

        // prevent screen from sleeping during handshake
        localBroadcast(BroadcastCode.KEEP_SCREEN_ON);

        joinTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                            exception.getMessage();
                }

            }
        });
    }

    public Intent getSignInIntent(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        return signInClient.getSignInIntent();
    }

    /**
     * returns null if signin successfull, StatusErrorMessage otherwise
     */
    public String onSigninReturn(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            return(account.getDisplayName());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            return("marche pas");
        }

    }

    public void  sendDeck(team23.tartot.core.Player player){
        return;
    }
    public void announce(List<Announces> a, team23.tartot.core.Player player){
        return;
    }

    public void bid(Bid b){
        return;
    }
    public void createLobby() {
        return;
    }

    /**
     * @return true if we are in a room
     */
    public boolean inRoom(){
        return mCurrentRoom != null;
    }
    /**
     * Callbacks to handle errors during the creation of a room.
     * Very close to doc examples.
     */
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        /**
         * Called when the client attempts to create a real-time room
         * @param i A status code indicating the result of the operation.
         * @param room The room data that was created if successful. The room can be null if the create(RoomConfig) operation failed.
         */
        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                mCurrentRoom = room;
                Log.d(TAG, "Room " + mCurrentRoom.getRoomId()+ " created.");
                showWaitingRoom(4);

                //notify the activity
                Intent intent = new Intent();
                intent.putExtra("room_id", mCurrentRoom.getRoomId());
                localBroadcast(BroadcastCode.ROOM_CREATED, intent);
            } else {
                Log.w(TAG, GamesCallbackStatusCodes.getStatusCodeString(i));
            }
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                mCurrentRoom = room;
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
                Log.i("debug", "currentRoomBeforeWaitingRoom : " + mCurrentRoom);
                showWaitingRoom(4);

                //notify the activity
                Intent intent = new Intent();
                intent.putExtra("room_id", room.getRoomId());
                localBroadcast(BroadcastCode.ROOM_JOINED, intent);

            } else {
                Log.w(TAG, "Error joining room:");
            }
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            if (i == GamesCallbackStatusCodes.INTERNAL_ERROR){
                Log.i("info", "failed to leave the room");
            }
            else {

                Log.i("info", "Room left");
                mCurrentRoom = null;
                currentRoomConfig = null;

                //notify the activity
                localBroadcast(BroadcastCode.ROOM_LEFT);
            }
        }

        /**
         * Called when all the participants in a real-time room are fully connected.
         * This gets called once all invitations are accepted and any necessary automatching has been completed
         * @param i status code
         * @param room
         */
        @Override
        public void onRoomConnected(int i, @Nullable Room room) {
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " connected.");

                //notify the activity
                localBroadcast(BroadcastCode.ROOM_CONNECTED);
            } else {
                Log.w(TAG, "Error connecting to room: " + i);
            }
        }
    };

    /**
     * Callbacks to handle events related to the room (players joining, leaving...)
     *
     */
    private RoomStatusUpdateCallback mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            // Update the UI status since we are in the process of connecting to a specific room.
            Log.i(CONTAG, "onRoomConnecting");
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            // Update the UI status since we are in the process of matching other players.
            Log.i(CONTAG, "onRoomAutoMatching");
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            // Update the UI status since we are in the process of matching other players.
            Log.i(CONTAG, "onPeerInvitedToRoom");

        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            // Peer declined invitation, see if game should be canceled
            Log.i(CONTAG, "onPeerDeclined");

        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            // Update UI status indicating new players have joined!
            Log.i(CONTAG, "onPeerJoined");

        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            // Peer left, see if game should be canceled.
            Log.i(CONTAG, "onPeerLeft");

        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            // Connected to room, record the room Id.
            Log.i(CONTAG, "onConnectedToRoom");
            mCurrentRoom = room;
            Games.getPlayersClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                    .getCurrentPlayerId().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String playerId) {
                    mMyParticipantId = mCurrentRoom.getParticipantId(playerId);
                }
            });


        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.i(CONTAG, "onDisconnectedFromRoom");
            // This usually happens due to a network error, leave the game.
            Games.getRealTimeMultiplayerClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                    .leave(currentRoomConfig, room.getRoomId());
            // show error message and return to main screen
            mCurrentRoom = null;
            currentRoomConfig = null;
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.i(CONTAG, "onPeerConnected");

        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.i(CONTAG, "onPeersDisconnected");

        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            // Update status due to new peer to peer connection.
            Log.i(CONTAG, "onP2PConnected");

        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            // Update status due to  peer to peer connection being disconnected.
            Log.i(CONTAG, "onP2PDisconnected");

        }
    };
    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby() {
        Log.i("info", "in leaveLobby");
        if (mCurrentRoom != null){
            Log.i("info", "yes");
            //the callback is in charge of setting the mCurrentRoom to null
            rtmc.leave(currentRoomConfig, mCurrentRoom.getRoomId());
        }
        //done in the leave method
        //mCurrentRoom = null;
        //currentRoomConfig = null;
        //roomID = null;
    }
    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.

    public void showWaitingRoom(int maxPlayersToStartGame) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        if(mCurrentRoom != null){
            rtmc.getWaitingRoomIntent(mCurrentRoom, maxPlayersToStartGame)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            // ask the activity the display the waiting room ui (send the google intent)
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.putExtra("intent", intent);
                            localBroadcast(BroadcastCode.SHOW_WAITING_ROOM, broadcastIntent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleException(e, "creation failed");
                        }
                    });
        }
        else{
            Log.i(CONTAG, "dans aucune room");
        }
    }


    public void startQuickGame(int nbOfPlayers) {

        //we want to match n-1 other players
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(nbOfPlayers-1, nbOfPlayers-1, 0);
        //reset the object
        currentRoomConfig = null;
        currentRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();


        // Create the room
        Task<Void> roomCreation = rtmc.create(currentRoomConfig);

        roomCreation.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Log.i(CONTAG, "success on creating room");
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i(CONTAG, "exception occured during room creation " +
                            exception.getMessage());
                }

            }
        });
    }

    //s'aider des exemple pour coder le handleException
    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }
        //TODO : switch case des status
    }

    private InvitationCallback invitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.getInvitationId();

            //call the activity to display that we have an invitation
            Intent intent = new Intent();
            intent.putExtra("invitation_id", mIncomingInvitationId);
            intent.putExtra("invitation", invitation);
            localBroadcast(BroadcastCode.INVITATION_RECEIVED, intent);
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                //hide pop ups in the activity
                localBroadcast(BroadcastCode.INVITATION_REMOVED);
            }
        }
    };


    public void getInvitePlayersIntent(){
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Games.getRealTimeMultiplayerClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .getSelectOpponentsIntent(1, 3, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        //ask the activity to display the player picker ui
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.putExtra("intent", intent);
                        localBroadcast(BroadcastCode.SHOW_PLAYER_PICKER, broadcastIntent);
                    }
                });
    }


    /**
     * Invite the player in parameter in the lobby occupied by us.
     * @param player the player to invite
     */
    public void inviteFriend(iPlayer player) {}

    /**
     * Start a new game with the players in the lobby.
     */
    public void startGame() {}

    /**
     * Leave the current game and notify the other players
     */
    public void leaveGame() {}

    /**
     * callback to treat incomming data when in game (card played, announce, ...)
     * @param data
     */
    public void onInGameDataReceive(Object data) {}

    /**
     * callback to treat incomming data when in lobby (player left the lobby, game starts, ...)
     */
    public void onInLobbyDataReceive() {}


    /**
     * deal the cards to the other players.
     * Raise an exception if the player is not in game.
     * No other verification is done.
     * @param destination: the player we send the cards to
     * @param cards: the cards delt
     */
    public void dealCards(team23.tartot.core.Player destination, Card[] cards) {}

    /**
     * notify other players the card we chosed to play
     * @param card
     */
    public void playCard(Card card) {}

    /**
     * Inform other players of an announce. Raise an exception if we are not in game
     * @param announce: the announce to broadcast
     */
    public void announce(Announces announce) {}

    /**
     * getter for the general infos of the players in the room
     * structure : for each player, a HashMap contains the keys :
     * username, status, participantId,
     * @return the array of the HashMaps
     */
    public ArrayList<HashMap<String, String>> getPlayersInRoomInfos(){
        ArrayList<HashMap<String,String>> ret = new ArrayList<>();
        if (mCurrentRoom == null || mCurrentRoom.getStatus() != Room.ROOM_STATUS_ACTIVE){
            //for testing purpose only
            for (int i=0 ; i<4 ; i++) {
                HashMap hm = new HashMap();
                hm.put("username", "joueur" + i);
                hm.put("status", "1");
                hm.put("participantId", "" + i);
                ret.add(hm);
            }
            return ret;

            //TODO : return null in the final version
            //return null;
        }
        else{
            Log.i(CONTAG, "getPlayersInRoom" + mCurrentRoom + " " + mCurrentRoom.getStatus());
            ArrayList<String> ids = mCurrentRoom.getParticipantIds();
            for (int i=0 ; i < ids.size() ; i++){
                Participant p = mCurrentRoom.getParticipant(ids.get(i));
                String[] couple = {p.getDisplayName(), p.getStatus() + ""};
                HashMap hm = new HashMap();
                hm.put("username", p.getDisplayName());
                hm.put("status", p.getStatus() + "");
                hm.put("participantId", "" + p.getParticipantId());
                ret.add(hm);
            }
            return ret;
        }
    }

    public int getMyParticipantId(){
        //TODO: supprimer ça quand on sera plus en tests
        if (mCurrentRoom == null) {
            return 0;
        }
        return Integer.parseInt(mCurrentRoom.getParticipantId(playerId));

    }

    public String[] getActivePlayersInRoom(){
        if (mCurrentRoom == null || mCurrentRoom.getStatus() != Room.ROOM_STATUS_ACTIVE){
            //TODO: retirer ceci quand on aura fini les tests
            return new String[]{"joueur1", "joueur2", "joueur3", "joueur4"};
            //return null;
        }
        else{
            Log.i(CONTAG, "getPlayersInRoom" + mCurrentRoom + " " + mCurrentRoom.getStatus());
            ArrayList<String> ids = mCurrentRoom.getParticipantIds();
            int count = 0;
            for (int i=0 ; i < ids.size() ; i++){
                Participant p = mCurrentRoom.getParticipant(ids.get(i));
                if (p.getStatus() == Participant.STATUS_JOINED) {
                    count++;
                }
            }
            String[] ret = new String[count];
            for (int i=0 ; i < ids.size() ; i++){
                Participant p = mCurrentRoom.getParticipant(ids.get(i));
                if (p.getStatus() == Participant.STATUS_JOINED) {
                    ret[i] = p.getDisplayName();
                }
            }

            return ret;
        }
    }

    public void logState() {
        if (mCurrentRoom != null) {
            Log.i(CONTAG, "room: " + mCurrentRoom);
            Log.i(CONTAG, "status: " + mCurrentRoom.getStatus());
            ArrayList<Participant> participants = mCurrentRoom.getParticipants();
            for (Participant p : participants) {
                Log.i(CONTAG, p.isConnectedToRoom() + " " + p.getStatus() + " " + p.getDisplayName());
            }
        } else {
            Log.i(CONTAG, "in no room right now");
        }

    }

    /**
     * take a serializable object, convert it to byte array and send it realiably on the network to everyone in the room
     * @param object : the object to send.Has to be parcelable
     */
    public void sendObjectToAll(Serializable object){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sendToAllReliably(bos.toByteArray());
    }

    //////////methods taken from the tutorial.
    // send a message to all participants except us using the sendReliableMessage method
    private void sendToAllReliably(byte[] message) {
        if(mCurrentRoom == null){
            return;
        }
        for (String participantId : mCurrentRoom.getParticipantIds()) {
            if (!participantId.equals(mMyParticipantId)) {
                Task<Integer> task = Games.
                        getRealTimeMultiplayerClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                        .sendReliableMessage(message, mCurrentRoom.getRoomId(), participantId,
                                handleMessageSentCallback).addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(@NonNull Task<Integer> task) {
                                recordMessageToken(task.getResult());
                            }
                        });
                Log.i("message", "sendReliable");

            }
        }
    }


    //put the message in queue
    synchronized void recordMessageToken(int tokenId) {
        pendingMessageSet.add(tokenId);
        Log.i("message", "recordMessageToken");
    }

    //callback raised when the message with tokenId tokenId is sent
    private RealTimeMultiplayerClient.ReliableMessageSentCallback handleMessageSentCallback =
            new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                @Override
                public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientId) {
                    // handle the message being sent.
                    synchronized (this) {
                        pendingMessageSet.remove(tokenId);
                        Log.i("message", "sentCallBack.onRealTimeMessageSent");
                    }
                }
            };

    /**
     * Callbacks to handle reception of messages.
     */
    private OnRealTimeMessageReceivedListener mMessageReceivedHandler =
            new OnRealTimeMessageReceivedListener() {
                @Override
                public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
                    byte[] buf = realTimeMessage.getMessageData();
                    String sender = realTimeMessage.getSenderParticipantId();
                    Log.d(TAG, "Message received: " + buf.toString());

                    ByteArrayInputStream bis = new ByteArrayInputStream(buf);
                    ObjectInput in = null;
                    Object o=null;
                    try {
                        in = new ObjectInputStream(bis);
                        o = in.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                            Log.e("error", "ApiManagerService : IOException in onRealTimeMessageReceived");

                        }
                    }

                    //add the decode routine for each type of object we could receive !
                    if (o instanceof Card) {
                        Card c = (Card) o;
                        Log.i("DECODAGE", c.getValue() + " ");
                        Intent intent = new Intent();
                        intent.putExtra("card", c);
                        //TODO : répérer les joueurs par leur id pour éviter les conflits de noms ?
                        intent.putExtra("participantId", mCurrentRoom.getParticipantId(sender));
                        localBroadcast(BroadcastCode.CARD_RECEIVED, intent);
                    }

                }
            };

}
