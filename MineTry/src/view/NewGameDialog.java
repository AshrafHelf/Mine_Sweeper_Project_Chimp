package view;

import model.Difficulty;

import javax.swing.*;
import java.awt.*;

public class NewGameDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private boolean confirmed = false;

    private JComboBox<Difficulty> difficultyBox;
    private JTextField p1Field;
    private JTextField p2Field;

    public NewGameDialog(JFrame owner) {
        super(owner, "Start New Game", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(420, 240));
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        difficultyBox = new JComboBox<>(Difficulty.values());
        p1Field = new JTextField("Player 1", 14);
        p2Field = new JTextField("Player 2", 14);

        JLabel l1 = label("Difficulty:");
        JLabel l2 = label("Player 1 name:");
        JLabel l3 = label("Player 2 name:");

        c.gridx = 0; c.gridy = 0; form.add(l1, c);
        c.gridx = 1; form.add(difficultyBox, c);

        c.gridx = 0; c.gridy = 1; form.add(l2, c);
        c.gridx = 1; form.add(p1Field, c);

        c.gridx = 0; c.gridy = 2; form.add(l3, c);
        c.gridx = 1; form.add(p2Field, c);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 12));

        JButton cancel = new JButton("Cancel");
        JButton start = new JButton("Start");

        cancel.addActionListener(e -> { confirmed = false; dispose(); });
        start.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        bottom.add(cancel);
        bottom.add(start);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(Color.WHITE);
        return l;
    }

    public boolean isConfirmed() { return confirmed; }
    public Difficulty getDifficulty() { return (Difficulty) difficultyBox.getSelectedItem(); }

    public String getPlayer1() {
        String s = p1Field.getText().trim();
        return s.isEmpty() ? "Player 1" : s;
    }

    public String getPlayer2() {
        String s = p2Field.getText().trim();
        return s.isEmpty() ? "Player 2" : s;
    }
}
