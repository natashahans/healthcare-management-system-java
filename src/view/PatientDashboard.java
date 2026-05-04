package view;

import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PatientDashboard extends JPanel {
    public PatientDashboard(String name, String nhs) {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- WELCOME SECTION ---
        JPanel welcomeCard = Theme.createCard();
        welcomeCard.setLayout(new BorderLayout());

        JLabel lblWelcome = new JLabel("Welcome back, " + name);
        lblWelcome.setFont(Theme.HEADER_FONT);
        lblWelcome.setForeground(Theme.NAV_BACKGROUND);

        JLabel lblNhs = new JLabel("NHS Identity Verified: " + nhs);
        lblNhs.setFont(Theme.BODY_FONT);
        lblNhs.setForeground(Color.GRAY);

        welcomeCard.add(lblWelcome, BorderLayout.NORTH);
        welcomeCard.add(lblNhs, BorderLayout.SOUTH);

        add(welcomeCard, BorderLayout.NORTH);

        // --- QUICK TIPS / INFO SECTION ---
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(Theme.createStatCard("Your Status", "ACTIVE", Theme.SUCCESS));
        infoPanel.add(Theme.createStatCard("Primary GP", "Dr. Thompson", Theme.ACCENT_BLUE));

        add(infoPanel, BorderLayout.CENTER);
    }
}