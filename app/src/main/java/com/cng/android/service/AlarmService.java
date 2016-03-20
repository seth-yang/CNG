package com.cng.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.cng.android.util.Keys;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by game on 2016/3/19
 */
public class AlarmService extends IntentService {
    public static void playAlarmVoice (Context context, int times) {
        Intent intent = new Intent (context, AlarmService.class);
        intent.setAction (Keys.Actions.PLAY_ALARM_VOICE);
        intent.putExtra (Keys.KEY_PLAY_VOICE_TIMES, times);
        context.startService (intent);
    }

    private AudioTrack track;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public AlarmService () {
        super ("Alarm Service");
    }

    @Override
    public void onCreate () {
        super.onCreate ();
        int size = AudioTrack.getMinBufferSize (
                15000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        track = new AudioTrack (
                AudioManager.STREAM_MUSIC,
                15000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size,
                AudioTrack.MODE_STREAM
        );
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        String action = intent.getAction ();
        switch (action) {
            case Keys.Actions.PLAY_ALARM_VOICE :
                int times = intent.getIntExtra (Keys.KEY_PLAY_VOICE_TIMES, 0);
                play (times);
                break;
        }
        stopSelf ();
    }

    private void play (int times) {
        track.play ();
        for (int i = 0; i < times; i ++) {
            try (InputStream in = getAssets ().open (Keys.ALARM_VOICE_NAME)) {
                byte[] buff = new byte[1024];
                int length;
                while ((length = in.read (buff)) != -1) {
                    track.write (buff, 0, length);
                }
            } catch (IOException ex) {
                //
                ex.printStackTrace ();
            }
        }
        track.stop ();
    }
}