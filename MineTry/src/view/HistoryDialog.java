package view;

import model.GameRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryDialog extends JDialog {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public HistoryDialog(JFrame owner, List<GameRecord> records) {
        super(owner, "Game History", true);
        buildUI(records);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(700, 400));
    }

    private void buildUI(List<GameRecord> records) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        String[] cols = {"Date / Time", "Difficulty", "Player 1", "Player 2", "Score", "Result"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (GameRecord r : records) {
            model.addRow(new Object[]{
                    TIME_FMT.format(r.getFinishedAt()),
                    r.getDifficulty().name(),
                    r.getPlayer1Name(),
                    r.getPlayer2Name(),
                    r.getFinalScore(),
                    r.isWon() ? "Win" : "Lose"
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setBackground(new Color(18, 24, 52));
        table.setForeground(new Color(235, 235, 255));
        table.setSelectionBackground(new Color(60, 80, 140));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        getContentPane().add(scroll, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.setFocusPainted(false);
        close.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(12, 16, 35));
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 8, 10));
        bottom.add(close);

        getContentPane().add(bottom, BorderLayout.SOUTH);
    }
}
