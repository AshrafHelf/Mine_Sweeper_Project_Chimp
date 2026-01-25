package view;

import enums.QuestionLevel;
import model.Question;
import model.QuestionService;
import view.theme.UIFactory;
import view.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyEvent;
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

        setUndecorated(true);
        setResizable(false);

        buildUI();
        loadQuestionsIntoTable();

        pack();
        setSize(980, 560);
        setLocationRelativeTo(owner);

        // ESC closes
        getRootPane().registerKeyboardAction(
                e -> { AudioManager.playSfx("button.wav"); dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel card = UIFactory.glassCard();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        // ---------- header ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(UIFactory.dialogTitle("📚 Question Bank"));
        titles.add(Box.createVerticalStrut(2));
        titles.add(UIFactory.dialogSubtitle("Add / Edit / Delete (saved to data/Questions.csv)"));

        JButton closeX = UIFactory.miniIconButton("✕");
        closeX.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        top.add(titles, BorderLayout.WEST);
        top.add(closeX, BorderLayout.EAST);

        // ---------- table ----------
        String[] cols = {"ID", "Question Text", "Difficulty", "Correct Answer"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        // FIX: JTable must be opaque + have a solid base to avoid paint artifacts
        table.setOpaque(true);
        table.setBackground(new Color(10, 10, 10)); // solid base (prevents glitch)
        table.setForeground(new Color(235, 235, 235, 220));
        table.setSelectionBackground(new Color(255, 255, 255, 30));
        table.setSelectionForeground(Theme.TEXT);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);

        // FIX: header should be opaque too
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 0, 170));
        header.setForeground(Theme.TEXT);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);

        // FIX: viewport must paint a base (not transparent)
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(new Color(10, 10, 10));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(6, 0, 0, 0));
        center.add(scroll, BorderLayout.CENTER);

        // ---------- right buttons ----------
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(6, 10, 0, 0));

        addBtn = UIFactory.primaryButton("Add");
        editBtn = UIFactory.glassButton("Edit");
        deleteBtn = UIFactory.glassButton("Delete");
        closeBtn = UIFactory.glassButton("Close");

        Dimension btnSize = new Dimension(170, 40);
        for (JButton b : new JButton[]{addBtn, editBtn, deleteBtn, closeBtn}) {
            b.setMaximumSize(btnSize);
            b.setPreferredSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        right.add(addBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(editBtn);
        right.add(Box.createVerticalStrut(10));
        right.add(deleteBtn);
        right.add(Box.createVerticalGlue());
        right.add(closeBtn);

        // actions
        closeBtn.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void onAdd() {
        AudioManager.playSfx("button.wav");
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

        AudioManager.playSfx("button.wav");
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

        AudioManager.playSfx("button.wav");
        qService.deleteById(id);
        loadQuestionsIntoTable();
    }

    private void loadQuestionsIntoTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        List<Question> questions = qService.getAll();
        for (Question q : questions) {
            String correct = "";
            try {
                correct = q.getOptions().get(q.getCorrectIndex());
            } catch (Exception ignored) {}

            model.addRow(new Object[]{
                    q.getId(),
                    q.getText(),
                    q.getLevel().name(),
                    correct
            });
        }
    }

    // ---------------- form dialog (glass) ----------------
    private static class QuestionFormDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private final String fixedId;
        private Question result = null;

        private final JTextArea textArea = new JTextArea(4, 38);
        private final JTextField aField = new JTextField(32);
        private final JTextField bField = new JTextField(32);
        private final JTextField cField = new JTextField(32);
        private final JTextField dField = new JTextField(32);

        private final JComboBox<QuestionLevel> levelBox = new JComboBox<>(QuestionLevel.values());
        private final JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        QuestionFormDialog(JFrame owner, Question existing, String idToUse) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            setUndecorated(true);
            setResizable(false);
         // FIX: remove default white square background for undecorated dialog
            setBackground(new Color(0, 0, 0, 0));
            getRootPane().setOpaque(false);

            this.fixedId = idToUse;

            JPanel root = new JPanel(new BorderLayout());
            root.setOpaque(false);
            root.setBorder(new EmptyBorder(14, 14, 14, 14));

            JPanel card = UIFactory.glassCard();
            card.setLayout(new BorderLayout(12, 12));
            card.setBorder(new EmptyBorder(16, 18, 14, 18));

            // header
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);

            JPanel titles = new JPanel();
            titles.setOpaque(false);
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            titles.add(UIFactory.dialogTitle(existing == null ? "➕ Add Question" : "✏️ Edit Question"));
            titles.add(Box.createVerticalStrut(2));
            titles.add(UIFactory.dialogSubtitle("ID: " + fixedId));

            JButton closeX = UIFactory.miniIconButton("✕");
            closeX.addActionListener(e -> { AudioManager.playSfx("button.wav"); result = null; dispose(); });

            top.add(titles, BorderLayout.WEST);
            top.add(closeX, BorderLayout.EAST);

            // form panel
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(7, 6, 7, 6);
            gc.anchor = GridBagConstraints.WEST;

            UIFactory.styleTextField(aField);
            UIFactory.styleTextField(bField);
            UIFactory.styleTextField(cField);
            UIFactory.styleTextField(dField);
            UIFactory.styleCombo(levelBox);
            UIFactory.styleCombo(correctBox);

            UIFactory.styleTextArea(textArea);
            JScrollPane qScroll = new JScrollPane(textArea);
            qScroll.setBorder(BorderFactory.createEmptyBorder());
            qScroll.getViewport().setBackground(new Color(0, 0, 0, 0));
            qScroll.setOpaque(false);

            int y = 0;
            addRow(form, gc, y++, "Difficulty:", levelBox);
            addRow(form, gc, y++, "Question:", qScroll);
            addRow(form, gc, y++, "Option A:", aField);
            addRow(form, gc, y++, "Option B:", bField);
            addRow(form, gc, y++, "Option C:", cField);
            addRow(form, gc, y++, "Option D:", dField);
            addRow(form, gc, y++, "Correct:", correctBox);

            // populate
            if (existing != null) {
                textArea.setText(existing.getText());
                levelBox.setSelectedItem(existing.getLevel());
                List<String> ops = existing.getOptions();
                if (ops != null && ops.size() >= 4) {
                    aField.setText(ops.get(0));
                    bField.setText(ops.get(1));
                    cField.setText(ops.get(2));
                    dField.setText(ops.get(3));
                }
                correctBox.setSelectedIndex(existing.getCorrectIndex());
            } else {
                levelBox.setSelectedItem(QuestionLevel.EASY);
                correctBox.setSelectedIndex(0);
            }

            // footer
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            footer.setOpaque(false);

            JButton cancel = UIFactory.glassButton("Cancel");
            JButton save   = UIFactory.primaryButton("Save");

            cancel.addActionListener(e -> { AudioManager.playSfx("button.wav"); result = null; dispose(); });

            save.addActionListener(e -> {
                AudioManager.playSfx("button.wav");

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

            footer.add(cancel);
            footer.add(save);

            card.add(top, BorderLayout.NORTH);
            card.add(form, BorderLayout.CENTER);
            card.add(footer, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            setContentPane(root);

            // ESC cancel
            getRootPane().registerKeyboardAction(
                    e -> { AudioManager.playSfx("button.wav"); result = null; dispose(); },
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );

            pack();
            setSize(820, 520);
            setLocationRelativeTo(owner);
        }

        private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent comp) {
            JLabel lab = new JLabel(label);
            lab.setForeground(new Color(235, 235, 235, 200));
            lab.setFont(new Font("SansSerif", Font.BOLD, 12));

            gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            p.add(lab, gc);

            gc.gridx = 1; gc.gridy = y; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            p.add(comp, gc);
        }

        Question showAndGet() {
            setVisible(true);
            return result;
        }
    }
}
