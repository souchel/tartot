package team23.tartot;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import static android.content.ContentValues.TAG;

public class LobbyActivity extends AppCompatActivity {

    private RealTimeMultiplayerClient RTMClient;
    private RoomConfig mJoinedRoom = null;
    private String roomID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

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

        // Create waiting room / lobby
        createLobby();
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

        }
    };

    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby() {}

}
