package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.Question;
import model.QuestionService;

import java.awt.*;
import java.util.List;

public class QuestionAdminDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
    private JButton addBtn, editBtn, deleteBtn, closeBtn;

    private final QuestionService qService;

    public QuestionAdminDialog(JFrame owner, QuestionService qService) {
        super(owner, "Question Management", true);
        this.qService = qService;

        buildUI();
        loadQuestionsIntoTable();

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

        JLabel subtitle = new JLabel("Manage questions used in Q-cells (read-only for now).");
        subtitle.setForeground(new Color(180, 185, 210));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        JPanel titleTexts = new JPanel();
        titleTexts.setOpaque(false);
        titleTexts.setLayout(new BoxLayout(titleTexts, BoxLayout.Y_AXIS));
        titleTexts.add(title);
        titleTexts.add(subtitle);

        titleBar.add(titleTexts, BorderLayout.WEST);
        getContentPane().add(titleBar, BorderLayout.NORTH);

        // ===== Table =====
        String[] cols = {"ID", "Question Text", "Difficulty", "Correct Answer"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only (Iteration 1 requirement)
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

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(27, 36, 78));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(18, 24, 52));

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        center.setBackground(new Color(12, 16, 35));
        center.add(scroll, BorderLayout.CENTER);
        getContentPane().add(center, BorderLayout.CENTER);

        // ===== Side buttons (disabled for Iteration 1) =====
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

        // Iteration 1: disable buttons (not required yet)
        addBtn.setEnabled(false);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        right.add(addBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(editBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(deleteBtn);
        right.add(Box.createVerticalGlue());
        right.add(closeBtn);

        getContentPane().add(right, BorderLayout.EAST);

        closeBtn.addActionListener(e -> dispose());
    }

    // ===== LOAD QUESTIONS FROM SERVICE =====
    private void loadQuestionsIntoTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // clear existing rows

        List<Question> questions = qService.getAll();

        for (Question q : questions) {
            model.addRow(new Object[]{
                    q.getId(),
                    q.getText(),
                    q.getLevel().name(),
                    q.getOptions().get(q.getCorrectIndex())
            });
        }
    }
}
