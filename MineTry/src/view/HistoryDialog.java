package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.GameRecord;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryDialog extends JDialog {

    public HistoryDialog(JFrame owner, List<GameRecord> records) {
        super(owner, "Game History", true);
        buildUI(records);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(700, 350));
    }

    private void buildUI(List<GameRecord> records) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        // ==== Title bar ====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(18, 24, 52));
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Game History");
        title.setForeground(new Color(235, 235, 255));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Previous games played on this computer.");
        subtitle.setForeground(new Color(175, 180, 210));
        subtitle.setFont(subtitle.getFont().deriveFont(12f));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(title);
        labels.add(subtitle);

        titleBar.add(labels, BorderLayout.WEST);
        getContentPane().add(titleBar, BorderLayout.NORTH);

        // ==== Table data ====
        String[] cols = {"Date/Time", "Player 1", "Player 2", "Difficulty", "Score P1", "Score P2"};
        Object[][] data;

        if (records == null || records.isEmpty()) {
            data = new Object[][]{
                    {"No games yet", "-", "-", "-", "-", "-"}
            };
        } else {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            data = new Object[records.size()][cols.length];
            for (int i = 0; i < records.size(); i++) {
                GameRecord r = records.get(i);
                data[i][0] = fmt.format(new Date(r.timestamp()));
                data[i][1] = r.player1();
                data[i][2] = r.player2();
                data[i][3] = r.difficulty().name();
                data[i][4] = r.score1();
                data[i][5] = r.score2();
            }
        }


        JTable table = new JTable(new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setBackground(new Color(18, 24, 52));
        table.setForeground(new Color(230, 230, 245));
        table.setSelectionBackground(new Color(60, 80, 140));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(30, 40, 80));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(27, 36, 78));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(18, 24, 52));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(12, 16, 35));
        center.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        center.add(scroll, BorderLayout.CENTER);

        getContentPane().add(center, BorderLayout.CENTER);

        // ==== Bottom bar ====
        JButton close = new JButton("Close");
        close.setFocusPainted(false);
        close.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(12, 16, 35));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        bottom.add(close);

        getContentPane().add(bottom, BorderLayout.SOUTH);
    }
}
