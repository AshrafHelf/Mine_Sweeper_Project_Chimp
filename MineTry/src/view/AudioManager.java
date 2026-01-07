package view;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.util.EnumMap;
import java.util.Map;

public class AudioManager {

    public enum Sfx {
        MENU_THEME,
        START_JINGLE,
        WIN,
        LOSE,
        CLICK_REVEAL,
        CLICK_FLAG,
        Q_GOOD,
        Q_BAD,
        S_GOOD,
        S_BAD
    }

    private static final AudioManager INSTANCE = new AudioManager();
    public static AudioManager get() { return INSTANCE; }

    private final Map<Sfx, Clip> clips = new EnumMap<>(Sfx.class);

    private boolean muted = false;
    private float volume = 0.85f; // 0..1
    private Clip menuLoop;

    private long lastRevealMs = 0; // throttle

    private AudioManager() { }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) stopMenuTheme();
    }

    public boolean isMuted() { return muted; }

    public void setVolume(float v) {
        if (v < 0f) v = 0f;
        if (v > 1f) v = 1f;
        this.volume = v;
        // apply to already loaded clips
        for (Clip c : clips.values()) applyVolume(c);
        if (menuLoop != null) applyVolume(menuLoop);
    }

    public float getVolume() { return volume; }

    // Load once at app start (optional)
    public void preloadAll() {
        load(Sfx.MENU_THEME, "/audio/menu_theme.wav");
        load(Sfx.START_JINGLE, "/audio/start_jingle.wav");
        load(Sfx.WIN, "/audio/win.wav");
        load(Sfx.LOSE, "/audio/lose.wav");
        load(Sfx.CLICK_REVEAL, "/audio/click_reveal.wav");
        load(Sfx.CLICK_FLAG, "/audio/click_flag.wav");
        load(Sfx.Q_GOOD, "/audio/q_good.wav");
        load(Sfx.Q_BAD, "/audio/q_bad.wav");
        load(Sfx.S_GOOD, "/audio/s_good.wav");
        load(Sfx.S_BAD, "/audio/s_bad.wav");
    }

    public void play(Sfx sfx) {
        if (muted) return;
        Clip c = clips.get(sfx);
        if (c == null) return;
        restartClip(c);
    }

    // for very frequent click sounds (reveal), throttle to avoid noise spam
    public void playRevealClickThrottled(long minGapMs) {
        if (muted) return;
        long now = System.currentTimeMillis();
        if (now - lastRevealMs < minGapMs) return;
        lastRevealMs = now;
        play(Sfx.CLICK_REVEAL);
    }

    public void startMenuThemeLoop() {
        if (muted) return;
        Clip c = clips.get(Sfx.MENU_THEME);
        if (c == null) return;
        stopMenuTheme();
        menuLoop = c;
        menuLoop.setFramePosition(0);
        applyVolume(menuLoop);
        menuLoop.loop(Clip.LOOP_CONTINUOUSLY);
        menuLoop.start();
    }

    public void stopMenuTheme() {
        if (menuLoop != null) {
            menuLoop.stop();
            menuLoop.setFramePosition(0);
            menuLoop = null;
        }
    }

    public void load(Sfx sfx, String resourcePath) {
        if (clips.containsKey(sfx)) return;

        try {
            var url = getClass().getResource(resourcePath);
            if (url == null) return;

            // BufferedInputStream helps on some JAR reads
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyVolume(clip);
            clips.put(sfx, clip);
        } catch (Exception ignored) {
            // keep silent (no crash)
        }
    }

    private void restartClip(Clip c) {
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        applyVolume(c);
        c.start();
    }

    private void applyVolume(Clip c) {
        try {
            FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            // convert 0..1 volume to dB
            float dB;
            if (volume <= 0.0001f) dB = gain.getMinimum();
            else dB = (float) (20.0 * Math.log10(volume));
            if (dB < gain.getMinimum()) dB = gain.getMinimum();
            if (dB > gain.getMaximum()) dB = gain.getMaximum();
            gain.setValue(dB);
        } catch (Exception ignored) {}
    }
}
