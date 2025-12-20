package view;

import javax.swing.*;
import java.awt.*;

public class AudioSettingsDialog extends JDialog {

    public AudioSettingsDialog(JFrame owner) {
        super(owner, "Audio Settings", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JCheckBox muteMusic = new JCheckBox("Mute music");
        muteMusic.setOpaque(false);
        muteMusic.setForeground(Color.WHITE);
        muteMusic.setSelected(SoundManager.isMusicMuted());

        JSlider volume = new JSlider(0, 100, (int)(SoundManager.getMusicVolume() * 100));
        volume.setOpaque(false);
        volume.setForeground(Color.WHITE);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);
        volume.setMajorTickSpacing(25);

        muteMusic.addActionListener(e ->
                SoundManager.setMusicMuted(muteMusic.isSelected())
        );

        volume.addChangeListener(e -> {
            float v = volume.getValue() / 100f;
            SoundManager.setMusicVolume(v);
        });

        JLabel volLabel = new JLabel("Music volume");
        volLabel.setForeground(new Color(220, 225, 255));
        volLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(muteMusic);
        content.add(Box.createVerticalStrut(12));
        content.add(volLabel);
        content.add(volume);

        add(content, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(close);

        add(bottom, BorderLayout.SOUTH);
    }
}
