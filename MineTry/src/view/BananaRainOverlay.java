package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BananaRainOverlay extends JComponent {
    private static final long serialVersionUID = 1L;

    private static class Banana {
        float x, y, vx, vy;
        float rot, rotV;
        int size;
    }

    private final List<Banana> bananas = new ArrayList<>();
    private final Random rnd = new Random();
    private final Image bananaImg;
    private Timer timer;

    public BananaRainOverlay(Image bananaImg) {
        this.bananaImg = bananaImg;
        setOpaque(false);
        setVisible(false);
    }

    public void startRain(int count) {
        if (bananaImg == null) return;

        bananas.clear();
        int w = getWidth();
        int h = getHeight();

        for (int i = 0; i < count; i++) {
            Banana b = new Banana();
            b.size = 28 + rnd.nextInt(22);
            b.x = rnd.nextInt(Math.max(1, w - b.size));
            b.y = -rnd.nextInt(400) - b.size;
            b.vx = -0.8f + rnd.nextFloat() * 1.6f;
            b.vy = 2.5f + rnd.nextFloat() * 3.5f;
            b.rot = rnd.nextFloat() * 360f;
            b.rotV = -4f + rnd.nextFloat() * 8f;
            bananas.add(b);
        }

        setVisible(true);

        if (timer != null) timer.stop();
        timer = new Timer(16, e -> tick());
        timer.start();
    }

    public void stopRain() {
        if (timer != null) timer.stop();
        timer = null;
        bananas.clear();
        setVisible(false);
        repaint();
    }

    private void tick() {
        int w = getWidth();
        int h = getHeight();
        boolean anyOnScreen = false;

        for (Banana b : bananas) {
            b.x += b.vx;
            b.y += b.vy;
            b.vy += 0.08f; // gravity
            b.rot += b.rotV;

            if (b.y < h + b.size) anyOnScreen = true;

            // wrap a little horizontally
            if (b.x < -b.size) b.x = w;
            if (b.x > w) b.x = -b.size;
        }

        repaint();

        // stop when all fell
        if (!anyOnScreen) stopRain();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isVisible() || bananaImg == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Banana b : bananas) {
            int s = b.size;
            int x = Math.round(b.x);
            int y = Math.round(b.y);

            g2.translate(x + s / 2.0, y + s / 2.0);
            g2.rotate(Math.toRadians(b.rot));
            g2.drawImage(bananaImg, -s / 2, -s / 2, s, s, this);
            g2.rotate(-Math.toRadians(b.rot));
            g2.translate(-(x + s / 2.0), -(y + s / 2.0));
        }

        g2.dispose();
    }
}
