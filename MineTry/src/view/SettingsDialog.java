package view;

import view.theme.UIFactory;
import view.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // pick whatever your main menu track is called
    private static final String DEFAULT_MUSIC = "menu_music.wav";

    public SettingsDialog(Window owner) {
        super(owner, "Settings", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel card = UIFactory.glassCard();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(UIFactory.dialogTitle("⚙ Settings"));
        titles.add(Box.createVerticalStrut(2));
        titles.add(UIFactory.dialogSubtitle("Audio & comfort options"));

        JButton close = UIFactory.miniIconButton("✕");
        close.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        top.add(titles, BorderLayout.WEST);
        top.add(close, BorderLayout.EAST);

        // content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JCheckBox musicOn = UIFactory.styledCheck("Music", AppSettings.musicOn());
        JCheckBox sfxOn   = UIFactory.styledCheck("Sound Effects", AppSettings.sfxOn());

        JSlider musicVol = UIFactory.styledSlider(AppSettings.musicVol());
        JSlider sfxVol   = UIFactory.styledSlider(AppSettings.sfxVol());

        // initial enable states
        musicVol.setEnabled(musicOn.isSelected());
        sfxVol.setEnabled(sfxOn.isSelected());

        content.add(row(musicOn, mutedRight("Volume")));
        content.add(Box.createVerticalStrut(6));
        content.add(musicVol);

        content.add(Box.createVerticalStrut(12));

        content.add(row(sfxOn, mutedRight("Volume")));
        content.add(Box.createVerticalStrut(6));
        content.add(sfxVol);

        content.add(Box.createVerticalStrut(14));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton test = UIFactory.glassButton("Test SFX");
        test.addActionListener(e -> {
            if (AppSettings.sfxOn()) AudioManager.playSfx("message.wav");
        });

        JButton done = UIFactory.glassButton("Done");
        done.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        buttons.add(test);
        buttons.add(done);

        // wiring
        musicOn.addActionListener(e -> {
            boolean on = musicOn.isSelected();
            AppSettings.setMusicOn(on);
            musicVol.setEnabled(on);

            if (on) {
                // ensures something plays even if music never started yet
                AudioManager.ensureMusic(DEFAULT_MUSIC, true);
            } else {
                AudioManager.stopMusic();
            }

            AudioManager.playSfx("button.wav");
        });

        sfxOn.addActionListener(e -> {
            boolean on = sfxOn.isSelected();
            AppSettings.setSfxOn(on);
            sfxVol.setEnabled(on);

            // only play click if sfx still on
            if (on) AudioManager.playSfx("button.wav");
        });

        musicVol.addChangeListener(e -> {
            AppSettings.setMusicVol(musicVol.getValue());
            AudioManager.refreshMusic(true);
        });

        sfxVol.addChangeListener(e -> {
            AppSettings.setSfxVol(sfxVol.getValue());
            if (!sfxVol.getValueIsAdjusting() && AppSettings.sfxOn()) {
                AudioManager.playSfx("message.wav");
            }
        });

        card.add(top, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().registerKeyboardAction(
                e -> { AudioManager.playSfx("button.wav"); dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        pack();
        setSize(520, 330);
        setLocationRelativeTo(owner);
    }

    private static JLabel mutedRight(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.DIALOG_SUB);
        return l;
    }

    private static JPanel row(JComponent left, JComponent right) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.add(left, BorderLayout.WEST);
        r.add(right, BorderLayout.EAST);
        return r;
    }
}
