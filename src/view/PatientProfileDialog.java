package view;

import model.Patient;
import view.style.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * PatientProfileDialog - Professional UX Implementation.
 */
public class PatientProfileDialog extends JDialog {
    private final JTextField[] fields = new JTextField[14];
    private final String[] labels = {
            "Patient ID", "First Name", "Last Name", "D.O.B (YYYY-MM-DD)", "NHS Number",
            "Gender", "Phone", "Email", "Address", "Postcode",
            "Next of Kin", "NoK Phone", "Registration Date", "GP Surgery ID"
    };
    private final JButton btnSave = new JButton("COMMIT TO DATABASE");
    private final JButton btnAdminRefer = new JButton("CREATE REFERRAL");

    public PatientProfileDialog(JFrame parent, Patient p, boolean editable) {
        super(parent, "Clinical Patient Record | HMS Global", true);

        setSize(750, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        setResizable(true);

        // HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        header.setBackground(Theme.ACCENT_BLUE);
        JLabel title = new JLabel(editable ? "REGISTRATION & EDITING" : "PATIENT ENCYCLOPEDIA (READ-ONLY)");
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // FORM CONTAINER
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBorder(new EmptyBorder(15, 20, 15, 20));
        formContent.setBackground(Color.WHITE);

        formContent.add(createSection("Medical Identity", 0, 5, p, editable));
        formContent.add(Box.createVerticalStrut(10));
        formContent.add(createSection("Communication & Address", 5, 10, p, editable));
        formContent.add(Box.createVerticalStrut(10));
        formContent.add(createSection("Emergency & Clinical Support", 10, 14, p, editable));

        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footer.setBackground(new Color(245, 246, 250));

        // FIX: THE REFERRAL BUTTON ONLY SHOWS UP IN VIEW MODE (READ ONLY)
        if (!editable && p != null) {
            Theme.stylePrimaryButton(btnAdminRefer, Theme.ACCENT_BLUE);
            footer.add(btnAdminRefer);
        }

        if (editable) {
            Theme.stylePrimaryButton(btnSave, Theme.SUCCESS);
            footer.add(btnSave);
        }

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSection(String title, int start, int end, Patient p, boolean editable) {
        JPanel sec = new JPanel(new GridLayout(0, 2, 20, 15));
        sec.setBackground(Color.WHITE);
        sec.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Theme.ACCENT_BLUE
        ));

        String[] pData = (p == null) ? new String[14] : new String[]{
                p.getId(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(), p.getNhsNumber(),
                p.getGender(), p.getPhone(), p.getEmail(), p.getAddress(), p.getPostcode(),
                p.getNextOfKin(), p.getNokPhone(), p.getRegDate(), p.getGpSurgeryId()
        };

        for (int i = start; i < end; i++) {
            JPanel item = new JPanel(new BorderLayout(0, 5));
            item.setOpaque(false);
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(Color.GRAY);
            fields[i] = new JTextField(pData[i] == null ? "" : pData[i]);
            fields[i].setEditable(editable);
            fields[i].setPreferredSize(new Dimension(0, 30));

            if (!editable) {
                fields[i].setBorder(null);
                fields[i].setBackground(Color.WHITE);
                fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            item.add(lbl, BorderLayout.NORTH);
            item.add(fields[i], BorderLayout.CENTER);
            sec.add(item);
        }
        return sec;
    }

    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnAdminRefer() { return btnAdminRefer; }
    public Patient getPatientFromForm() {
        return new Patient(
                fields[0].getText(), fields[1].getText(), fields[2].getText(), fields[3].getText(),
                fields[4].getText(), fields[5].getText(), fields[6].getText(), fields[7].getText(),
                fields[8].getText(), fields[9].getText(), fields[10].getText(), fields[11].getText(),
                fields[12].getText(), fields[13].getText()
        );
    }
}