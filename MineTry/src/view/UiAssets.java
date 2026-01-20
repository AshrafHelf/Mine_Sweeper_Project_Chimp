package view;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UiAssets {

    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    public static ImageIcon icon(String file, int w, int h) {
        String key = file + "@" + w + "x" + h;
        if (ICON_CACHE.containsKey(key)) return ICON_CACHE.get(key);

        URL url = UiAssets.class.getClassLoader().getResource("img/" + file);
        if (url == null) {
            System.out.println("⚠ Icon not found: img/" + file);
            return null;
        }

        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        ImageIcon ic = new ImageIcon(img);
        ICON_CACHE.put(key, ic);
        return ic;
    }

    public static Image background(String file) {
        URL url = UiAssets.class.getClassLoader().getResource("img/" + file);
        if (url == null) {
            System.out.println("⚠ Background not found: img/" + file);
            return null;
        }
        return new ImageIcon(url).getImage();
    }
}
