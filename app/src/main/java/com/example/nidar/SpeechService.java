package com.example.nidar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechService extends Service {

    private SpeechRecognizer speech = null;
    private static String LOG_TAG = "In Speech Service";
    private Intent recognizerIntent;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    String [] keyWords;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        // On tapping the notification, it will open the application's main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("App is running in background")
                .setContentText("Tap to open the application")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
    }


    private void resetSpeechRecognizer() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        if (speech != null) {
            speech.destroy();
        }

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if (SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onBeginningOfSpeech() {
                    Log.i(LOG_TAG, "onBeginningOfSpeech");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.i(LOG_TAG, "onBufferReceived: " + buffer);
                }

                @Override
                public void onEndOfSpeech() {
                    Log.i(LOG_TAG, "onEndOfSpeech");
                }

                @Override
                public void onResults(Bundle results) {
                    Log.i(LOG_TAG, "onResults");
                    final ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    String gotText = matches.get(0).toLowerCase();
                    Log.i(LOG_TAG, gotText);

                    boolean flag = false;
                    for (String panicWords: keyWords) {
                        if (gotText.contains(panicWords)) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        Log.i("test", "In flag = true");
                        Intent intent = new Intent(SpeechService.this, OwnDialog.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.putExtra("Title", "Are you in a problem?");
                        intent.putExtra("Calling Class", "Speech Service");
                        startActivity(intent);
                        stopSelf();
                    }
                    else {
                        resetSpeechRecognizer();
                        speech.startListening(recognizerIntent);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    String errorMessage = getErrorText(errorCode);
                    Log.i(LOG_TAG, "FAILED " + errorMessage);
                    resetSpeechRecognizer();
                    speech.startListening(recognizerIntent);
                }

                @Override
                public void onEvent(int arg0, Bundle arg1) {
                    Log.i(LOG_TAG, "onEvent");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    Log.i(LOG_TAG, "onPartialResults");
                }

                @Override
                public void onReadyForSpeech(Bundle arg0) {
                    Log.i(LOG_TAG, "onReadyForSpeech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
                }
            });
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    private void setRecogniserIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        //Adding keywords
        keyWords = new String [] {"help me",  "help me please", "bachao bachao", "bachao mujhe",
                "chor se bacho", "please"};

        resetSpeechRecognizer();
        setRecogniserIntent();
        speech.startListening(recognizerIntent);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speech!=null)
            speech.destroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
