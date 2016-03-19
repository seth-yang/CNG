package com.cng.android.activity;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by game on 2016/3/19
 */
public class VoiceActivity extends Activity {
    int size;
    AudioTrack track;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        new PcmPlayer ().init ().start ();
    }

    class PcmPlayer extends Thread {

        PcmPlayer init () {
            size = AudioTrack.getMinBufferSize (15000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            track = new AudioTrack (AudioManager.STREAM_MUSIC, 15000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, size, AudioTrack.MODE_STREAM);
            track.play ();
            return this;
        }

        @Override
        public void run () {
            byte[] buff = new byte[1024];
            int length;
            while (true) {
                try (InputStream in = getAssets ().open ("voice.pcm")) {
                    while ((length = in.read (buff)) != -1) {
                        track.write (buff, 0, length);
                    }
                } catch (IOException ex) {
                    //
                }
                try {
                    sleep (1000);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }
        }
    }
}