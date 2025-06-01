package com.example.localloopapp_android.activities;

import com.example.localloopapp_android.R;
import com.google.firebase.FirebaseApp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogin, buttonPlayGame;
    private TextView textCreateAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        buttonLogin = findViewById(R.id.button_login);
        textCreateAccount = findViewById(R.id.text_create_account);
        buttonPlayGame = findViewById(R.id.button_play_game);

        buttonLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        textCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        buttonPlayGame.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, com.example.localloopapp_android.xbombgame.GameActivity.class);
            startActivity(intent);
        });

    }
}


