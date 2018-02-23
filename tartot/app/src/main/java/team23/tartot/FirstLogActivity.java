package team23.tartot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_log);

        final Button firstLogButton = findViewById(R.id.button_first_log);
        firstLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMainMenuIntent = new Intent(FirstLogActivity.this, MainMenuActivity.class);
                //Player newPlayer = Player(username);
                //goToMainMenuIntent.putExtra(newPlayer);
                startActivity(goToMainMenuIntent);
            }
        });
    }

}
