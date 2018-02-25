package team23.tartot;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class LobbyActivity extends AppCompatActivity {

    private RealTimeMultiplayerClient RTMClient;
    private RoomConfig mJoinedRoom = null;
    private String roomID = null;
    private RoomConfig mJoinedRoomConfig;
    private Room currentRoom = null; //the room we belong to. Null otherwise

    private static final int RC_SELECT_PLAYERS = 9006; //request code for external invitation activity
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_INVITATION_INBOX = 9008;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.i("info", "Welcome to the lobby, tester !");

        // Get Google client object
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        this.RTMClient = Games.getRealTimeMultiplayerClient(this, account);

        // Handle leaving the lobby
        final Button button_leave_lobby = findViewById(R.id.button_leave_lobby);
        button_leave_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RTMClient.leave(mJoinedRoom, roomID);

                // Sending to main menu again
                Intent goToMainMenuIntent = new Intent(LobbyActivity.this, MainMenuActivity.class);
                startActivity(goToMainMenuIntent);
            }
        });

        final Button button_invite_players = findViewById(R.id.inviteBtn);
        button_invite_players.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invitePlayers();
            }
        });

        // List and respond to invitations
        final Button goToInvitationInbox = findViewById(R.id.button_go_to_invitation_inbox);
        goToInvitationInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInvitationInbox();
            }
        });


        // Create waiting room / lobby
        //createLobby(); //commenté car il vaut mieux créer le lobby après invitation de joueurs ou auto match
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        currentRoom = savedInstanceState.getParcelable("currentRoom");
        roomID = currentRoom.getRoomId();

    }

    /**
     * This method will display Google default UI for listing invitations.
     */
    private void showInvitationInbox() {
        Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getInvitationInboxIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_INVITATION_INBOX);
                    }
                });
    }


    public void createLobby() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(4, 4, 0);

        RoomConfig roomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        mJoinedRoom = roomConfig;

        // Create the room
        Task<Void> roomCreation = RTMClient.create(roomConfig);

        roomCreation.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Log.i("info", "success on creating room");
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i("error", "exception occured during room creation " +
                            exception.getMessage());
                }

            }
        });
    }

    private void invitePlayers() {
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getSelectOpponentsIntent(1, 3, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }

    private void showWaitingRoom(Room room, int maxPlayersToStartGame) {
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //return of the invitation of players
        if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some other error.
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // Get Automatch criteria.
            int minAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

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
            mJoinedRoomConfig = roomBuilder.build();
            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .create(mJoinedRoomConfig);
        }

        /**
         * Called when the player gets back to the lobby from the invitation inbox.
         * If he has accepted an invitation, he should be redirected to the concerned room.
         */
        else if (requestCode == RC_INVITATION_INBOX) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some error.
                Log.i("error", "Error when returning from invitation inbox");
                return;
            }

            Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null) {

                Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(4, 4, 0);

                RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                        .setOnMessageReceivedListener(mMessageReceivedHandler)
                        .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                        .setAutoMatchCriteria(autoMatchCriteria)
                        .setInvitationIdToAccept(invitation.getInvitationId());
                mJoinedRoomConfig = builder.build();
                Task<Void> joinTask = Games.getRealTimeMultiplayerClient(this,
                            GoogleSignIn.getLastSignedInAccount(this))
                            .join(mJoinedRoomConfig);

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        }
        /**
         * returns from the waiting room UI
         */
        if (requestCode == RC_WAITING_ROOM) {

            // Look for finishing the waiting room from code, for example if a
            // "start game" message is received.  In this case, ignore the result.
            /*if (mWaitingRoomFinishedFromCode) {
                return;
            }*/

            if (resultCode == Activity.RESULT_OK) {
                // Start the game!
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                //On fait quoi nous ?
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                Games.getRealTimeMultiplayerClient(this,
                        GoogleSignIn.getLastSignedInAccount(this))
                        .leave(mJoinedRoomConfig, currentRoom.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

    }


    /**
     * Callbacks to handle reception of messages.
     */

    private OnRealTimeMessageReceivedListener mMessageReceivedHandler =
            new OnRealTimeMessageReceivedListener() {
                @Override
                public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {

                }
            };

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
                Log.d(TAG, "Room " + roomID + " created.");
                showWaitingRoom(room, 4);
                final Button button_lobby_id = findViewById(R.id.button_lobby_id);
                button_lobby_id.setText("Lobby ID : " + roomID); // TODO: Locale
            } else {
                Log.w(TAG, "Error creating room: " + i);
            }
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (i == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
                showWaitingRoom(room, 4);

            } else {
                    Log.w(TAG, "Error joining room:");
            }
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            final Button button_lobby_id = findViewById(R.id.button_lobby_id);
            button_lobby_id.setText("No room"); // TODO: Locale
            Log.i("info", "Room left");
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
            Log.i("info", "A player joined the room !");
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.i("info", "Congratulations, you just connected to a room");
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {

        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.i("info", "Several new players joined the room !");
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.i("info", "Connection established to " + s);
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.i("info", s + "left the room");
        }
    };

    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby() {}



}
