package view.style;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The Branding Engine for HMS Global.
 * High-performance UI configuration with dashboard components and detail-view helpers.
 */
public class Theme {
    // Medical Professional Palette
    public static final Color NAV_BACKGROUND = new Color(31, 40, 51);
    public static final Color NAV_ACTIVE = new Color(52, 152, 219);
    public static final Color MAIN_BG = new Color(245, 246, 250);
    public static final Color TEXT_DARK = new Color(44, 62, 80);
    public static final Color SUCCESS = new Color(46, 204, 113);
    public static final Color DANGER = new Color(231, 76, 60);
    public static final Color ACCENT_BLUE = new Color(52, 152, 219);

    // Standardized Fonts
    public static final Font LOGO_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font NAV_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    /**
     * Full-width sidebar buttons for professional navigation highlighting.
     */
    public static void styleSidebarButton(JButton btn) {
        btn.setMaximumSize(new Dimension(260, 50));
        btn.setPreferredSize(new Dimension(260, 50));
        btn.setBackground(NAV_BACKGROUND);
        btn.setForeground(new Color(191, 203, 217));
        btn.setFont(NAV_FONT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(0, 30, 0, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles primary action buttons (Save, Register, Authorize).
     */
    public static void stylePrimaryButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Standardizes the look and feel of JTables across the application.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(BODY_FONT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(NAV_BACKGROUND);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    /**
     * Creates a white card-style container with a subtle border and padding.
     */
    public static JPanel createCard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 25, 25, 25)
        ));
        return panel;
    }

    /**
     * Creates a high-end statistic card for the Reports screen.
     */
    public static JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(220, 130));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a professional label-value row for Read-Only detail views.
     */
    public static JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(lbl);
        row.add(val);
        return row;
    }
}