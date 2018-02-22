package com.pathways;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by rahulmagow on 2/22/18.
 */




import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity implements RecognitionListener {



        private SpeechRecognizer stt;
        private Intent recognizer_intent;
        private int id;
        private String num;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.marker_layout);

            stt = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    this.getPackageName());
            recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            stt.setRecognitionListener(this);
            start();



        }

        private void log1(String message) {
            Log.d("moo", num + ": " + message);
        }

        private void log2(String message) {
            Log.d("cow", num + ": " + message);
        }

        public void start() {
            num = Integer.toString(++id);
            log1("start");
            stt.startListening(recognizer_intent);
        }

        public void stop(View view) {
            log1("stop");
            stt.stopListening();
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            log1("onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            log1("onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rms_dB) {
            log2("onRmsChanged");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            log1("onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            log1("onEndOfSpeech");
        }

        @Override
        public void onResults(Bundle results) {
            log1("onResults");
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            log1("onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            log1("onEvent");
        }

        @Override
        public void onError(int error) {
            String message = "";

            if (error == SpeechRecognizer.ERROR_AUDIO) message = "audio";
            else if (error == SpeechRecognizer.ERROR_CLIENT) message = "client";
            else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
                message = "insufficient permissions";
            else if (error == SpeechRecognizer.ERROR_NETWORK) message = "network";
            else if (error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) message = "network timeout";
            else if (error == SpeechRecognizer.ERROR_NO_MATCH) message = "no match found";
            else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) message = "recognizer busy";
            else if (error == SpeechRecognizer.ERROR_SERVER) message = "server";
            else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) message = "speech timeout";

            log1("error " + message);
        }
    }

