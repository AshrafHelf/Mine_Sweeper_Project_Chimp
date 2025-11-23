package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;

public class QuestionAdminDialog extends JDialog {

    private JTable table;
    private JButton addBtn, editBtn, deleteBtn, closeBtn;

    public QuestionAdminDialog(JFrame owner) {
        super(owner, "Question Management", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(800, 450));
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        // ===== Title bar =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        titleBar.setBackground(new Color(18, 24, 52));

        JLabel title = new JLabel("Question Bank");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(235, 235, 255));

        JLabel subtitle = new JLabel("Manage questions used in Q-cells (add / edit / delete).");
        subtitle.setForeground(new Color(180, 185, 210));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        JPanel titleTexts = new JPanel();
        titleTexts.setOpaque(false);
        titleTexts.setLayout(new BoxLayout(titleTexts, BoxLayout.Y_AXIS));
        titleTexts.add(title);
        titleTexts.add(subtitle);

        titleBar.add(titleTexts, BorderLayout.WEST);
        getContentPane().add(titleBar, BorderLayout.NORTH);

        // ===== Center: table =====
        String[] cols = {"ID", "Question Text", "Difficulty", "Correct Answer"};
        // placeholder empty model – real data will come from service later
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
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
        center.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        center.setBackground(new Color(12, 16, 35));
        center.add(scroll, BorderLayout.CENTER);

        getContentPane().add(center, BorderLayout.CENTER);

        // ===== Right: action buttons =====
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 16));
        right.setBackground(new Color(12, 16, 35));

        addBtn = new JButton("Add Question");
        editBtn = new JButton("Edit Selected");
        deleteBtn = new JButton("Delete Selected");
        closeBtn = new JButton("Close");

        Dimension btnSize = new Dimension(150, 32);
        for (JButton b : new JButton[]{addBtn, editBtn, deleteBtn, closeBtn}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFocusPainted(false);
        }

        right.add(addBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(editBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(deleteBtn);
        right.add(Box.createVerticalGlue());
        right.add(closeBtn);

        getContentPane().add(right, BorderLayout.EAST);

        // ===== Bottom hint =====
        JLabel hint = new JLabel("Tip: double-click a row to quickly preview the full question.");
        hint.setBorder(BorderFactory.createEmptyBorder(4, 16, 8, 16));
        hint.setForeground(new Color(160, 165, 190));
        hint.setFont(hint.getFont().deriveFont(11f));
        getContentPane().add(hint, BorderLayout.SOUTH);

        // --- Actions (only close is functional for now) ---
        closeBtn.addActionListener(e -> dispose());
    }
}
