package team23.tartot;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.SignInButton;

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
                //Player newPlayer = Player(username);
                //goToMainMenuIntent.putExtra(newPlayer);
                startActivity(goToMainMenuIntent);
            }
        });

        findViewById(R.id.logInBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                // start the asynchronous sign in flow
                Log.i("coucou", "coucou");
                startSignInIntent();
            }
        });
        findViewById(R.id.logOutBtn).setOnClickListener( new View.OnClickListener() {
            public void onClick(View view){
                // start the asynchronous sign in flow
                Log.i("coucou", "coucou");
            }
        });
    }





    //methods for sign in
    private void signInSilently(Activity activity){
        final GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(activity, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // The signed in account is stored in the task's result.
                    GoogleSignInAccount signedInAccount = task.getResult();
                } else {
                    // Player will need to sign-in explicitly using via UI
                    Intent intent = signInClient.getSignInIntent();
                    startActivityForResult(intent, RC_SIGN_IN);
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

            /*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                Log.i("coucou", "SUCCESSSSSSSS");
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }/*/

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
}
