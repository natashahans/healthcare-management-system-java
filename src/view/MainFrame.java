package view;

import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final JPanel sidebar;
    private final JPanel workspace;
    private final CardLayout cardLayout;
    private final JButton logoutBtn;
    private final List<JButton> navButtons = new ArrayList<>();

    public MainFrame() {
        setTitle("Healthcare Management System | Global");

        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 800));
        sidebar.setBackground(Theme.NAV_BACKGROUND);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("HMS GLOBAL");
        logo.setForeground(Color.WHITE);
        logo.setFont(Theme.LOGO_FONT);
        logo.setBorder(new EmptyBorder(40, 25, 40, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        cardLayout = new CardLayout();
        workspace = new JPanel(cardLayout);
        workspace.setBackground(Theme.MAIN_BG);
        workspace.setBorder(new EmptyBorder(30, 40, 30, 40));
        add(workspace, BorderLayout.CENTER);

        logoutBtn = new JButton("SIGN OUT");
        Theme.styleSidebarButton(logoutBtn);
        logoutBtn.setForeground(Theme.DANGER);

        add(sidebar, BorderLayout.WEST);
    }

    public void addNav(String name) {
        JButton btn = new JButton(name);
        Theme.styleSidebarButton(btn);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(_ -> {
            cardLayout.show(workspace, name);
            highlightButton(btn);
        });

        navButtons.add(btn);
        sidebar.add(btn);
    }

    private void highlightButton(JButton activeBtn) {
        for (JButton btn : navButtons) {
            btn.setBackground(Theme.NAV_BACKGROUND);
        }
        activeBtn.setBackground(Theme.NAV_ACTIVE);
    }

    public void addScreen(JPanel panel, String name) {
        workspace.add(panel, name);
    }

    public void finalizeSidebar() {
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(20));
        // Highlight first button by default
        if (!navButtons.isEmpty()) highlightButton(navButtons.get(0));
    }

    public JButton getLogoutBtn() { return logoutBtn; }
}