package com.pathways;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;
import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.view.View;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.Metadata;
import java.util.HashMap;
import android.support.annotation.NonNull;
import ai.api.model.AIResponse;
import java.util.Map;

import ai.api.model.Result;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import android.Manifest;
import android.support.v4.content.ContextCompat;
import ai.api.model.Status;
import ai.api.android.GsonFactory;
import android.os.Vibrator;


/**
 * Created by abhishek on 2/21/18.
 */

public class ConversationActivity extends Activity implements AIListener, UtteranceCompleteListener {

    private AIService aiService;
    private TextView resultTextView;
    private static final String TAG = ConversationActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;

    private final String CLIENT_ACCESS_TOKEN = "a72af662d4e441eb87aead05e632b6d2";
    private boolean isListening = false;
    private boolean isUttering = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_layout);
        initViews();
        checkAudioRecordPermission();
    }

    private void initViews() {
        resultTextView = findViewById(R.id.conversation_text_view);
    }

    private void initAISDK() {
        TTS.init(this, this);
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        vibrate();
        aiService.startListening();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    protected void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSIONS_ID);
        }else {
            initAISDK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS_ID: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAISDK();

                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (aiService != null) {
            aiService.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (aiService != null) {
            aiService.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (aiService != null) {
            aiService.stopListening();
        }
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Log.d(TAG, "onResult");

                resultTextView.setText(gson.toJson(response));

                // Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                // Log.i(TAG, "Status code: " + status.getCode());
                // Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                // Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                // Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                // Log.i(TAG, "Speech: " + speech);
                isUttering = true;
                TTS.speak(speech);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    // Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    // Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }
        });
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isListening = true;
                resultTextView.setText("Listening Started");
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText("Listening Canceled");
            }
        });
    }

    @Override
    public void onListeningFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText("Listening Finished");
                isListening = false;
                StartListeningIfAlreadyNotStarted();
            }
        });
    }

    private void StartListeningIfAlreadyNotStarted() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isUttering && !isListening) {
                    vibrate();
                    aiService.startListening();
                }
            }
        }, 5000);
    }

    @Override
    public void onUtteranceComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText("Start listening again");
                vibrate();
                isUttering = false;
                aiService.startListening();
            }
        });
    }
}