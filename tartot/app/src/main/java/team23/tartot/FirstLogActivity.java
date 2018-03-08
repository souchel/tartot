package team23.tartot;

import team23.tartot.core.Player;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;

import android.app.AlertDialog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

import static com.google.android.gms.games.Games.getPlayersClient;

public class FirstLogActivity extends AppCompatActivity {

    //request codes
    int RC_SIGN_IN = 23;

    private GoogleSignInAccount userAccount;
    private com.google.android.gms.games.Player googlePlayer;

    // Client used to interact with the Invitation system.
    private InvitationsClient invitationsClient = null;

    // Client used to sign in with Google APIs
    private GoogleSignInClient signInClient = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    private String incomingInvitationId = null;

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {R.id.first_log};
    //id of the currently showed screen
    private int curScreen = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("debug","create");

        setContentView(R.layout.activity_first_log);

        signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Log.i("debug","signin trouvé");
        final Button firstLogButton = findViewById(R.id.button_first_log);
        firstLogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToMainMenuIntent = new Intent(FirstLogActivity.this, MainMenuActivity.class);
                String username = ((EditText) findViewById(R.id.edit_text_first_log)).getText().toString();
                Player newPlayer = new Player(username);
                //goToMainMenuIntent.putExtra(newPlayer);
                startActivity(goToMainMenuIntent);
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

    //tries to log in silently and automatically without prompt. If it succeeds, it connects to the google game account and retrieve google Player object.
    private void signInSilently() {
        Log.i("debug","sigin silent");

        signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // The signed in account is stored in the task's result.
                    userAccount = task.getResult();
                    retrieveGooglePlayer(userAccount);

                    invitationsClient = Games.getInvitationsClient(getApplicationContext(), userAccount);

                    // register listener so we are notified if we receive an invitation to play
                    // while we are in the game
                    invitationsClient.registerInvitationCallback(invitationCallback);

                } else {
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

        if (requestCode == RC_SIGN_IN) {
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
}