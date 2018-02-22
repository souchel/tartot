package team23.tartot.network;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.util.List;

import team23.tartot.core.Announce;
import team23.tartot.core.Card;
import team23.tartot.core.Player;
import team23.tartot.core.iPlayer;

import static android.content.ContentValues.TAG;

/**
 * Created by thomas on 2/20/18.
 */

public class APIManager implements iCoreToNetwork {

    RealTimeMultiplayerClient client;

    /**
     * constructor
     * Initialize the google play RealTimeMultiplayerClient instance in order to be able to use its functions.
     */
    public APIManager(Activity mainMenuActivity){
        //we determine if the player is already signed in to the google play services

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        GoogleSignIn .getLastSignedInAccount(mainMenuActivity);

        this.client = Games.getRealTimeMultiplayerClient(mainMenuActivity, )

    }
    /**
     * creates a new empty lobby - an empty room.
     */

    RoomConfig mJoinedRoom = null;

    public void createLobby() {
        RoomConfig roomConfig = RoomConfig.builder(mRoomUpdateCallback)
                                        //.setOnMessageReceivedListener(mMessageReceivedHandler)
                                        .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                                        .build();

        mJoinedRoom = roomConfig;

        // Create the room
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .create(roomConfig);
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
                Log.d(TAG, "Room " + room.getRoomId() + " created.");
            } else {
                Log.w(TAG, "Error creating room: " + i);
            }
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
            } else {
                Log.w(TAG, "Error joining room: " + code);
            }
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            Log.d(TAG, "Left room" + s);
        }

        @Override
        public void onRoomConnected(int i, @Nullable Room room) {
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " connected.");
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
    public void leaveLobby() {}


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
    public void dealCards(Player destination, Card[] cards) {}

    /**
     * notify other players the card we chosed to play
     * @param card
     */
    public void playCard(Card card) {}

    /**
     * Inform other players of an announce. Raise an exception if we are not in game
     * @param announce: the announce to broadcast
     */
    public void announce(Announce announce) {}
}
