package team23.tartot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import team23.tartot.core.Player;

import static java.lang.Integer.valueOf;

public class MenuActivity extends AppCompatActivity {

    //request codes
    int RC_SIGN_IN = 23;
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_SELECT_PLAYERS = 9006; //request code for external invitation activity
    private static final int RC_INVITATION_INBOX = 9008;

    private ApiManagerService mApiManagerService;
    private boolean mBound = false;

    //broadcast receiver to receive broadcast from the apiManagerService
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("broadcast", intent.getStringExtra("value"));
        }
    };

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


    }

    @Override
    public void onStart(){
        super.onStart();

        //register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter("apiManagerService");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

        // Bind to ApiManagerService
        Intent intent = new Intent(this, ApiManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        switchToMainScreen();



        // Management of the welcome TextView displaying
        final TextView welcomeTextView = findViewById(R.id.text_view_welcome_main_menu);
        String welcome = welcomeTextView.getText().toString() + " Hsb511"; // + getExtra.getUsername();
        welcomeTextView.setText(welcome);

        initListeners();


    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ApiManagerService.LocalBinder binder = (ApiManagerService.LocalBinder) service;
            mApiManagerService = binder.getService();
            mBound = true;
            mApiManagerService.update();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void initListeners(){
        final Button firstLogButton = findViewById(R.id.button_first_log);
        firstLogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
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
                startActivityForResult(mApiManagerService.getSignInIntent(), RC_SIGN_IN);
            }
        });

        //logOutBtn callback
        findViewById(R.id.logOutBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mApiManagerService.signOut();
            }
        });

       // setOnClickListener of the button to access the lobby
        final Button goToLobbyButton = findViewById(R.id.button_go_to_lobby);
        goToLobbyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switchToScreen(R.id.lobby_screen);
            }
        });

        // setOnClickListener of the test button to access GameActivity
        final Button goToGameButton = findViewById(R.id.button_go_to_game);
        goToGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToGameIntent = new Intent(MenuActivity.this, GameActivity.class);
                startActivity(goToGameIntent);
            }
        });

        // Handle leaving the lobby
        final Button button_leave_lobby = findViewById(R.id.button_leave_lobby);
        button_leave_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApiManagerService.leaveLobby();
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
                mApiManagerService.showWaitingRoom(4);
            }
        });

        final Button autoPlayersBtn = findViewById(R.id.autoPlayersBtn);
        autoPlayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner sp = findViewById(R.id.playerNbSpinner);
                int nbOfPlayers = valueOf(sp.getSelectedItem().toString());
                mApiManagerService.startQuickGame(nbOfPlayers);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }
        });
    }


    //onResume tries to silently sign in. It connects to the last google account used or use one already logged in.
    @Override
    protected void onResume() {
        super.onResume();
        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.

        if (mBound){
            mApiManagerService.update();
        }
        //TODO : action after signin (in onServceConnected ?)
        /*mApiManagerService.signInSilently().addOnSuccessListener(this, new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                //on s'est bien connecté, on peut afficher le boutton pour accéder au menu principal
                findViewById(R.id.button_first_log).setVisibility(View.VISIBLE);
                //à enlever quand on aura fusionné les activités de menu.
                switchToMainScreen();
            }
        });*/
        Log.i("debug","resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister our listeners.  They will be re-registered via
        // onResume->mApiManagerService.signInSilently->onConnected.
        if (mBound) {
            mApiManagerService.unregisterListeners();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        //TODO : uncomment when Parcelable will be implemented
        /*
        //Restoring the apiManager
        Parcel apiManagerParcel = savedInstanceState.getParcelable("apiManager");
        apiManager = new APIManager(apiManagerParcel, this);




        Log.i("debug", "LobbyActivity.onRestoreInstanceState");
        apiManager = savedInstanceState.getParcelable("apiManager"); //TODO : Implement parcelable
        currentRoom = savedInstanceState.getParcelable("currentRoom");
        if (currentRoom != null){
            roomID = currentRoom.getRoomId();
        }
        */
    }

    //display that we got an invitation
    public void onInvitationReceived(String invitationId, String inviterName) {
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                inviterName + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(curScreen); // We change the current screen for the same, but it updates the notifications
    }

    //startActivity when a service asks to
    public void requestForStartActivityForResul(Intent intent, int requestCode){
        this.startActivityForResult(intent, RC_SIGN_IN);

    }

    //handles the return of the intent of external activity log in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

            //start the room
            mApiManagerService.startRoomAfterPlayerPickup(invitees, minAutoPlayers, maxAutoPlayers);
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
                mApiManagerService.startRoomFromInvitation(invitation);
            }
        }
        /**
         * returns from the waiting room UI
         */
        else if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == Activity.RESULT_OK) {
                // Start the game!
                startGame();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                //On fait quoi nous ?
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                mApiManagerService.leaveLobby();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        //if we come back from the manual signin
        else if (requestCode == RC_SIGN_IN) {
            String errorMessage = mApiManagerService.onSigninReturn(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
            //check if signin successfull (<=> message is null)
            if (errorMessage != null)
                new AlertDialog.Builder(this).setMessage(errorMessage)
                        .setNeutralButton(android.R.string.ok, null).show();
        }
    }
    private void startGame(){
        Intent goToGameIntent = new Intent(this, GameActivity.class);
        //TODO : notify api manager we switched to game !
        //TODO : implement parcelable so that we can pass it to the next activity !
        //we pass the mApiManagerService object to the gameActivity to keep the connection
        //goToGameIntent.putExtra("apiManager", apiManager);
        startActivity(goToGameIntent);
    }

    public void hidePopUps(){
        switchToScreen(curScreen);
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
        findViewById(R.id.invitation_popup).setVisibility(mBound && mApiManagerService.isInvitationPending() ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mBound && mApiManagerService.isConnected()) {
            switchToScreen(R.id.main_menu_screen);
        } else {
            switchToScreen(R.id.log_in_screen);
        }
    }


    //callbacks on the room event. (just graphical actions)
    public void onRoomCreated(String roomId){
        final Button button_lobby_id = findViewById(R.id.button_lobby_id);
        button_lobby_id.setText("Lobby ID : " + roomId); // TODO: Locale
    }

    //TODO : do something ?
    public void onJoinedRoom(String roomId){
        return;
    }
    public void onLeftRoom(){
        final Button button_lobby_id = findViewById(R.id.button_lobby_id);
        button_lobby_id.setText("No room"); // TODO: Locale
    }

    //TODO : do something ?
    public void onRoomConnected(){
        return;
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




    private void invitePlayers() {
        mApiManagerService.getInvitePlayersIntent();
    }


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    public void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}