package view;

import model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class QuestionDialog extends JDialog {

    private boolean answered = false;
    private boolean correct  = false;

    public QuestionDialog(JFrame owner, Question question) {
        super(owner, "Question", true); // modal
        buildUI(question);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(450, 260));
    }

    private void buildUI(Question question) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        // ===== Title / text =====
        JPanel center = new JPanel();
        center.setBackground(new Color(12, 16, 35));
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Answer the question:");
        lblTitle.setForeground(new Color(230, 230, 255));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txt = new JTextArea(question.getText());
        txt.setWrapStyleWord(true);
        txt.setLineWrap(true);
        txt.setEditable(false);
        txt.setFocusable(false);
        txt.setOpaque(false);
        txt.setForeground(new Color(210, 215, 240));
        txt.setFont(txt.getFont().deriveFont(14f));
        txt.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        txt.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblTitle);
        center.add(Box.createVerticalStrut(6));
        center.add(txt);

        getContentPane().add(center, BorderLayout.NORTH);

        // ===== Answer buttons (A, B, C, D) =====
        JPanel answersPanel = new JPanel();
        answersPanel.setBackground(new Color(12, 16, 35));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 12, 20));
        answersPanel.setLayout(new GridLayout(2, 2, 10, 8));

        List<String> opts = question.getOptions();

        for (int i = 0; i < opts.size(); i++) {
            final int idx = i;
            String label = switch (i) {
                case 0 -> "A. " + opts.get(i);
                case 1 -> "B. " + opts.get(i);
                case 2 -> "C. " + opts.get(i);
                case 3 -> "D. " + opts.get(i);
                default -> opts.get(i);
            };

            JButton btn = new JButton(label);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                answered = true;
                correct = (idx == question.getCorrectIndex());
                dispose();
            });

            answersPanel.add(btn);
        }

        getContentPane().add(answersPanel, BorderLayout.CENTER);

        // ===== Bottom: cancel button =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(12, 16, 35));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> {
            // user closed without answering → no effect
            answered = false;
            dispose();
        });

        bottom.add(cancel);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Show the dialog and return:
     *  - TRUE  if answered correctly
     *  - FALSE if answered incorrectly
     *  - null  if user cancelled (ESC / X / Cancel)
     */
    public Boolean showAndGetResult() {
        setVisible(true); // blocks until dispose()
        return answered ? Boolean.valueOf(correct) : null;
    }
}
