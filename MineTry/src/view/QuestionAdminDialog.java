package view;

import enums.QuestionLevel;
import model.Question;
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
        setMinimumSize(new Dimension(920, 500));
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        titleBar.setBackground(new Color(18, 24, 52));

        JLabel title = new JLabel("Question Bank");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(235, 235, 255));

        JLabel subtitle = new JLabel("Add / Edit / Delete (saved to data/Questions.csv)");
        subtitle.setForeground(new Color(180, 185, 210));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        JPanel titleTexts = new JPanel();
        titleTexts.setOpaque(false);
        titleTexts.setLayout(new BoxLayout(titleTexts, BoxLayout.Y_AXIS));
        titleTexts.add(title);
        titleTexts.add(subtitle);

        titleBar.add(titleTexts, BorderLayout.WEST);
        getContentPane().add(titleBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Question Text", "Difficulty", "Correct Answer"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 16));
        right.setBackground(new Color(12, 16, 35));

        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        closeBtn = new JButton("Close");

        Dimension btnSize = new Dimension(160, 36);
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

        closeBtn.addActionListener(e -> dispose());
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
    }

    private void onAdd() {
        String newId = qService.nextId();
        QuestionFormDialog dlg = new QuestionFormDialog((JFrame) getOwner(), null, newId);
        Question created = dlg.showAndGet();
        if (created == null) return;

        qService.add(created);
        loadQuestionsIntoTable();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a question first.");
            return;
        }

        String id = table.getValueAt(row, 0).toString();
        Question original = qService.getAll().stream()
                .filter(q -> q.getId().equals(id))
                .findFirst().orElse(null);

        if (original == null) {
            JOptionPane.showMessageDialog(this, "Question not found.");
            return;
        }

        QuestionFormDialog dlg = new QuestionFormDialog((JFrame) getOwner(), original, id);
        Question updated = dlg.showAndGet();
        if (updated == null) return;

        qService.update(updated);
        loadQuestionsIntoTable();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a question first.");
            return;
        }

        String id = table.getValueAt(row, 0).toString();
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete question ID " + id + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        qService.deleteById(id);
        loadQuestionsIntoTable();
    }

    private void loadQuestionsIntoTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

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

    // ---------------- form dialog (nicer) ----------------
    private static class QuestionFormDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private final String fixedId;
        private Question result = null;

        private JTextArea textArea = new JTextArea(4, 36);
        private JTextField aField = new JTextField(30);
        private JTextField bField = new JTextField(30);
        private JTextField cField = new JTextField(30);
        private JTextField dField = new JTextField(30);

        private JComboBox<QuestionLevel> levelBox = new JComboBox<>(QuestionLevel.values());
        private JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        public QuestionFormDialog(JFrame owner, Question existing, String idToUse) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            this.fixedId = idToUse;
            build(existing);
            pack();
            setLocationRelativeTo(owner);
        }

        private void build(Question q) {
            setLayout(new BorderLayout(12, 12));

            JPanel top = new JPanel(new BorderLayout());
            top.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
            JLabel idLbl = new JLabel("ID: " + fixedId);
            idLbl.setFont(idLbl.getFont().deriveFont(Font.BOLD, 14f));
            top.add(idLbl, BorderLayout.WEST);
            add(top, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;

            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            int y = 0;

            addRow(form, gc, y++, "Difficulty:", levelBox);

            gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("Question:"), gc);
            gc.gridx = 1; gc.gridy = y++; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            form.add(new JScrollPane(textArea), gc);

            addRow(form, gc, y++, "Option A:", aField);
            addRow(form, gc, y++, "Option B:", bField);
            addRow(form, gc, y++, "Option C:", cField);
            addRow(form, gc, y++, "Option D:", dField);
            addRow(form, gc, y++, "Correct:", correctBox);

            if (q != null) {
                textArea.setText(q.getText());
                levelBox.setSelectedItem(q.getLevel());
                List<String> ops = q.getOptions();
                aField.setText(ops.get(0));
                bField.setText(ops.get(1));
                cField.setText(ops.get(2));
                dField.setText(ops.get(3));
                correctBox.setSelectedIndex(q.getCorrectIndex());
            } else {
                levelBox.setSelectedItem(QuestionLevel.EASY);
                correctBox.setSelectedIndex(0);
            }

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

            JButton cancel = new JButton("Cancel");
            JButton save = new JButton("Save");

            cancel.addActionListener(e -> { result = null; dispose(); });

            save.addActionListener(e -> {
                String text = textArea.getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Question text is required.");
                    return;
                }

                List<String> opts = new ArrayList<>();
                opts.add(aField.getText().trim());
                opts.add(bField.getText().trim());
                opts.add(cField.getText().trim());
                opts.add(dField.getText().trim());

                for (String s : opts) {
                    if (s.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "All options (A-D) are required.");
                        return;
                    }
                }

                QuestionLevel lvl = (QuestionLevel) levelBox.getSelectedItem();
                int correctIdx = correctBox.getSelectedIndex();

                result = new Question(fixedId, text, opts, correctIdx, lvl);
                dispose();
            });

            buttons.add(cancel);
            buttons.add(save);
            add(buttons, BorderLayout.SOUTH);
        }

        private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent comp) {
            gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            p.add(new JLabel(label), gc);
            gc.gridx = 1; gc.gridy = y; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            p.add(comp, gc);
        }

        public Question showAndGet() {
            setVisible(true);
            return result;
        }
    }
}
