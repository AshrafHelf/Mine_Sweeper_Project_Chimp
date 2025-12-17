package view;

import model.Question;
import model.QuestionLevel;
import model.QuestionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionAdminDialog extends JDialog {

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
        setMinimumSize(new Dimension(900, 520));
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

        JLabel subtitle = new JLabel("Manage questions used in Q-cells.");
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
                return false; // edit via dialog
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setBackground(new Color(18, 24, 52));
        table.setForeground(new Color(230, 230, 245));
        table.setSelectionBackground(new Color(60, 80, 140));
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        // ===== Side buttons =====
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 16));
        right.setBackground(new Color(12, 16, 35));

        addBtn = new JButton("Add Question");
        editBtn = new JButton("Edit Selected");
        deleteBtn = new JButton("Delete Selected");
        closeBtn = new JButton("Close");

        Dimension btnSize = new Dimension(170, 34);
        for (JButton b : new JButton[]{addBtn, editBtn, deleteBtn, closeBtn}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFocusPainted(false);
        }

        addBtn.setEnabled(true);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            boolean hasSelection = table.getSelectedRow() >= 0;
            editBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
        });

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEditSelected());
        deleteBtn.addActionListener(e -> onDeleteSelected());
        closeBtn.addActionListener(e -> dispose());

        right.add(addBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(editBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(deleteBtn);
        right.add(Box.createVerticalGlue());
        right.add(closeBtn);

        getContentPane().add(right, BorderLayout.EAST);
    }

    private void onAdd() {
        String newId = generateNextId();
        QuestionEditor editor = new QuestionEditor(this, newId, null);
        editor.setVisible(true);

        if (!editor.isSaved()) return;

        try {
            Question q = editor.buildQuestion();
            qService.addQuestion(q);
            loadQuestionsIntoTable();
            selectRowById(q.getId());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onEditSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String id = String.valueOf(table.getValueAt(row, 0));
        Question existing = findQuestionById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Could not find the selected question in the repository.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        QuestionEditor editor = new QuestionEditor(this, id, existing);
        editor.setVisible(true);

        if (!editor.isSaved()) return;

        try {
            Question updated = editor.buildQuestion(); // same ID, new content
            boolean ok = qService.updateQuestion(updated);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Update failed: question not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadQuestionsIntoTable();
            selectRowById(id);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String id = String.valueOf(table.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete question " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = qService.deleteById(id);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Delete failed: question not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadQuestionsIntoTable();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ===== LOAD QUESTIONS FROM SERVICE =====
    private void loadQuestionsIntoTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        List<Question> questions = qService.getAll();

        for (Question q : questions) {
            String correct = "";
            if (q.getOptions() != null && q.getCorrectIndex() >= 0 && q.getCorrectIndex() < q.getOptions().size()) {
                correct = q.getOptions().get(q.getCorrectIndex());
            }

            model.addRow(new Object[]{
                    q.getId(),
                    q.getText(),
                    q.getLevel().name(),
                    correct
            });
        }
    }

    private Question findQuestionById(String id) {
        for (Question q : qService.getAll()) {
            if (q.getId() != null && q.getId().equals(id)) return q;
        }
        return null;
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    // ===== ID generation: Q001, Q002 ... =====
    private String generateNextId() {
        int max = 0;
        for (Question q : qService.getAll()) {
            String id = q.getId();
            if (id == null) continue;

            // accept numeric IDs like "12" or formatted "Q012"
            String digits = id.replaceAll("\\D+", "");
            if (digits.isEmpty()) continue;

            try {
                int n = Integer.parseInt(digits);
                if (n > max) max = n;
            } catch (NumberFormatException ignored) { }
        }
        return String.format("Q%03d", max + 1);
    }

    private void selectRowById(String id) {
        for (int r = 0; r < table.getRowCount(); r++) {
            if (String.valueOf(table.getValueAt(r, 0)).equals(id)) {
                table.setRowSelectionInterval(r, r);
                table.scrollRectToVisible(table.getCellRect(r, 0, true));
                return;
            }
        }
    }

    // =========================================================
    // Embedded editor dialog (single file, no extra dependency)
    // =========================================================
    private static class QuestionEditor extends JDialog {
        private static final long serialVersionUID = 1L;

        private final String idFixed;
        private boolean saved = false;

        private JTextField idField;
        private JTextArea questionArea;
        private JComboBox<QuestionLevel> levelBox;

        private JTextField optA, optB, optC, optD;
        private JRadioButton ra, rb, rc, rd;
        private ButtonGroup group;

        public QuestionEditor(Dialog owner, String idFixed, Question existing) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            this.idFixed = idFixed;
            build(existing);
            pack();
            setLocationRelativeTo(owner);
            setMinimumSize(new Dimension(620, 470));
        }

        public boolean isSaved() { return saved; }

        public Question buildQuestion() {
            String id = idField.getText().trim();
            String text = questionArea.getText().trim();
            QuestionLevel lvl = (QuestionLevel) levelBox.getSelectedItem();

            List<String> options = new ArrayList<>();
            options.add(optA.getText().trim());
            options.add(optB.getText().trim());
            options.add(optC.getText().trim());
            options.add(optD.getText().trim());

            int correctIndex = getCorrectIndex();

            return new Question(id, text, options, correctIndex, lvl);
        }

        private void build(Question existing) {
            getContentPane().setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(12, 16, 35));

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(new Color(18, 24, 52));
            top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            JLabel title = new JLabel("Question Editor");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            title.setForeground(new Color(235, 235, 255));
            top.add(title, BorderLayout.WEST);

            getContentPane().add(top, BorderLayout.NORTH);

            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

            // ID
            form.add(label("ID:"));
            idField = new JTextField(existing == null ? idFixed : existing.getId());
            idField.setEditable(false);
            styleField(idField);
            form.add(idField);
            form.add(Box.createVerticalStrut(10));

            // Question text
            form.add(label("Question text:"));
            questionArea = new JTextArea(4, 40);
            questionArea.setLineWrap(true);
            questionArea.setWrapStyleWord(true);
            questionArea.setText(existing == null ? "" : existing.getText());
            styleArea(questionArea);

            JScrollPane qScroll = new JScrollPane(questionArea);
            qScroll.setBorder(BorderFactory.createLineBorder(new Color(27, 36, 78)));
            qScroll.getViewport().setBackground(new Color(18, 24, 52));
            form.add(qScroll);
            form.add(Box.createVerticalStrut(10));

            // Level
            form.add(label("Difficulty:"));
            levelBox = new JComboBox<>(QuestionLevel.values());
            levelBox.setSelectedItem(existing == null ? QuestionLevel.EASY : existing.getLevel());
            levelBox.setBackground(new Color(18, 24, 52));
            levelBox.setForeground(new Color(230, 230, 245));
            form.add(levelBox);
            form.add(Box.createVerticalStrut(10));

            // Options
            form.add(label("Options (select correct):"));

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 6, 6));
            optionsPanel.setOpaque(false);

            group = new ButtonGroup();
            ra = new JRadioButton("A");
            rb = new JRadioButton("B");
            rc = new JRadioButton("C");
            rd = new JRadioButton("D");
            styleRadio(ra); styleRadio(rb); styleRadio(rc); styleRadio(rd);
            group.add(ra); group.add(rb); group.add(rc); group.add(rd);

            List<String> opts = existing == null ? List.of("", "", "", "") : existing.getOptions();
            optA = new JTextField(opts.size() > 0 ? opts.get(0) : "");
            optB = new JTextField(opts.size() > 1 ? opts.get(1) : "");
            optC = new JTextField(opts.size() > 2 ? opts.get(2) : "");
            optD = new JTextField(opts.size() > 3 ? opts.get(3) : "");
            styleField(optA); styleField(optB); styleField(optC); styleField(optD);

            optionsPanel.add(optionRow(ra, optA));
            optionsPanel.add(optionRow(rb, optB));
            optionsPanel.add(optionRow(rc, optC));
            optionsPanel.add(optionRow(rd, optD));

            form.add(optionsPanel);
            form.add(Box.createVerticalStrut(12));

            // correct selection default
            int ci = existing == null ? 0 : existing.getCorrectIndex();
            switch (ci) {
                case 0 -> ra.setSelected(true);
                case 1 -> rb.setSelected(true);
                case 2 -> rc.setSelected(true);
                case 3 -> rd.setSelected(true);
                default -> ra.setSelected(true);
            }

            // Buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.setOpaque(false);

            JButton cancel = new JButton("Cancel");
            JButton save = new JButton("Save");
            cancel.setFocusPainted(false);
            save.setFocusPainted(false);

            cancel.addActionListener(e -> {
                saved = false;
                dispose();
            });

            save.addActionListener(e -> {
                if (!validateInput()) return;
                saved = true;
                dispose();
            });

            btns.add(cancel);
            btns.add(save);

            form.add(btns);

            getContentPane().add(form, BorderLayout.CENTER);
        }

        private boolean validateInput() {
            if (questionArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Question text is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (optA.getText().trim().isEmpty() || optB.getText().trim().isEmpty()
                    || optC.getText().trim().isEmpty() || optD.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All 4 options are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (levelBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Difficulty is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        private int getCorrectIndex() {
            if (ra.isSelected()) return 0;
            if (rb.isSelected()) return 1;
            if (rc.isSelected()) return 2;
            return 3;
        }

        private JLabel label(String t) {
            JLabel l = new JLabel(t);
            l.setForeground(new Color(180, 185, 210));
            l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
            return l;
        }

        private void styleField(JTextField f) {
            f.setBackground(new Color(18, 24, 52));
            f.setForeground(new Color(230, 230, 245));
            f.setCaretColor(Color.WHITE);
            f.setBorder(BorderFactory.createLineBorder(new Color(27, 36, 78)));
        }

        private void styleArea(JTextArea a) {
            a.setBackground(new Color(18, 24, 52));
            a.setForeground(new Color(230, 230, 245));
            a.setCaretColor(Color.WHITE);
            a.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        private void styleRadio(JRadioButton r) {
            r.setOpaque(false);
            r.setForeground(new Color(230, 230, 245));
            r.setFocusPainted(false);
        }

        private JPanel optionRow(JRadioButton r, JTextField f) {
            JPanel p = new JPanel(new BorderLayout(8, 0));
            p.setOpaque(false);
            p.add(r, BorderLayout.WEST);
            p.add(f, BorderLayout.CENTER);
            return p;
        }
    }
}
