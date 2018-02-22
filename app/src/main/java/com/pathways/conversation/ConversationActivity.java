package com.pathways.conversation;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
<<<<<<< HEAD:app/src/main/java/com/pathways/ConversationActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
=======
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
>>>>>>> 7aa798166ba00af45982a25db59c585ef86c5bd8:app/src/main/java/com/pathways/conversation/ConversationActivity.java
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
<<<<<<< HEAD:app/src/main/java/com/pathways/ConversationActivity.java
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.Metadata;

import java.util.ArrayList;
import java.util.HashMap;
import android.support.annotation.NonNull;
import ai.api.model.AIResponse;

import java.util.Locale;
import java.util.Map;

import ai.api.model.Result;
import android.os.AsyncTask;
=======
>>>>>>> 7aa798166ba00af45982a25db59c585ef86c5bd8:app/src/main/java/com/pathways/conversation/ConversationActivity.java
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private final String CLIENT_ACCESS_TOKEN = "a72af662d4e441eb87aead05e632b6d2";
    private final String DEFAULT_SPEECH = "";
    private boolean isListening = false;
    private boolean isUttering = false;
    private ListView chatListView;
    private ChatAdapter adapter;
    private ImageView imageView;

    private AudioManager audioManager;
    private int currentVolume = 50;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_layout);
        initViews();
        checkAudioRecordPermission();
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.conversation_image);
        Glide.with(this).load(R.raw.giphy).into(imageView);
        imageView.setVisibility(View.INVISIBLE);
        resultTextView = findViewById(R.id.conversation_text_view);
        chatListView = findViewById(R.id.chat_message_listview);
        adapter = new ChatAdapter(getApplicationContext());
        chatListView.setAdapter(adapter);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void initAISDK() {
        TTS.init(this, this);
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
<<<<<<< HEAD:app/src/main/java/com/pathways/ConversationActivity.java
        vibrate();
        askSpeechInput();
        aiService.startListening();
=======
>>>>>>> 7aa798166ba00af45982a25db59c585ef86c5bd8:app/src/main/java/com/pathways/conversation/ConversationActivity.java
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
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

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


    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    // Receiving speech input

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.i("ConversationActivity", result.get(0));
                }
                break;
            }

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
                imageView.setVisibility(View.VISIBLE);
                resultTextView.setText("Listening Started");
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
                resultTextView.setText("Listening Canceled");
                isListening = false;
                StartListeningIfAlreadyNotStarted();
            }
        });
    }

    @Override
    public void onListeningFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
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
<<<<<<< HEAD:app/src/main/java/com/pathways/ConversationActivity.java
                    askSpeechInput();
=======
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
>>>>>>> 7aa798166ba00af45982a25db59c585ef86c5bd8:app/src/main/java/com/pathways/conversation/ConversationActivity.java
                    aiService.startListening();
                }
            }
        }, 3000);
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
                askSpeechInput();
                aiService.startListening();
            }
        });
    }
}