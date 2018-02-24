package team23.tartot;

import team23.tartot.core.Player;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;

import android.app.Activity;
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
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.SignInButton;

import static com.google.android.gms.games.Games.getPlayersClient;

public class FirstLogActivity extends AppCompatActivity {

    //request codes
    int RC_SIGN_IN = 23;

    private GoogleSignInAccount userAccount;

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

        findViewById(R.id.logInBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // start the asynchronous sign in flow
                Log.i("coucou", "bienvenue");
                startSignInIntent();
            }
        });
        findViewById(R.id.logOutBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // start the asynchronous sign in flow
                Log.i("coucou", "coucou");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("coucou", "onResume");
        signInSilently();
    }

    //methods for sign in
    private void signInSilently() {
        final GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // The signed in account is stored in the task's result.
                    userAccount = task.getResult();
                    retrieveInfo(userAccount);

                } else {
                    // Player will need to sign-in explicitly using via UI
                    // Intent intent = signInClient.getSignInIntent();
                    //startActivityForResult(intent, RC_SIGN_IN);
                }
            }
        });
    }


    //public void onClick(View view) {
    //    if (view.getId() == R.id.logInBtn) {
    // start the asynchronous sign in flow
    //        startSignInIntent();
    //    } /* else if (view.getId() == R.id.logOutBtn) {
    // sign out.
    //        signOut();
    // show sign-in button, hide the sign-out button
    //        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    //        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    //    } */
    //}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                userAccount = result.getSignInAccount();
                retrieveInfo(userAccount);
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

    private void retrieveInfo(GoogleSignInAccount account) {
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

                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.i("exception", exception.toString());
                }
            }
        });
    }
}