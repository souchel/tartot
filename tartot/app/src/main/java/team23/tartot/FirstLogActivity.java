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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.games.Games.getPlayersClient;

public class FirstLogActivity extends AppCompatActivity {

    //request codes
    int RC_SIGN_IN = 23;

    private GoogleSignInAccount userAccount;
    private com.google.android.gms.games.Player googlePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_log);

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
        if (userAccount == null) {
            signInSilently();
        }
    }

    //tries to log in silently and automatically without prompt. If it succeeds, it connects to the google game account and retrieve google Player object.
    private void signInSilently() {
        final GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // The signed in account is stored in the task's result.
                    userAccount = task.getResult();
                    retrieveGooglePlayer(userAccount);

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
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result
                userAccount = result.getSignInAccount();
                retrieveGooglePlayer(userAccount);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    //start external log in activity to retrieve google player's account
    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
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

                    //test to read username
                    Log.i("debug", googlePlayer.getDisplayName() + googlePlayer.getName());

                    ///////////////////////////////////////////////temporaire
                    Intent goToMainMenuIntent = new Intent(FirstLogActivity.this, MainMenuActivity.class);
                    String username = ((EditText) findViewById(R.id.edit_text_first_log)).getText().toString();
                    Player newPlayer = new Player(username);
                    //goToMainMenuIntent.putExtra(newPlayer);
                    startActivity(goToMainMenuIntent);
                    ///////////////////////////////////////////////temporaire

                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i("exception", exception.toString());
                }
            }
        });
    }
}