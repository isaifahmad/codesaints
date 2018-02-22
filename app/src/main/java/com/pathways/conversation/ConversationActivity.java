package com.pathways.conversation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pathways.R;
import com.pathways.conversation.ui.ChatAdapter;
import com.pathways.conversation.ui.UIIncomingChatMessage;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;


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
    private final String DEFAULT_SPEECH = "";
    private boolean isListening = false;
    private boolean isUttering = false;
    private ListView chatListView;
    private ChatAdapter adapter;

    private AudioManager audioManager;
    private int currentVolume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_layout);
        initViews();
        checkAudioRecordPermission();
    }

    private void initViews() {
        resultTextView = findViewById(R.id.conversation_text_view);
        chatListView = findViewById(R.id.chat_message_listview);
        adapter = new ChatAdapter(getApplicationContext());
        chatListView.setAdapter(adapter);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void initAISDK() {
        TTS.init(this, this);
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
    }

    protected void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSIONS_ID);
        } else {
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
            aiService.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (aiService != null) {
            vibrate();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            aiService.startListening();
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

                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();
                if (!speech.equalsIgnoreCase(DEFAULT_SPEECH)) {
                    // UIOutgoingChatMessage message = new UIOutgoingChatMessage(speech);
                    UIIncomingChatMessage message = new UIIncomingChatMessage(speech);
                    adapter.appendMessages(message);
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                isUttering = true;
                TTS.speak(speech);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
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
                if (!isUttering && !isListening) {
                    vibrate();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
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
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                isUttering = false;
                aiService.startListening();
            }
        });
    }
}