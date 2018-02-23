package com.pathways.conversation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonElement;
import com.pathways.R;
import com.pathways.conversation.ui.ChatAdapter;
import com.pathways.conversation.ui.UIIncomingChatMessage;
import com.pathways.conversation.ui.UIOutgoingChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;


/**
 * Created by abhishek on 2/21/18.
 */

public class ConversationActivity extends Activity implements UtteranceCompleteListener, RecognitionListener {

    private TextView resultTextView;
    private static final String TAG = ConversationActivity.class.getName();
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;

    private final String CLIENT_ACCESS_TOKEN = "a72af662d4e441eb87aead05e632b6d2";
    private boolean isListening = false;
    private boolean isUttering = false;
    private ListView chatListView;
    private ChatAdapter adapter;
    private ImageView imageView;

    private AudioManager audioManager;
    private int currentVolume = 50;
    private SpeechRecognizer stt;
    private Intent recognizer_intent;
    private AIDataService aiDataService;

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
    }

    private void initAISDK() {
        stt = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        stt.setRecognitionListener(this);
        stt.startListening(recognizer_intent);


        TTS.init(this, this);
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
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
        if (stt != null) {
            stt.stopListening();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stt != null) {
            vibrate();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            stt.startListening(recognizer_intent);
        }
    }


    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();
                // UIOutgoingChatMessage message = new UIOutgoingChatMessage(speech);
                UIIncomingChatMessage message = new UIIncomingChatMessage(speech);
                adapter.appendMessages(message);
                if (speech.equalsIgnoreCase("4 BHK") ||
                        speech.equalsIgnoreCase("5 BHK") ||
                        speech.equalsIgnoreCase("6 BHK")) {
                    Intent intent = new Intent(ConversationActivity.this, ViewFlipperActivity.class);
                    intent.putExtra(ViewFlipperActivity.CHECK, speech);
                    startActivity(intent);
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


    private void StartListeningIfAlreadyNotStarted() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUttering && !isListening) {
                    vibrate();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    stt.startListening(recognizer_intent);
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
                stt.startListening(recognizer_intent);
            }
        });
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        imageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBeginningOfSpeech() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isListening = true;
                resultTextView.setText("Listening Started");
            }
        });
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
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

    @Override
    public void onError(int i) {
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && matches.size() > 0 && !matches.get(0).isEmpty()) {
            requestQuery(matches.get(0));
        }
    }

    private void requestQuery(String s) {
        UIOutgoingChatMessage message = new UIOutgoingChatMessage(s);
        adapter.appendMessages(message);
        QueryTask task = new QueryTask();
        task.execute(s);

    }

    class QueryTask extends AsyncTask<String, Void, AIResponse> {

        @Override
        protected AIResponse doInBackground(final String... params) {
            final AIRequest request = new AIRequest();
            String query = params[0];
            if (!TextUtils.isEmpty(query))
                request.setQuery(query);
            try {
                return aiDataService.request(request);
            } catch (final AIServiceException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final AIResponse response) {
            if (response != null) {
                onResult(response);
            } else {
                onError(1);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}