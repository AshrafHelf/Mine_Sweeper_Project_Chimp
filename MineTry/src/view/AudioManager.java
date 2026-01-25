package view;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class AudioManager {

    private static Clip musicClip;
    private static String currentMusicFile;

    // Cache SFX clips (kept open for fast replay)
    private static final Map<String, Clip> SFX_CACHE = new HashMap<>();

    // Safety limit so you don't keep infinite open lines
    private static final int MAX_SFX_CACHE = 24;

    private AudioManager() {}

    private static URL audioUrl(String wavFile) {
        // Must match: resources/audio/<file>.wav inside your jar
        return AudioManager.class.getClassLoader().getResource("audio/" + wavFile);
    }

    // ============================
    // Music
    // ============================

    public static synchronized void startMusic(String wavFile, boolean loop) {
        currentMusicFile = wavFile;

        if (!AppSettings.musicOn()) {
            stopMusic();
            return;
        }

        stopMusic();

        Clip clip = loadClip(wavFile);
        if (clip == null) return;

        musicClip = clip;
        setClipVolume(musicClip, AppSettings.musicVol());

        if (loop) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            musicClip.start();
        }
    }

    /** Apply current settings to playing music. If missing/closed, it restarts last track. */
    public static synchronized void refreshMusic(boolean loop) {
        if (!AppSettings.musicOn()) {
            stopMusic();
            return;
        }

        // If music exists, just update volume and ensure playing
        if (musicClip != null && musicClip.isOpen()) {
            setClipVolume(musicClip, AppSettings.musicVol());
            if (!musicClip.isRunning()) {
                if (loop) musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                else musicClip.start();
            }
            return;
        }

        // Otherwise restart the last known track
        if (currentMusicFile != null) startMusic(currentMusicFile, loop);
    }

    public static synchronized void stopMusic() {
        if (musicClip != null) {
            try {
                musicClip.stop();
                musicClip.close();
            } catch (Exception ignored) {}
            musicClip = null;
        }
    }

    // ============================
    // SFX
    // ============================

    public static synchronized void playSfx(String wavFile) {
        if (!AppSettings.sfxOn()) return;

        try {
            Clip c = SFX_CACHE.get(wavFile);

            if (c == null || !c.isOpen()) {
                c = loadClip(wavFile);
                if (c == null) return;

                // keep cache bounded
                if (SFX_CACHE.size() >= MAX_SFX_CACHE) evictOneSfx();
                SFX_CACHE.put(wavFile, c);
            }

            // restart from beginning
            c.stop();
            c.setFramePosition(0);
            setClipVolume(c, AppSettings.sfxVol());
            c.start();

        } catch (Exception ex) {
            System.out.println("⚠ SFX error: " + wavFile + " -> " + ex.getMessage());
        }
    }

    private static void evictOneSfx() {
        // Simple eviction: remove the first entry (good enough for this project)
        Iterator<Map.Entry<String, Clip>> it = SFX_CACHE.entrySet().iterator();
        if (!it.hasNext()) return;

        Map.Entry<String, Clip> e = it.next();
        Clip c = e.getValue();
        try {
            if (c != null) {
                c.stop();
                c.close();
            }
        } catch (Exception ignored) {}
        it.remove();
    }

    public static synchronized void stopAllSfx() {
        for (Clip c : SFX_CACHE.values()) {
            try {
                if (c != null && c.isOpen()) {
                    c.stop();
                    c.setFramePosition(0);
                    c.flush();
                }
            } catch (Exception ignored) {}
        }
    }

    /** Call this once when app exits. */
    public static synchronized void shutdown() {
        stopMusic();
        for (Clip c : SFX_CACHE.values()) {
            try {
                if (c != null) {
                    c.stop();
                    c.close();
                }
            } catch (Exception ignored) {}
        }
        SFX_CACHE.clear();
    }

    // ============================
    // Internals: load + decode to PCM_SIGNED 16-bit
    // ============================

    private static Clip loadClip(String wavFile) {
        URL url = audioUrl(wavFile);
        if (url == null) {
            System.out.println("⚠ Missing audio: audio/" + wavFile);
            return null;
        }

        try (BufferedInputStream bin = new BufferedInputStream(url.openStream());
             AudioInputStream original = AudioSystem.getAudioInputStream(bin)) {

            AudioFormat base = original.getFormat();
            AudioInputStream audioToPlay = original;

            // Convert anything non-PCM16 into PCM_SIGNED 16-bit (most compatible)
            boolean needDecode =
                    !AudioFormat.Encoding.PCM_SIGNED.equals(base.getEncoding())
                    || base.getSampleSizeInBits() != 16;

            if (needDecode) {
                AudioFormat decoded = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        base.getSampleRate(),
                        16,
                        base.getChannels(),
                        base.getChannels() * 2,
                        base.getSampleRate(),
                        false
                );
                audioToPlay = AudioSystem.getAudioInputStream(decoded, original);
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioToPlay); // Clip buffers data internally
            return clip;

        } catch (UnsupportedAudioFileException e) {
            System.out.println("⚠ Unsupported audio file: " + wavFile);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ Audio format not supported for " + wavFile + " (" + e.getMessage() + ")");
            System.out.println("   Fix: convert to WAV PCM 16-bit, 44100/48000Hz.");
        } catch (IOException e) {
            System.out.println("⚠ Audio IO error (" + wavFile + "): " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("⚠ Audio line unavailable (" + wavFile + "): " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("⚠ Audio load error (" + wavFile + "): " + ex.getMessage());
        }

        return null;
    }
    public static synchronized void ensureMusic(String wavFile, boolean loop) {
        // if music is off, do nothing (settings might call it)
        if (!AppSettings.musicOn()) return;

        // if nothing is set yet, or music closed, start it
        if (currentMusicFile == null) {
            startMusic(wavFile, loop);
            return;
        }

        // if the requested track is different, switch
        if (!currentMusicFile.equals(wavFile)) {
            startMusic(wavFile, loop);
            return;
        }

        // otherwise just refresh volume/play state
        refreshMusic(loop);
    }

    /** volume0to100 -> MASTER_GAIN with perceptual curve */
    private static void setClipVolume(Clip clip, int volume0to100) {
        if (clip == null) return;

        try {
            if (!clip.isOpen()) return;
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float min = gain.getMinimum();
            float max = gain.getMaximum();

            float v = Math.max(0, Math.min(100, volume0to100)) / 100f;

            // If fully muted, push to minimum
            if (v <= 0f) {
                gain.setValue(min);
                return;
            }

            // Perceptual curve: better control at low volume
            float db = min + (max - min) * (float) Math.pow(v, 0.65);
            gain.setValue(db);

        } catch (Exception ignored) {
            // Some mixers/drivers don't expose MASTER_GAIN, ignore silently
        }
    }
}
