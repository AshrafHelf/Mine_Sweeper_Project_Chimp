package view;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class UiAssets {
    private UiAssets() {}

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    public static Image loadImage(String classpath) {
        try {
            URL url = UiAssets.class.getResource(classpath);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    public static ImageIcon icon(String classpath, int w, int h) {
        String key = classpath + "|" + w + "x" + h;
        if (CACHE.containsKey(key)) return CACHE.get(key);

        URL url = UiAssets.class.getResource(classpath);
        if (url == null) return null;

        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(img);
        CACHE.put(key, icon);
        return icon;
    }
}
