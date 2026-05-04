package view;

import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LoginView - The Secure Entry Tier for HMS Global.
 */
public class LoginView extends JDialog {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JButton loginBtn;

    public LoginView(JFrame parent) {
        super(parent, "HMS Global | Secure Access Gateway", true);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Theme.MAIN_BG);
        setResizable(false);

        // --- THE LOGIN CARD ---
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(45, 50, 45, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        // 1. Title
        JLabel title = new JLabel("SYSTEM ACCESS", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Theme.TEXT_DARK);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(title, gbc);

        // 2. Identity Label - NEUTRAL FOR BOTH STAFF & PATIENTS
        JLabel userLabel = new JLabel("User Identity (Email or NHS Number):");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 2, 0);
        card.add(userLabel, gbc);

        // 3. Identity Input Field
        userField = new JTextField(20);
        userField.setPreferredSize(new Dimension(320, 35));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(userField, gbc);

        // 4. Password Label
        JLabel passLabel = new JLabel("Security Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 2, 0);
        card.add(passLabel, gbc);

        // 5. Password Field
        passField = new JPasswordField(20);
        passField.setPreferredSize(new Dimension(320, 35));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 35, 0);
        card.add(passField, gbc);

        // 6. Access Button
        loginBtn = new JButton("AUTHORIZE ACCESS");
        Theme.stylePrimaryButton(loginBtn, Theme.ACCENT_BLUE);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 25, 0);
        card.add(loginBtn, gbc);

        // --- ACCURATE 4-LINE SYSTEM NOTICE ---
        JPanel helpPanel = new JPanel(new GridBagLayout());
        helpPanel.setBackground(new Color(232, 241, 250));
        helpPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_BLUE, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.anchor = GridBagConstraints.WEST;
        hgbc.gridx = 0;

        JLabel helpTitle = new JLabel("SECURE GATEWAY NOTICE:");
        helpTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        helpTitle.setForeground(Theme.ACCENT_BLUE);
        hgbc.gridy = 0; hgbc.insets = new Insets(0, 0, 8, 0);
        helpPanel.add(helpTitle, hgbc);

        // Accurate instructions for each specific role
        JLabel l1 = new JLabel("• Administrative Staff: Use registered email address.");
        JLabel l2 = new JLabel("• Medical Clinicians (GP, Nurse, Specialist): Use email.");
        JLabel l3 = new JLabel("• Patients: Use NHS Number or registered email.");
        JLabel l4 = new JLabel("• Security Protocol: Use universal password 'pass'.");

        Font f = new Font("Segoe UI", Font.ITALIC, 11);
        l1.setFont(f); l2.setFont(f); l3.setFont(f); l4.setFont(f);

        hgbc.gridy = 1; helpPanel.add(l1, hgbc);
        hgbc.gridy = 2; helpPanel.add(l2, hgbc);
        hgbc.gridy = 3; helpPanel.add(l3, hgbc);
        hgbc.gridy = 4; helpPanel.add(l4, hgbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 0, 0);
        card.add(helpPanel, gbc);

        // --- UX ENHANCEMENT: ENTER KEY MAPPING ---
        this.getRootPane().setDefaultButton(loginBtn);

        add(card);
        pack();
        setLocationRelativeTo(null);
    }

    public JTextField getUserField() { return userField; }
    public JPasswordField getPassField() { return passField; }
    public JButton getLoginBtn() { return loginBtn; }
}