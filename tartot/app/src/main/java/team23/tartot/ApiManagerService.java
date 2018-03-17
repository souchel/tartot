package team23.tartot;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import team23.tartot.core.Announces;
import team23.tartot.core.Bid;
import team23.tartot.core.Card;
import team23.tartot.core.iPlayer;

import static android.content.ContentValues.TAG;

public class ApiManagerService extends Service {
    //some constants
    private static final String CONTAG = "connectionDebug";
    //request codes
    int RC_SIGN_IN = 23;
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_SELECT_PLAYERS = 9006; //request code for external invitation activity
    private static final int RC_INVITATION_INBOX = 9008;


    private GoogleSignInAccount userAccount;
    private com.google.android.gms.games.Player googlePlayer;
    private RealTimeMultiplayerClient rtmc = null;
    private String playerId;
    private RoomConfig currentRoomConfig = null;
    private Room currentRoom = null; //the room we belong to. Null otherwise
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    private String roomID = null;
    // Client used to interact with the Invitation system.
    private InvitationsClient invitationsClient = null;
    // Client used to sign in with Google APIs
    private GoogleSignInClient signInClient = null;
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    private String mIncomingInvitationId = null;

    //very bad idea apparently

    private String myParticipantId=null;
    private HashSet<Integer> pendingMessageSet = new HashSet<>(); //queue of some messages waiting to be sent


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    public ApiManagerService() {
    }

    //called at service creation
    @Override
    public void onCreate(){
        //this.myParticipantId = currentRoom.getParticipantId(playerId);
        initialize().addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                Log.i(CONTAG, "signedin silent");
            }
        });
    }

    //called at each request
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        signInClient = GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

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
        return mBinder;
    }


    public Task<GoogleSignInAccount> initialize(){
        userAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        this.rtmc = Games.getRealTimeMultiplayerClient(getApplicationContext(), userAccount);
        signInClient = GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        return signInSilently();
    }

    public void update(){
        //if we are in a room, we don't update everything because we want to keep the connection
        if(currentRoom != null){

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
        Log.i(CONTAG, "signin silent");
        Task<GoogleSignInAccount> silentSignInTask = signInClient.silentSignIn();

        silentSignInTask.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    Log.i("debug", "signInSilently(): success");
                    // The signed in account is stored in the task's result.
                    onConnected(task.getResult());

                    //invitationsClient = Games.getInvitationsClient(getApplicationContext(), userAccount);

                    // register listener so we are notified if we receive an invitation to play
                    // while we are in the game
                    invitationsClient.registerInvitationCallback(invitationCallback);
                } else {
                    Log.i("debug", "signInSilently(): failed");


                    //automatic log in failed.
                    localBroadcast("manual_log");
                }
            }
        });
        return silentSignInTask;
    }

    //used to push actions to the activities (on events coming from the network for example)
    private void localBroadcast(String value){
        Intent intent = new Intent();
        intent.setAction("apiManagerService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void localBroadcast(String value, Intent intent) {
        intent.setAction("apiManagerService");
        intent.putExtra("value", value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d("debug", "onConnected(): connected to Google APIs");

        userAccount = googleSignInAccount;
        Log.i("debug", "alloc rtmc");
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
                        Log.i("le-nom", "m "+ googlePlayer.getDisplayName());
                        localBroadcast("connected");

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
                                Log.d("debug", "onConnected: connection hint has a room invite!");
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


    public boolean isConnected(){
        return rtmc != null;
    }

    public void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getApplicationContext(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                        Log.i("info", "Déconnexion réussie");
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
        Log.d("debug", "Accepting invitation: " + invitationId);

        currentRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .build();

        //TODO: wait screen
        //switchToScreen(R.id.screen_wait);
        localBroadcast("keep_screen_on");
        rtmc.join(currentRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
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
        localBroadcast("keep_screen_on");

        joinTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Log.i("info", "success on joining room");
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i("error", "exception occurred during room joining " +
                            exception.getMessage());
                }

            }
        });
    }

    public Intent getSignInIntent(){
        return signInClient.getSignInIntent();
    }

    /**
     * returns null if signin successfull, StatusErrorMessage otherwise
     */
    public String onSigninReturn(GoogleSignInResult result){
        if (result.isSuccess()) {
            // The signed in account is stored in the result
            userAccount = result.getSignInAccount();
            onConnected(userAccount);
            return null;
        } else {
            String message = result.getStatus().getStatusMessage();
            if (message == null || message.isEmpty()) {
                return "";
            }
            else{
                return message;
            }
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
     * Callbacks to handle errors during the creation of a room.
     * Very close to doc examples.
     */
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                roomID = room.getRoomId();
                currentRoom = room;
                Log.d(TAG, "Room " + roomID + " created.");
                showWaitingRoom(4);

                //notify the activity
                Intent intent = new Intent();
                intent.putExtra("room_id", roomID);
                localBroadcast("room_created", intent);
            } else {
                Log.w(TAG, GamesCallbackStatusCodes.getStatusCodeString(i));
            }
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                currentRoom = room;
                roomID = room.getRoomId();
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
                Log.i("debug", "currentRoomBeforeWaitingRoom : " + currentRoom);
                showWaitingRoom(4);

                //notify the activity
                Intent intent = new Intent();
                intent.putExtra("room_id", roomID);
                localBroadcast("room_joined", intent);

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
                currentRoom = null;
                currentRoomConfig = null;
                roomID = null;

                //notify the activity
                localBroadcast("room_left");
            }
        }

        @Override
        public void onRoomConnected(int i, @Nullable Room room) {
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " connected.");

                //notify the activity
                localBroadcast("room_connected");
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

        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {

        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {

        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {

        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onP2PConnected(@NonNull String s) {

        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {

        }
    };
    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby() {
        Log.i("info", "in leaveLobby");
        if (currentRoom != null){
            Log.i("info", "yes");
            rtmc.leave(currentRoomConfig, roomID);
        }
        //done in the leave method
        //currentRoom = null;
        //currentRoomConfig = null;
        //roomID = null;
    }
    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.

    public void showWaitingRoom(int maxPlayersToStartGame) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        if(currentRoom != null){
            rtmc.getWaitingRoomIntent(currentRoom, maxPlayersToStartGame)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            // ask the activity the display the waiting room ui (send the google intent)
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.putExtra("intent", intent);
                            localBroadcast("show_waiting_room", broadcastIntent);

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
            localBroadcast("invitation_received", intent);
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                //hide pop ups in the activity
                localBroadcast("hide_popup");
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
                    Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

                    //TODO : on fait quoi ?
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
                        localBroadcast("show_player_picker", broadcastIntent);
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


    //////////methods taken from the tutorial.
    // send a message to all participants except us using the sendReliableMessage method
    void sendToAllReliably(byte[] message) {
        for (String participantId : currentRoom.getParticipantIds()) {
            if (!participantId.equals(myParticipantId)) {
                Task<Integer> task = Games.
                        getRealTimeMultiplayerClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                        .sendReliableMessage(message, currentRoom.getRoomId(), participantId,
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
    ///////////////

}
