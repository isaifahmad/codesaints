/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pathways.conversation;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

public class TTS {

    private static TextToSpeech textToSpeech;
    private static UtteranceCompleteListener listener;

    public static void init(final Context context, UtteranceCompleteListener completeListener) {
        listener = completeListener;
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int initStatus) {
                    // Log.i("TestToSpeech", "" +initStatus);
                    addListener();

                }
            });
        }
    }

    private static void addListener(){
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Log.i("TTS", "OnStart");
            }

            @Override
            public void onDone(String utteranceId) {
                // Log.i("TTS", "OnDone");
                listener.onUtteranceComplete();
            }

            @Override
            public void onError(String utteranceId) {
                // Log.i("TTS", "OnError");
            }
        });
    }

    public static void speak(final String text) {
        if(Build.VERSION.SDK_INT >= 21) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "123");
        }
    }
}
