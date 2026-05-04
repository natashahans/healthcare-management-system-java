package controller;

import model.*;
import model.services.*;
import view.*;
import view.style.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AppController - The "C" in MVC.
 * Master Global Version: Fully synchronized role-based access control (RBAC).
 * Supports Admin oversight, Patient portals, and specialized Clinician workflows.
 * Tier 2 Services (AuthService, AppointmentManager, PrescriptionService) integrated.
 */
public class AppController {
    private final DataAccessLayer dal;
    private final AuthenticationService authService;
    private final AppointmentManager apptManager;
    private final PrescriptionService presService; // SERVICE TIER INTEGRATION
    private final MainFrame mainFrame;

    private String userRole, subRole, loggedInUserName, loggedInUserId, loggedInSpecialty;

    public AppController() {
        // Tier 3: Data Tier
        this.dal = new DataAccessLayer();

        // Tier 2: Business Logic Tier
        this.authService = new AuthenticationService(dal);
        this.apptManager = new AppointmentManager(dal);
        this.presService = new PrescriptionService(dal);

        // Tier 1: Presentation Tier
        this.mainFrame = new MainFrame();
        showLogin();
    }

    /**
     * ENFORCED LOGIN: Uses AuthenticationService to validate against CSV.
     * Supports Enter-key submission via LoginView's DefaultButton property.
     */
    private void showLogin() {
        LoginView login = new LoginView(mainFrame);

        login.getLoginBtn().addActionListener(e -> {
            String user = login.getUserField().getText().trim();
            String pass = new String(login.getPassField().getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(login, "Security Alert: Both fields required.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Person authUser = authService.authenticate(user, pass);

            if (authUser != null) {
                this.loggedInUserId = authUser.getId();
                this.loggedInUserName = authUser.getFullName();

                if (authUser instanceof AdministrativeStaff) {
                    userRole = "ADMIN";
                } else if (authUser instanceof Clinician) {
                    userRole = "CLINICIAN";
                    Clinician c = (Clinician) authUser;
                    this.loggedInSpecialty = c.getSpecialty();
                    // REQUIREMENT 2: DERIVE ROLE FROM SPECIALTY COLUMN
                    this.subRole = dal.deriveRole(loggedInSpecialty);
                } else if (authUser instanceof Patient) {
                    userRole = "PATIENT";
                }

                login.dispose();
                setupDashboard();
            } else {
                JOptionPane.showMessageDialog(login, "Authentication Failed: User record not found or password incorrect.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        login.setVisible(true);
    }

    private void setupDashboard() {
        mainFrame.getContentPane().revalidate();

        if ("ADMIN".equals(userRole)) {
            mainFrame.addNav("Patients"); mainFrame.addNav("Appointments"); mainFrame.addNav("Reports");
            mainFrame.addScreen(createPatientScreen(), "Patients");
            mainFrame.addScreen(createAppointmentScreen(), "Appointments");
            mainScreenReports();
        } else if ("CLINICIAN".equals(userRole)) {
            mainFrame.addNav("My Schedule"); mainFrame.addNav("Clinical Tasks");
            mainFrame.addScreen(createAppointmentScreen(), "My Schedule");
            mainScreenClinical();
        } else {
            // Default Patient View
            mainFrame.addNav("Dashboard"); mainFrame.addNav("My Appointments");
            mainFrame.addScreen(new PatientDashboard(loggedInUserName, loggedInUserId), "Dashboard");
            mainFrame.addScreen(createPatientSpecificAppointmentScreen(), "My Appointments");
        }

        mainFrame.getLogoutBtn().addActionListener(_ -> { mainFrame.dispose(); new AppController(); });
        mainFrame.finalizeSidebar();
        mainFrame.setVisible(true);
    }

    private void mainScreenReports() {
        mainFrame.addScreen(createReportScreen(), "Reports");
    }

    private JPanel createReportScreen() {
        JPanel main = new JPanel(new BorderLayout(0, 30)); main.setOpaque(false);
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); statsPanel.setOpaque(false);

        List<Patient> pats = dal.loadPatients();
        List<Clinician> clins = dal.loadClinicians();

        statsPanel.add(Theme.createStatCard("Total Patients", String.valueOf(pats.size()), Theme.ACCENT_BLUE));
        statsPanel.add(Theme.createStatCard("Clinician Count", String.valueOf(clins.size()), Theme.SUCCESS));
        main.add(statsPanel, BorderLayout.NORTH);

        JPanel card = Theme.createCard(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Administrative Audit Console"); title.setFont(Theme.SUBHEADER_FONT);
        JButton btn = new JButton("GENERATE FULL SYSTEM REPORT"); Theme.stylePrimaryButton(btn, Theme.ACCENT_BLUE);

        btn.addActionListener(_ -> {
            String reportText = dal.generateSystemReport(pats.size(), clins.size(), ReferralService.getInstance().getReferralQueue().size());
            JOptionPane.showMessageDialog(mainFrame, new JScrollPane(new JTextArea(reportText, 20, 45)), "Audit Preview", JOptionPane.PLAIN_MESSAGE);
        });

        card.add(title); card.add(Box.createVerticalStrut(20)); card.add(btn);
        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private JPanel createPatientScreen() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"First Name", "Last Name", "NHS Number"}, 0);
        refreshPatientTable(model);
        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel panel = createBaseTablePanel("Patient Registry", table, () -> {
            PatientProfileDialog diag = new PatientProfileDialog(mainFrame, null, true);
            diag.getBtnSave().addActionListener(_ -> {
                Patient newP = diag.getPatientFromForm();
                // FIX: DUPLICATE VALIDATION
                if (dal.isDuplicatePatient(newP.getId(), newP.getNhsNumber())) {
                    JOptionPane.showMessageDialog(diag, "Registration Conflict: Patient ID or NHS Number already in use.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    dal.saveNewPatient(newP);
                    refreshPatientTable(model); diag.dispose();
                }
            });
            diag.setVisible(true);
        }, "Register New Patient", dal::deletePatient, 2, () -> refreshPatientTable(model));

        JPanel south = (JPanel) panel.getComponent(2);

        JButton btnView = new JButton("View Profile"); Theme.stylePrimaryButton(btnView, Theme.NAV_BACKGROUND);
        btnView.addActionListener(_ -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String nhs = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
                Patient p = dal.loadPatients().stream().filter(pat -> pat.getNhsNumber().equals(nhs)).findFirst().orElse(null);

                // FIXED: Set to 'false' (Read-Only) for View Profile so referral button shows
                PatientProfileDialog diag = new PatientProfileDialog(mainFrame, p, false);

                // FIX: ADMIN REFERRAL EXTENSION (ISSUE 6B)
                diag.getBtnAdminRefer().addActionListener(_2 -> {
                    diag.dispose();
                    openReferralFlow(p, "Administrative Referral");
                });

                diag.setVisible(true);
            }
        });

        JButton btnUpdate = new JButton("Modify Record"); Theme.stylePrimaryButton(btnUpdate, Theme.ACCENT_BLUE);
        btnUpdate.addActionListener(_ -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String nhs = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
                Patient p = dal.loadPatients().stream().filter(pat -> pat.getNhsNumber().equals(nhs)).findFirst().orElse(null);

                // Set to 'true' (Editable) for Modify mode - referral button is hidden here
                PatientProfileDialog diag = new PatientProfileDialog(mainFrame, p, true);
                diag.getBtnSave().addActionListener(_ -> {
                    dal.deletePatient(nhs); dal.saveNewPatient(diag.getPatientFromForm());
                    refreshPatientTable(model); diag.dispose();
                });
                diag.setVisible(true);
            }
        });

        // ACCESSIBLE REFER BUTTON ON MAIN DASHBOARD
        JButton btnQuickRefer = new JButton("Refer Patient"); Theme.stylePrimaryButton(btnQuickRefer, Theme.SUCCESS);
        btnQuickRefer.addActionListener(_ -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String nhs = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
                Patient p = dal.loadPatients().stream().filter(pat -> pat.getNhsNumber().equals(nhs)).findFirst().orElse(null);
                openReferralFlow(p, "Administrative Referral");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select a patient from the list to refer.");
            }
        });

