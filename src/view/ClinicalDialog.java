package view;

import model.Patient;
import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A professional, high-end dialog for clinicians to issue prescriptions or referrals.
 */
public class ClinicalDialog extends JDialog {
    private final JComboBox<String> urgencyBox = new JComboBox<>(new String[]{"Routine", "Urgent", "Emergency"});
    private final JTextArea summaryArea = new JTextArea(8, 20);
    private final JButton btnSubmit = new JButton("AUTHORIZE & GENERATE REPORT");

    public ClinicalDialog(JFrame parent, Patient p, String type) {
        super(parent, type + " - " + p.getFullName(), true);
        setSize(550, 650);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Section
        JPanel header = new JPanel(new GridLayout(0, 1));
        header.setBackground(Theme.ACCENT_BLUE);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("CLINICAL " + type.toUpperCase());
        title.setForeground(Color.WHITE);
        title.setFont(Theme.HEADER_FONT);
        header.add(title);

        JLabel patientLabel = new JLabel("Patient: " + p.getFullName() + " | NHS: " + p.getNhsNumber());
        patientLabel.setForeground(Color.WHITE);
        header.add(patientLabel);

        add(header, BorderLayout.NORTH);

        // Form Section
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(30, 30, 30, 30));
        form.setOpaque(false);

        form.add(new JLabel("Urgency Level:"));
        form.add(Box.createVerticalStrut(10));
        form.add(urgencyBox);

        form.add(Box.createVerticalStrut(30));

        form.add(new JLabel("Clinical Summary & Reason for Referral:"));
        form.add(Box.createVerticalStrut(10));
        summaryArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        form.add(new JScrollPane(summaryArea));

        add(form, BorderLayout.CENTER);

        // Footer Section
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));
        footer.setOpaque(false);
        Theme.stylePrimaryButton(btnSubmit, Theme.ACCENT_BLUE);
        footer.add(btnSubmit);

        add(footer, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
    }

    public String getUrgency() { return (String) urgencyBox.getSelectedItem(); }
    public String getSummary() { return summaryArea.getText(); }
    public JButton getBtnSubmit() { return btnSubmit; }
}