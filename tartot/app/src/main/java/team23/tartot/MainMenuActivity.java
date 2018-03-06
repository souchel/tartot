package team23.tartot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.SignInButton;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //à quoi ça sert ?
        //requestWindowFeature(Window.FEATURE_NO_TITLE);


        // Management of the welcome TextView displaying
        final TextView welcomeTextView = findViewById(R.id.text_view_welcome_main_menu);
        final Button goToLobbyButton = findViewById(R.id.button_go_to_lobby);
        String welcome = welcomeTextView.getText().toString() + " Hsb511"; // + getExtra.getUsername();
        welcomeTextView.setText(welcome);

        // setOnClickListener of the button to access the lobby
        goToLobbyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToLobbyIntent = new Intent(MainMenuActivity.this, LobbyActivity.class);
                startActivity(goToLobbyIntent);
            }
        });

        // setOnClickListener of the test button to access GameActivity
        final Button goToGameButton = findViewById(R.id.button_go_to_game);
        goToGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToLobbyIntent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(goToLobbyIntent);
            }
        });

    }

}