        JButton btnFilter = new JButton("Filter List"); Theme.stylePrimaryButton(btnFilter, Theme.NAV_BACKGROUND);
        btnFilter.addActionListener(_ -> {
            String s = JOptionPane.showInputDialog(mainFrame, "Search by Name or NHS:");
            if (s != null) sorter.setRowFilter(RowFilter.regexFilter("(?i)" + s));
        });

        JButton btnClear = new JButton("Clear Filter"); Theme.stylePrimaryButton(btnClear, Theme.DANGER);
        btnClear.addActionListener(_ -> sorter.setRowFilter(null));

        south.add(btnView);
        south.add(btnUpdate);
        south.add(btnQuickRefer); // ADDED TO DASHBOARD FOR PROFESSIONAL ACCESS
        south.add(btnFilter);
        south.add(btnClear);
        return panel;
    }

    private void refreshPatientTable(DefaultTableModel model) {
        model.setRowCount(0);
        dal.loadPatients().forEach(p -> model.addRow(new Object[]{p.getFirstName(), p.getLastName(), p.getNhsNumber()}));
    }

    /**
     * CLINICIAN SCHEDULE FILTERING & PERMISSIONS
     * Role-based filtering and permission restriction (No reschedule for Clinicians).
     */
    private JPanel createAppointmentScreen() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Date", "Time", "Status", "Patient Name"}, 0);
        refreshApptTable(model);
        JTable table = new JTable(model);

        // PERMISSIONS: Disable Global Booking/Removal for Clinicians
        boolean isAdmin = "ADMIN".equals(userRole);
        boolean isClinician = "CLINICIAN".equals(userRole);

        Runnable addAction = isAdmin ? () -> {
            List<Patient> patients = dal.loadPatients();
            String[] names = patients.stream().map(Patient::getFullName).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(mainFrame, "Select Patient:", "Booking", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (sel != null) {
                JTextField d = new JTextField("2026-05-20"); JComboBox<String> t = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00"});
                if (JOptionPane.showConfirmDialog(mainFrame, new Object[]{"Date:", d, "Time:", t}, "Set Slot", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    String id = dal.generateNextId("A", "appointments.csv");
                    int idx = java.util.Arrays.asList(names).indexOf(sel);
                    Appointment a = new Appointment(id, patients.get(idx).getNhsNumber(), "C001", "S001", d.getText(), (String)t.getSelectedItem(), "15", "Routine", "Scheduled", "Admin Booking", "", "2026-01-13", "2026-01-13");
                    apptManager.bookAppointment(a); refreshApptTable(model);
                }
            }
        } : null;

        JPanel panel = createBaseTablePanel(isAdmin ? "Global Schedule" : "My Schedule", table,
                addAction, isAdmin ? "New Booking" : "",
                isAdmin ? dal::deleteAppointment : null, 0, () -> refreshApptTable(model));

        JPanel south = (JPanel) panel.getComponent(2);

        if (!isClinician) {
            JButton btnResched = new JButton("Reschedule"); Theme.stylePrimaryButton(btnResched, Theme.ACCENT_BLUE);
            btnResched.addActionListener(_ -> {
                int r = table.getSelectedRow();
                if (r != -1) {
                    String id = (String) model.getValueAt(table.convertRowIndexToModel(r), 0);
                    JTextField d = new JTextField("2026-06-01"); JComboBox<String> t = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00"});
                    if (JOptionPane.showConfirmDialog(mainFrame, new Object[]{"New Date:", d, "Pick Slot:", t}, "Reschedule", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        apptManager.reschedule(id, d.getText(), (String)t.getSelectedItem());
                        refreshApptTable(model);
                    }
                }
            });
            south.add(btnResched);
        }

        JButton btnCancel = new JButton("Cancel Slot"); Theme.stylePrimaryButton(btnCancel, Theme.DANGER);
        btnCancel.addActionListener(_ -> {
            int row = table.getSelectedRow();
            if (row != -1) { apptManager.cancel((String)model.getValueAt(row, 0)); refreshApptTable(model); }
        });

        south.add(btnCancel);
        return panel;
    }

    private void refreshApptTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Appointment> all = dal.loadAppointments();
        if ("CLINICIAN".equals(userRole)) {
            all = all.stream().filter(a -> a.getClinicianId().equals(loggedInUserId)).collect(Collectors.toList());
        } else if ("PATIENT".equals(userRole)) {
            all = all.stream().filter(a -> a.getPatientId().equals(loggedInUserId)).collect(Collectors.toList());
        }
        all.forEach(a -> model.addRow(new Object[]{a.getId(), a.getDate(), a.getTime(), a.getStatus(), dal.getPatientName(a.getPatientId())}));
    }

    /**
     * ALIGNED PATIENT APPOINTMENTS TAB
     * Implements Book, Reschedule, Cancel, and View with REASON field capture.
     */
    private JPanel createPatientSpecificAppointmentScreen() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Date", "Time", "Status"}, 0);
        refreshApptTable(model);
        JTable table = new JTable(model);

        Runnable addAction = () -> {
            JTextField dateF = new JTextField("2026-05-20");
            JComboBox<String> timeB = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00", "15:00"});
            JTextField reasonF = new JTextField(); // ADDED REASON INPUT

            Object[] form = {
                    "Preferred Date (YYYY-MM-DD):", dateF,
                    "Available Time Slot:", timeB,
                    "Reason for Visit:", reasonF
            };

            if (JOptionPane.showConfirmDialog(mainFrame, form, "Self Booking Portal", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String id = dal.generateNextId("A", "appointments.csv");
                String reason = reasonF.getText().trim().isEmpty() ? "Patient Consultation" : reasonF.getText();

                // Set patientId = loggedInUserId for strict permission enforcement
                Appointment a = new Appointment(id, loggedInUserId, "C001", "S001", dateF.getText(), (String)timeB.getSelectedItem(), "15", "Consultation", "Scheduled", reason, "", "2026-01-15", "2026-01-15");
                apptManager.bookAppointment(a);
                refreshApptTable(model);
                JOptionPane.showMessageDialog(mainFrame, "Appointment Booked Successfully.");
            }
        };

        JPanel panel = createBaseTablePanel("My Medical Appointments", table, addAction, "New Booking", null, -1, () -> refreshApptTable(model));
        JPanel south = (JPanel) panel.getComponent(2);

        JButton btnResched = new JButton("Reschedule"); Theme.stylePrimaryButton(btnResched, Theme.ACCENT_BLUE);
        btnResched.addActionListener(_ -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = (String) model.getValueAt(r, 0);
                String currentStatus = (String) model.getValueAt(r, 3);
                if (!"Scheduled".equalsIgnoreCase(currentStatus)) {
                    JOptionPane.showMessageDialog(mainFrame, "Error: Only 'Scheduled' appointments can be modified.");
                    return;
                }
                JTextField d = new JTextField("2026-06-01"); JComboBox<String> t = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00"});
                if (JOptionPane.showConfirmDialog(mainFrame, new Object[]{"New Date:", d, "Pick Slot:", t}, "Reschedule Appointment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    apptManager.reschedule(id, d.getText(), (String)t.getSelectedItem());
                    refreshApptTable(model);
                }
            }
        });

        JButton btnCancel = new JButton("Cancel Appointment"); Theme.stylePrimaryButton(btnCancel, Theme.DANGER);
        btnCancel.addActionListener(_ -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = (String) model.getValueAt(r, 0);
                String currentStatus = (String) model.getValueAt(r, 3);
                if (!"Scheduled".equalsIgnoreCase(currentStatus)) {
                    JOptionPane.showMessageDialog(mainFrame, "Error: This appointment is already " + currentStatus.toLowerCase() + ".");
                    return;
                }
                if (JOptionPane.showConfirmDialog(mainFrame, "Cancel this appointment?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    apptManager.cancel(id);
                    refreshApptTable(model);
                }
            }
        });

        JButton btnView = new JButton("View Details"); Theme.stylePrimaryButton(btnView, Theme.NAV_BACKGROUND);
        btnView.addActionListener(_ -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = (String) model.getValueAt(r, 0);
                showAppointmentDetails(dal.loadAppointments().stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null));
            }
        });

        south.add(btnResched); south.add(btnCancel); south.add(btnView);
        return panel;
    }

    private void showAppointmentDetails(Appointment a) {
        if (a == null) return;
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        p.add(Theme.createDetailRow("Booking ID", a.getId()));
        p.add(Theme.createDetailRow("Schedule", a.getDate() + " at " + a.getTime()));
        p.add(Theme.createDetailRow("Status", a.getStatus()));
        // REQUIREMENT 2: REASON VISIBLE IN DETAILS
        p.add(Theme.createDetailRow("Reason", a.getReason()));
        JOptionPane.showMessageDialog(mainFrame, p, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mainScreenClinical() {
        mainFrame.addScreen(createClinicalTasks(), "Clinical Tasks");
    }

    private JPanel createClinicalTasks() {
        // IDENTITY HEADER PASSED TO PANEL
        ClinicalPanel cp = new ClinicalPanel(loggedInUserName, loggedInUserId, subRole, loggedInSpecialty);

        cp.getBtnVitals().addActionListener(_ -> {
            List<Patient> patients = dal.loadPatients();
            String[] names = patients.stream().map(Patient::getFullName).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(mainFrame, "Select Patient for Vitals:", "Nurse Task", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);

            if (sel != null) {
                int idx = java.util.Arrays.asList(names).indexOf(sel);
                String nhs = patients.get(idx).getNhsNumber();
                JTextField bp = new JTextField("120/80"); JTextField hr = new JTextField("72");
                Object[] form = {"BP:", bp, "Heart Rate:", hr, "Temp:", new JTextField("36.6"), "Weight:", new JTextField("70kg")};
                if (JOptionPane.showConfirmDialog(mainFrame, form, "Record Vitals", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    dal.saveVitals(nhs, bp.getText(), hr.getText(), "36.6", "70kg");
                    cp.logStatus("Vitals recorded for: " + sel);
                    ReferralService.getInstance().updateElectronicHealthRecord(nhs, new Referral("VITALS", "2026-01-13", "Routine", "Logged"), "Nurse " + loggedInUserName);
                }
            }
        });

        // REQUIREMENT 6: REAL PERSISTENCE FOR PRESCRIPTIONS
        cp.getBtnPrescribe().addActionListener(_ -> {
            List<Patient> pats = dal.loadPatients();
            String[] names = pats.stream().map(Patient::getFullName).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(mainFrame, "Prescribe For:", "GP Authority", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (sel != null) {
                int idx = java.util.Arrays.asList(names).indexOf(sel);
                String med = JOptionPane.showInputDialog("Medication:");
                if (med != null) {
                    String id = "RX" + (System.currentTimeMillis() % 1000);
                    Prescription rx = new Prescription(id, "2026-01-13", med, "500mg", "Daily", "14", "Take after meal", "Boots Pharmacy");
                    presService.issuePrescription(rx, pats.get(idx).getNhsNumber(), loggedInUserId);
                    cp.logStatus("Prescription " + id + " persisted to CSV and TXT for " + sel);
                }
            }
        });

        cp.getBtnRefer().addActionListener(_ -> {
            List<Patient> pats = dal.loadPatients();
            String[] names = pats.stream().map(Patient::getFullName).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(mainFrame, "Refer Patient:", "GP Authority", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (sel != null) {
                int idx = java.util.Arrays.asList(names).indexOf(sel);
                openReferralFlow(pats.get(idx), "Clinical Referral");
            }
        });

        // SCROLLABLE SPECIALIST VIEW
        cp.getBtnViewReferrals().addActionListener(_ -> {
            List<String[]> myRef = dal.loadRawData("referrals.csv").stream()
                    .filter(r -> r.length > 3 && r[3].equals(loggedInUserId))
                    .collect(Collectors.toList());

            if (myRef.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No referrals assigned.");
            } else {
                DefaultTableModel m = new DefaultTableModel(new String[]{"Ref ID", "Patient", "Urgency", "Reason"}, 0);
                myRef.forEach(r -> m.addRow(new Object[]{r[0], dal.getPatientName(r[1]), r[7], r[8]}));
                JTable t = new JTable(m); Theme.styleTable(t);
                JScrollPane scroll = new JScrollPane(t);
                scroll.setPreferredSize(new Dimension(650, 450));
                JOptionPane.showMessageDialog(mainFrame, scroll, "Assigned Referrals", JOptionPane.PLAIN_MESSAGE);
            }
            cp.logStatus("Queue synchronized.");
        });

        // REQUIREMENT 8: REAL MEDICAL HISTORY LOOKUP
        cp.getBtnHistory().addActionListener(_ -> {
            List<Patient> pats = dal.loadPatients();
            String[] names = pats.stream().map(Patient::getFullName).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(mainFrame, "View Patient Records For:", "Records", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (sel != null) {
                int idx = java.util.Arrays.asList(names).indexOf(sel);
                String nhs = pats.get(idx).getNhsNumber();
                List<String[]> scripts = dal.getPatientPrescriptions(nhs);
                List<String[]> refs = dal.getPatientReferrals(nhs);

                StringBuilder sb = new StringBuilder("=== MEDICAL RECORD: " + sel + " ===\n\n");
                sb.append("ACTIVE PRESCRIPTIONS:\n");
                if (scripts.isEmpty()) sb.append("- No records found\n");
                else scripts.forEach(s -> sb.append("- ").append(s[5]).append(" (").append(s[6]).append(") Issued: ").append(s[4]).append("\n"));

                sb.append("\nCLINICAL REFERRALS:\n");
                if (refs.isEmpty()) sb.append("- No records found\n");
                else refs.forEach(r -> sb.append("- ").append(r[8]).append(" [").append(r[7]).append("] Status: ").append(r[11]).append("\n"));

                // FIXED: EXPLICITLY USING java.io.File TO PREVENT TYPE CONFLICT
                java.io.File ehrFile = new java.io.File("data/EHR_" + nhs + ".txt");
                if (ehrFile.exists()) {
                    sb.append("\nElectronic Health Record log found and synchronized.\n");
                }

                JOptionPane.showMessageDialog(mainFrame, new JScrollPane(new JTextArea(sb.toString(), 15, 40)), "View Records", JOptionPane.INFORMATION_MESSAGE);
                cp.logStatus("Accessed records for " + sel);
            }
        });

        return cp;
    }

    /**
     * SHARED REFERRAL WORKFLOW (Used by Admin and GP)
     * Generates Referral Letter + Persists CSV + Updates EHR.
     */
    private void openReferralFlow(Patient p, String type) {
        ClinicalDialog cd = new ClinicalDialog(mainFrame, p, type);
        cd.getBtnSubmit().addActionListener(_ -> {
            String id = "REF" + (System.currentTimeMillis() % 1000);
            Referral r = new Referral(id, "2026-01-13", cd.getUrgency(), "NEW");
            ReferralService.getInstance().createReferral(r, cd.getSummary(), p.getFullName());
            dal.saveNewReferral(r, p.getNhsNumber(), loggedInUserId, cd.getSummary());

            // UPDATE EHR LOG
            ReferralService.getInstance().updateElectronicHealthRecord(p.getNhsNumber(), r, loggedInUserName);

            cd.dispose();
            JOptionPane.showMessageDialog(mainFrame, "Referral and EHR updated for " + p.getFirstName());
        });
        cd.setVisible(true);
    }

    private JPanel createBaseTablePanel(String title, JTable table, Runnable addAction, String btnText, Consumer<String> delLogic, int idIdx, Runnable refreshAction) {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false);
        Theme.styleTable(table);
        p.add(new JLabel(title), BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel s = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); s.setOpaque(false);
        if (addAction != null) {
            JButton b = new JButton(btnText); Theme.stylePrimaryButton(b, Theme.SUCCESS);
            b.addActionListener(_ -> addAction.run()); s.add(b);
        }
        if (delLogic != null) {
            JButton d = new JButton("Remove Record"); Theme.stylePrimaryButton(d, Theme.DANGER);
            d.addActionListener(_ -> {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int mR = table.convertRowIndexToModel(row);
                    delLogic.accept(table.getModel().getValueAt(mR, idIdx).toString());
                    refreshAction.run();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Select a record to remove.");
                }
            });
            s.add(d);
        }
        p.add(s, BorderLayout.SOUTH); return p;
    }
}