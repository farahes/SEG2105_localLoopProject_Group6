package com.example.localloopapp_android.xbombgame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.example.localloopapp_android.R;

public class GameActivity extends AppCompatActivity {

    // definition of variables brother

    // Layouts
    LinearLayout startLayout;
    RelativeLayout bombLayout;
    RelativeLayout boringLayout;

    // Views
    Button fiveButton, gameButton, disarmButton, replayButton;
    TextView timerView, outcomeMessage, bombInstruction, boringText, momLine, textBackToLogin;

    // Logic
    CountDownTimer countDownTimer;
    boolean isFlashing = false;
    boolean disarmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Link layouts
        startLayout = findViewById(R.id.startLayout); // R is the directory of all the resources
        bombLayout = findViewById(R.id.bombLayout); // giant static map of constants
        boringLayout = findViewById(R.id.boringLayout);
        textBackToLogin = findViewById(R.id.textBackToLogin);


        // Link buttons and views
        fiveButton = findViewById(R.id.fiveButton);
        gameButton = findViewById(R.id.gameButton);
        //---
        disarmButton = findViewById(R.id.disarmButton);
        replayButton = findViewById(R.id.replayButton);
        timerView = findViewById(R.id.timerView);
        outcomeMessage = findViewById(R.id.outcomeMessage);
        bombInstruction = findViewById(R.id.bombInstruction);
        //---
        boringText = findViewById(R.id.boringText);
        momLine = findViewById(R.id.momLine);

        // Boring screen btn
        fiveButton.setOnClickListener(view -> {
            startLayout.setVisibility(View.GONE);
            boringLayout.setVisibility(View.VISIBLE);

            animateBoringText();
            showBoringMomMessageAfterDelay();
        });

        // Start bomb game
        gameButton.setOnClickListener(view -> {
            startLayout.setVisibility(View.GONE);
            bombLayout.setVisibility(View.VISIBLE);
            startBombSequence();
        });

        // DISARM button
        disarmButton.setOnClickListener(view -> disarmed = true);

        // Replay button
        replayButton.setOnClickListener(view -> {
            recreate(); // restarts activity (fresh reset)
        });

        textBackToLogin.setOnClickListener(view -> {
            finish(); // This sends you back to the previous activity (MainActivity)
        });

    }

    private void animateBoringText(){
        boringText.setScaleX(1f);
        boringText.setScaleY(1f);
        boringText.setText("You are a very BORING person.");
        boringText.setVisibility(View.VISIBLE);

        // ⏳ Wait 3 seconds before starting animation
        // postDelayed(Runnable, delayMillis)
        boringText.postDelayed(() -> {
            // method chaining for readability
            boringText.animate() // returns a ViewPropertyAnimator object
                    .scaleX(10f) // calls scaleX on that obj
                    .scaleY(10f)
                    .setDuration(3000);
        }, 1000);
    }

    private void showBoringMomMessageAfterDelay(){
        boringText.postDelayed(() -> { // lambda expression. "when clicked, do this"
            boringText.setVisibility(View.INVISIBLE);
            momLine.setText("Your mom doesn't love you.");
            momLine.setVisibility(View.VISIBLE);
            replayButton.setVisibility(View.VISIBLE);
        }, 4000); // waits 4 sec
    }

    private void startBombSequence() {
        countDownTimer = new CountDownTimer(10000, 1000) {
            int timeLeft = 10;

            @Override
            public void onTick(long millisUntilFinished) {
                if (disarmed) {
                    showOutcome("Wow. You actually did it.\nYour mom is… mildly proud.");
                    cancel();
                    return;
                }

                timerView.setText(String.format("00:%02d", timeLeft--));
                toggleFlashing();
            }

            @Override
            public void onFinish() {
                if (!disarmed) {
                    showOutcome("💥 BOOM.\nYour life insurance has been notified.\n\nYour mom still doesn't love you.");
                }
            }
        };

        countDownTimer.start();
    }

    private void showOutcome(String message) {
        disarmButton.setVisibility(View.GONE);
        timerView.setVisibility(View.GONE);
        bombInstruction.setVisibility(View.GONE);
        outcomeMessage.setVisibility(View.VISIBLE);
        outcomeMessage.setText(message);
        replayButton.setVisibility(View.VISIBLE);
    }

    private void toggleFlashing() {
        isFlashing = !isFlashing;
        bombLayout.setBackgroundColor(isFlashing ? 0xFFFF0000 : 0xFF000000);
    }
}
