package view;

import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ClinicalPanel - Fully aligned with Requirements 1, 4, and 6.
 */
public class ClinicalPanel extends JPanel {
    private final JButton btnVitals = new JButton("Record Patient Vitals");
    private final JButton btnPrescribe = new JButton("Issue New Prescription");
    private final JButton btnRefer = new JButton("Create Specialist Referral");

    private final JButton btnHistory = new JButton("View Patient Records");

    private final JButton btnViewReferrals = new JButton("View Assigned Referrals");
    private final JTextArea statusArea = new JTextArea(8, 40);

    public ClinicalPanel(String name, String id, String subRole, String specialty) {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- REQUIREMENT 1 & 6: IDENTITY HEADER ---
        JPanel header = Theme.createCard();
        header.setLayout(new GridLayout(1, 2));

        JPanel left = new JPanel(new GridLayout(0, 1)); left.setOpaque(false);
        JLabel lblName = new JLabel("Logged in as: " + name + " (" + id + ")");
        lblName.setFont(Theme.HEADER_FONT);

        JLabel lblRole = new JLabel("Role: " + subRole + " | Specialty: " + specialty);
        lblRole.setForeground(Theme.ACCENT_BLUE);
        lblRole.setFont(Theme.NAV_FONT);

        left.add(lblName);
        left.add(lblRole);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false);
        JLabel badge = new JLabel(" AUTHORIZED " + subRole.toUpperCase() + " ");
        badge.setOpaque(true);
        badge.setBackground(Theme.SUCCESS);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        right.add(badge);

        header.add(left); header.add(right);
        add(header, BorderLayout.NORTH);

        // --- ROLE-BASED TASKS ---
        JPanel centerGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        centerGrid.setOpaque(false);

        if ("Nurse".equalsIgnoreCase(subRole)) {
            addClinicalAction(centerGrid, "Vitals Management", btnVitals, Theme.ACCENT_BLUE);
            // REQUIREMENT 4: Label alignment
            addClinicalAction(centerGrid, "Patient Tracking", btnHistory, Theme.NAV_BACKGROUND);

        } else if ("GP".equalsIgnoreCase(subRole)) {
            addClinicalAction(centerGrid, "Pharmacy Authority", btnPrescribe, Theme.ACCENT_BLUE);
            addClinicalAction(centerGrid, "Referral Authority", btnRefer, Theme.SUCCESS);
            addClinicalAction(centerGrid, "View Patient Records", btnHistory, Theme.NAV_BACKGROUND);

        } else if ("Specialist".equalsIgnoreCase(subRole)) {
            addClinicalAction(centerGrid, "Inbound Referrals", btnViewReferrals, Theme.ACCENT_BLUE);
            addClinicalAction(centerGrid, "View Patient Records", btnHistory, Theme.NAV_BACKGROUND);
        }

        add(centerGrid, BorderLayout.CENTER);

        statusArea.setEditable(false);
        statusArea.setFont(Theme.BODY_FONT);
        JScrollPane scroll = new JScrollPane(statusArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Active Clinical Session Log"));
        add(scroll, BorderLayout.SOUTH);
    }

    private void addClinicalAction(JPanel parent, String title, JButton btn, Color accent) {
        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(Color.GRAY);
        Theme.stylePrimaryButton(btn, accent);
        card.add(lbl, BorderLayout.NORTH);
        card.add(btn, BorderLayout.CENTER);
        parent.add(card);
    }

    public JButton getBtnVitals() { return btnVitals; }
    public JButton getBtnPrescribe() { return btnPrescribe; }
    public JButton getBtnRefer() { return btnRefer; }
    public JButton getBtnHistory() { return btnHistory; }
    public JButton getBtnViewReferrals() { return btnViewReferrals; }

    public void logStatus(String msg) {
        statusArea.append("[" + new java.util.Date().toString().substring(11, 19) + "] AUTH > " + msg + "\n");
    }
}