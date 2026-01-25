package view.theme;

import java.awt.*;

public final class Theme {

    // Jungle
    public static final Color BG_FALLBACK = new Color(12, 18, 16);
    public static final Color OVERLAY = new Color(0, 0, 0, 150);

    // Wood + banana accents
    public static final Color WOOD = new Color(125, 82, 43);
    public static final Color WOOD_DARK = new Color(74, 49, 27);
    public static final Color BANANA = new Color(255, 216, 77);
    public static final Color LEAF = new Color(104, 186, 93);

    // Text
    public static final Color TEXT = new Color(245, 241, 232);
    // Muted for overlays/subtitles (keep alpha so it works on dark glass)
    public static final Color TEXT_MUTED = new Color(235, 235, 235, 170);

    // Glass system (shared by dialogs + screens)
    public static final Color GLASS_FILL   = new Color(0, 0, 0, 150);
    public static final Color GLASS_SHADOW = new Color(0, 0, 0, 85);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 60);

    // Radii / spacing
    public static final int ARC = 22;
    public static final int GLASS_ARC = 24;
    public static final int SHADOW_PAD = 6;
    public static final int PAD = 14;

    // Fonts (balanced: premium + friendly)
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 46);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BTN = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);

    // Dialog fonts (smaller than main screens)
    public static final Font DIALOG_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font DIALOG_SUB   = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font DIALOG_BTN   = new Font("SansSerif", Font.BOLD, 12);

    private Theme() {}
}
