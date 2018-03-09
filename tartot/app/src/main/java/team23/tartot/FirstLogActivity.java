package team23.tartot;

import team23.tartot.core.Player;

import team23.tartot.core.Player;
import android.content.Intent;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.Button;

import android.app.AlertDialog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
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
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.games.Games.getPlayersClient;
import static java.lang.Integer.valueOf;

public class FirstLogActivity extends AppCompatActivity {

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
    private String incomingInvitationId = null;

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {R.id.log_in_screen, R.id.main_menu_screen, R.id.lobby_screen};
    //id of the currently showed screen
    private int curScreen = -1;
    private Player newPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("debug","create");

        setContentView(R.layout.activity_first_log);
        switchToMainScreen();

        signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        final Button firstLogButton = findViewById(R.id.button_first_log);
        firstLogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToMainMenuIntent = new Intent(FirstLogActivity.this, MainMenuActivity.class);
                String username = ((EditText) findViewById(R.id.edit_text_first_log)).getText().toString();
                newPlayer = new Player(username);
                switchToMainScreen();
            }
        });

        //logInBtn connects to googlePlay service to retrieve player's google account and his google game account
        //this button manually connects to google accounts if the silent signIn in the onResume does not work.
        findViewById(R.id.logInBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // start the asynchronous sign in flow by starting an activityForResult dealing with the sign in
                //If the return is successfull, the
                startSignInIntent();
            }
        });

        //logOutBtn callback
        findViewById(R.id.logOutBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                signOut();
            }
        });

        // Management of the welcome TextView displaying
        final TextView welcomeTextView = findViewById(R.id.text_view_welcome_main_menu);
        final Button goToLobbyButton = findViewById(R.id.button_go_to_lobby);
        String welcome = welcomeTextView.getText().toString() + " Hsb511"; // + getExtra.getUsername();
        welcomeTextView.setText(welcome);

        // setOnClickListener of the button to access the lobby
        goToLobbyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switchToScreen(R.id.lobby_screen);
            }
        });

        // setOnClickListener of the test button to access GameActivity
        final Button goToGameButton = findViewById(R.id.button_go_to_game);
        goToGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToGameIntent = new Intent(FirstLogActivity.this, GameActivity.class);
                startActivity(goToGameIntent);
            }
        });

        // Handle leaving the lobby
        final Button button_leave_lobby = findViewById(R.id.button_leave_lobby);
        button_leave_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveLobby();
                // Sending to main menu again
                switchToMainScreen();
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

        //display the waiting room corresponding to the currentRoom
        final Button displayWaitingRoom = findViewById(R.id.displayWaitingRoom);
        displayWaitingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRoom != null){
                    showWaitingRoom(currentRoom, 4);
                }
                else{
                    Log.i("debug", "dans aucune room en ce moment");
                }
            }
        });

        final Button autoPlayersBtn = findViewById(R.id.autoPlayersBtn);
        autoPlayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRoom == null){
                    startQuickGame();
                }
                else{
                    Log.i("debug", "déjà dans une room");
                }
            }
        });
    }


    //onResume tries to silently sign in. It connects to the last google account used or use one already logged in.
    @Override
    protected void onResume() {
        super.onResume();
        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
        Log.i("debug","resume");

    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (invitationsClient != null) {
            invitationsClient.unregisterInvitationCallback(invitationCallback);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("debug", "LobbyActivity.onRestoreInstanceState");
        currentRoom = savedInstanceState.getParcelable("currentRoom");
        if (currentRoom != null){
            roomID = currentRoom.getRoomId();
        }
    }


    private InvitationCallback invitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            incomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    invitation.getInviter().getDisplayName() + " " +
                            getString(R.string.is_inviting_you));
            switchToScreen(curScreen); // We change the current screen for the same, but it updates the notifications
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (incomingInvitationId.equals(invitationId) && incomingInvitationId != null) {
                incomingInvitationId = null;
                switchToScreen(curScreen); // This will hide the invitation popup
            }
        }
    };

    // Accept the given invitation.
    void acceptInviteToRoom(String invitationId) {
        // accept the invitation
        Log.d("debug", "Accepting invitation: " + invitationId);

        currentRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .build();

        //switchToScreen(R.id.screen_wait);
        keepScreenOn();

        rtmc.join(currentRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    //tries to log in silently and automatically without prompt. If it succeeds, it connects to the google game account and retrieve google Player object.
    private void signInSilently() {
        Log.i("debug","sigin silent");

        signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    Log.i("debug", "signInSilently(): success");
                    // The signed in account is stored in the task's result.
                    onConnected(task.getResult());
                    //retrieveGooglePlayer(userAccount);

                    invitationsClient = Games.getInvitationsClient(getApplicationContext(), userAccount);

                    // register listener so we are notified if we receive an invitation to play
                    // while we are in the game
                    invitationsClient.registerInvitationCallback(invitationCallback);

                } else {
                    Log.i("debug", "signInSilently(): failed");


                    //automatic log in failed.
                    //todo : notify user that he has to log in manually
                }
            }
        });
    }

    //handles the return of the intent of external activity log in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("le-nom","test");

        /**
         * called after the player pickup in the invitation activity
         */
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
            currentRoomConfig = roomBuilder.build();
            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .create(currentRoomConfig);
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
                RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                        .setOnMessageReceivedListener(mMessageReceivedHandler)
                        .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                        .setInvitationIdToAccept(invitation.getInvitationId());
                currentRoomConfig = builder.build();
                Task<Void> joinTask = Games.getRealTimeMultiplayerClient(this,
                        GoogleSignIn.getLastSignedInAccount(this))
                        .join(currentRoomConfig);

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
        else if (requestCode == RC_WAITING_ROOM) {

            // Look for finishing the waiting room from code, for example if a
            // "start game" message is received.  In this case, ignore the result.
            /*if (mWaitingRoomFinishedFromCode) {
                return;
            }*/

            if (resultCode == Activity.RESULT_OK) {
                // Start the game!
                Intent goToGameIntent = new Intent(this, GameActivity.class);
                startActivity(goToGameIntent);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                //On fait quoi nous ?
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                leaveLobby();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }


    /**
     * Callbacks to handle reception of messages.
     */

        else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result
                userAccount = result.getSignInAccount();
                retrieveGooglePlayer(userAccount);

            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = result.getStatus().toString() + " | error" ;
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }


    //change the visibility of the layout to change screen. used to display invites notification bar.
    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        curScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        //la belle condition ternaire
        findViewById(R.id.invitation_popup).setVisibility(incomingInvitationId != null ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (rtmc != null) {
            switchToScreen(R.id.main_menu_screen);
        } else {
            switchToScreen(R.id.log_in_screen);
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room, int maxPlayersToStartGame) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        rtmc.getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }


    //start external log in activity to retrieve google player's account
    private void startSignInIntent() {
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                        Log.i("info", "Déconnexion réussie");
                    }
                });
    }

    //retrieve google Player object from the google account
    private void retrieveGooglePlayer(GoogleSignInAccount account) {
        PlayersClient playersClient = getPlayersClient(this, account);
        Task<com.google.android.gms.games.Player> playerTask = playersClient.getCurrentPlayer(); //carefull, use Player object from google and not from tartot
        playerTask.addOnCompleteListener(new OnCompleteListener<com.google.android.gms.games.Player>() {
            @Override
            public void onComplete(@NonNull Task<com.google.android.gms.games.Player> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    googlePlayer = task.getResult();
                    Log.i("le-nom", "m "+ googlePlayer.getDisplayName());

                    // The Player can now go to the MainMenu and if there is no username, it will be its Google username
                    EditText usernameET = findViewById(R.id.edit_text_first_log);
                    Log.i("le-nom","ici");
                    String username = usernameET.getText().toString();
                    Log.i("le-nom",username +" ");
                    if (username.equals("")) {
                        Log.i("nom",googlePlayer.getDisplayName());
                        usernameET.setText(googlePlayer.getDisplayName());
                    }
                    findViewById(R.id.button_first_log).setVisibility(View.VISIBLE);


                    /*
                    ///////////////////////////////////////////////temporaire
                    Intent goToMainMenuIntent = new Intent(FirstLogActivity.this, MainMenuActivity.class);
                    String username = ((EditText) findViewById(R.id.edit_text_first_log)).getText().toString();
                    Player newPlayer = new Player(username);
                    //goToMainMenuIntent.putExtra(newPlayer);
                    startActivity(goToMainMenuIntent);
                    ///////////////////////////////////////////////temporaire
                    */
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i("exception", exception.toString());
                }
            }
        });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d("debug", "onConnected(): connected to Google APIs");
        if (userAccount != googleSignInAccount) {

            userAccount = googleSignInAccount;
            Log.i("debug", "alloc rtmc");
            // update the clients
            rtmc = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            invitationsClient = Games.getInvitationsClient(this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<com.google.android.gms.games.Player>() {
                        @Override
                        public void onSuccess(com.google.android.gms.games.Player player) {
                            googlePlayer = player;
                            playerId = player.getPlayerId();
                            Log.i("le-nom", "m "+ googlePlayer.getDisplayName());

                            //on s'est bien connecté, on peut afficher le boutton pour accéder au menu principal
                            findViewById(R.id.button_first_log).setVisibility(View.VISIBLE);
                            //à enlever quand on aura fusionné les activités de menu.
                            switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        invitationsClient.registerInvitationCallback(invitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(this, googleSignInAccount);
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
                .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
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
                currentRoom = room;
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
                currentRoom = room;
                roomID = room.getRoomId();
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
                Log.i("debug", "currentRoomBeforeWaitingRoom : " + currentRoom);
                showWaitingRoom(room, 4);

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
                final Button button_lobby_id = findViewById(R.id.button_lobby_id);
                button_lobby_id.setText("No room"); // TODO: Locale
                Log.i("info", "Room left");
                currentRoom = null;
                currentRoomConfig = null;
                roomID = null;
            }
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
     * (merged from lobbyActivity)
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


    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();
            Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

            //TODO : on fait quoi ?
        }
    };


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


    public void startQuickGame() {
        Spinner sp = findViewById(R.id.playerNbSpinner);
        int nbOfPlayers = valueOf(sp.getSelectedItem().toString());

        //we want to match n-1 other players
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(nbOfPlayers-1, nbOfPlayers-1, 0);

        currentRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create the room
        Task<Void> roomCreation = rtmc.create(currentRoomConfig);

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


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}