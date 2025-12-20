package view;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class SoundManager {

    private static Clip musicClip;
    private static FloatControl musicGain;
    private static final Map<String, Clip> sfxCache = new HashMap<>();
    private static boolean musicMuted = false;
    private static float musicVolume = 0.35f; // default


    private SoundManager() {}

    // ---------- MUSIC ----------
    public static void playMusicLoop(String classpathWav) {
        if (musicMuted) return;

        stopMusic();
        musicClip = loadClip(classpathWav);
        if (musicClip == null) return;

        musicGain = getGainControl(musicClip);
        setMusicVolume(musicVolume);

        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip.start();
    }


    public static boolean isMusicMuted() {
        return musicMuted;
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    public static void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
            musicGain = null;
        }
    }

    public static void setMusicMuted(boolean muted) {
        musicMuted = muted;

        if (muted) {
            stopMusic();
        }
    }

    
    public static void setMusicVolume(float volume01) { // 0..1
        if (musicGain == null) return;
        volume01 = Math.max(0f, Math.min(1f, volume01));

        // convert linear volume to decibels (safe)
        float dB;
        if (volume01 == 0f) dB = musicGain.getMinimum();
        else dB = (float) (20.0 * Math.log10(volume01));

        dB = Math.max(musicGain.getMinimum(), Math.min(musicGain.getMaximum(), dB));
        musicGain.setValue(dB);
    }

    // ---------- SFX ----------
    public static void playSfx(String classpathWav) {
        try {
            Clip clip = sfxCache.get(classpathWav);
            if (clip == null) {
                clip = loadClip(classpathWav);
                if (clip == null) return;
                sfxCache.put(classpathWav, clip);
            }

            // restart from beginning each time
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception ignored) {}
    }

    // ---------- INTERNAL ----------
    private static Clip loadClip(String classpathWav) {
        try {
            var in = SoundManager.class.getResourceAsStream(classpathWav);
            if (in == null) {
                System.out.println("Sound not found: " + classpathWav);
                return null;
            }

            var bin = new java.io.BufferedInputStream(in);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bin);

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static FloatControl getGainControl(Clip clip) {
        try {
            return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception e) {
            return null;
        }
    }
}
