package team23.tartot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private static final int RC_INVITATION_INBOX = 9008;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final TextView welcomeTextView = findViewById(R.id.text_view_welcome_main_menu);
        final Button goToLobbyButton = findViewById(R.id.text_view_go_to_lobby);
        final Button goToInvitationInbox = findViewById(R.id.button_go_to_invitation_inbox);

        String welcome = welcomeTextView.getText().toString() + " Hsb511"; // + getExtra.getUsername();
        welcomeTextView.setText(welcome);

        goToLobbyButton.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View view) {
               Intent goToLobbyIntent = new Intent(MainMenuActivity.this, LobbyActivity.class);
               startActivity(goToLobbyIntent);
                                               }
                                           }
        );

        goToInvitationInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInvitationInbox();
            }
        });
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

    /**
     * Called when the player gets back to the main menu from the invitation inbox.
     * If he has accepted an invitation, he should be redirected to the concerned room.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_INVITATION_INBOX) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some error.
                return;
            }

            Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null) {
                RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                        .setInvitationIdToAccept(invitation.getInvitationId());
                mJoinedRoomConfig = builder.build();
                Games.getRealTimeMultiplayerClient(thisActivity,
                        GoogleSignIn.getLastSignedInAccount(this))
                        .join(mJoinedRoomConfig);
                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }


}
