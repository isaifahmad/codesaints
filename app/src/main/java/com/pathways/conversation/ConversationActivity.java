package com.pathways.conversation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pathways.R;
import com.pathways.conversation.ui.ChatAdapter;
import com.pathways.conversation.ui.UIIncomingChatMessage;

import java.util.ArrayList;
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

public class ConversationActivity extends Activity implements AIListener, UtteranceCompleteListener, RecognitionListener {

    private AIService aiService;
    String message;
    private TextView resultTextView;
    private static final String TAG = ConversationActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;
    Intent recognizerIntent;
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
        init();
        initViews();
        checkAudioRecordPermission();
        SpeechRecognizer.createSpeechRecognizer(ConversationActivity.this).startListening(recognizerIntent);
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
            //aiService.startListening();
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
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    //aiService.startListening();
                    SpeechRecognizer.createSpeechRecognizer(ConversationActivity.this).startListening(recognizerIntent);

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
               // aiService.startListening();
            }
        });
    }


    void init() {

        SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        switch (i) {

            case SpeechRecognizer.ERROR_AUDIO:

                message = "1";

                break;

            case SpeechRecognizer.ERROR_CLIENT:

                message = "2";

                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

                message = "3";

                break;

            case SpeechRecognizer.ERROR_NETWORK:

                message = "4";

                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:

                message = "5";

                break;

            case SpeechRecognizer.ERROR_NO_MATCH:

                message = "6";

                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

                message = "7";

                break;

            case SpeechRecognizer.ERROR_SERVER:

                message = "8";

                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

                message = "9";

                break;

            default:

                message = "10";

                break;

        }
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            Log.i("speech", matches.toString());
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }


    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}