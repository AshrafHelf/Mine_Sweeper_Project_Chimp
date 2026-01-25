package view;

import java.util.prefs.Preferences;

public final class AppSettings {

    private static final Preferences P = Preferences.userNodeForPackage(AppSettings.class);

    private static final String K_MUSIC_ON  = "musicOn";
    private static final String K_SFX_ON    = "sfxOn";
    private static final String K_MUSIC_VOL = "musicVol"; // 0..100
    private static final String K_SFX_VOL   = "sfxVol";   // 0..100

    private AppSettings() {}

    public static boolean musicOn() { return P.getBoolean(K_MUSIC_ON, true); }
    public static boolean sfxOn()   { return P.getBoolean(K_SFX_ON, true); }

    public static int musicVol() { return clamp(P.getInt(K_MUSIC_VOL, 55)); }
    public static int sfxVol()   { return clamp(P.getInt(K_SFX_VOL, 75)); }

    public static void setMusicOn(boolean v) { P.putBoolean(K_MUSIC_ON, v); }
    public static void setSfxOn(boolean v)   { P.putBoolean(K_SFX_ON, v); }

    public static void setMusicVol(int v) { P.putInt(K_MUSIC_VOL, clamp(v)); }
    public static void setSfxVol(int v)   { P.putInt(K_SFX_VOL, clamp(v)); }

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}
