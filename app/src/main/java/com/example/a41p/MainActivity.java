package com.example.a41p;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText workoutDurationEditText;
    private EditText restDurationEditText;
    private TextView progressWorkoutTextView;
    private TextView restTimeTextView;
    private TextView workoutTimeTextView;
    private Button timeStartButton;
    private Button timeStopButton;
    private ProgressBar progressBar;
    private CountDownTimer workoutTimer;
    private CountDownTimer restTimer;
    private Handler handler;
    private Runnable runnable;
    private long workoutDurationInMillis;
    private long restDurationInMillis;
    private long timeLeftInMillis;
    private boolean isWorkoutPhase = true;
    private boolean isTimerRunning = false;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        workoutDurationEditText = findViewById(R.id.workoutduration);
        restDurationEditText = findViewById(R.id.restduration);
        progressWorkoutTextView = findViewById(R.id.progressworkout);
        restTimeTextView = findViewById(R.id.resttime);
        workoutTimeTextView = findViewById(R.id.workouttime);
        timeStartButton = findViewById(R.id.timestart);
        timeStopButton = findViewById(R.id.timestop);
        progressBar = findViewById(R.id.progressBar);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //handler and runnable called for updates
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        //buttons listeners set for click event
        timeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
        timeStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });
    }

    private void updateUI() {
        long minutes = timeLeftInMillis / 1000 / 60;
        long seconds = timeLeftInMillis / 1000 % 60;
        String timeLeftString = String.format("%02d:%02d", minutes, seconds);
        progressWorkoutTextView.setText(timeLeftString);

        //calculation for progress bar based on time ramining
        int progress = (int) ((isWorkoutPhase ? workoutDurationInMillis : restDurationInMillis) - timeLeftInMillis);
        progressBar.setMax((int) (isWorkoutPhase ? workoutDurationInMillis : restDurationInMillis));
        progressBar.setProgress(progress);

        //updating times based on current timer, workout or rest
        if (isWorkoutPhase) {
            workoutTimeTextView.setVisibility(View.VISIBLE);
            restTimeTextView.setVisibility(View.INVISIBLE);
        } else {
            workoutTimeTextView.setVisibility(View.INVISIBLE);
            restTimeTextView.setVisibility(View.VISIBLE);
        }
    }


    //time starting
    private void startTimer() {
        if (!isTimerRunning) {
            //get values from edittexts and convert to milliseconds
            workoutDurationInMillis = Long.parseLong(workoutDurationEditText.getText().toString()) * 1000;
            restDurationInMillis = Long.parseLong(restDurationEditText.getText().toString()) * 1000;

            if (isWorkoutPhase) {
                timeLeftInMillis = workoutDurationInMillis;
                workoutTimeTextView.setVisibility(View.VISIBLE);
                restTimeTextView.setVisibility(View.INVISIBLE);
            } else {
                timeLeftInMillis = restDurationInMillis;
                workoutTimeTextView.setVisibility(View.INVISIBLE);
                restTimeTextView.setVisibility(View.VISIBLE);
            }

            //this loop checks and changes timer based if rest or workout
            if (isWorkoutPhase) {
                if (restTimer != null) {
                    restTimer.cancel();
                }
                workoutTimer = new CountDownTimer(workoutDurationInMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftInMillis = millisUntilFinished;
                        updateUI();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onFinish() {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        isTimerRunning = false;
                        progressBar.setProgress(0);
                        timeLeftInMillis = restDurationInMillis;
                        isWorkoutPhase = false;
                        workoutTimeTextView.setVisibility(View.INVISIBLE);
                        restTimeTextView.setVisibility(View.VISIBLE);
                        startTimer();
                    }
                }.start();
            } else {
                if (workoutTimer != null) {
                    workoutTimer.cancel();
                }
                restTimer = new CountDownTimer(restDurationInMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftInMillis = millisUntilFinished;
                        updateUI();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onFinish() {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        isTimerRunning = false;
                        progressBar.setProgress(0);
                        timeLeftInMillis = workoutDurationInMillis;
                        isWorkoutPhase = true;
                        workoutTimeTextView.setVisibility(View.VISIBLE);
                        restTimeTextView.setVisibility(View.INVISIBLE);
                        startTimer();
                    }
                }.start();
            }

            isTimerRunning = true;
            handler.post(runnable);
        }
    }
    // Stop the timer
    private void stopTimer() {
        if (isTimerRunning) {
            if (isWorkoutPhase) {
                workoutTimer.cancel();
            } else {
                restTimer.cancel();
            }
            isTimerRunning = false;
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }
}
