import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

/**
 * VolunteerApp (Frontend)
 * This class builds the main Java Swing GUI. It manages the CardLayout
 * to switch between different panels (Events, Profile, Manage).
 * It communicates with the DatabaseManager (backend) for all data operations.
 */
public class VolunteerApp extends JFrame {

    // --- Modern White Theme Colors ---
    private static final Color COLOR_BACKGROUND = new Color(245, 245, 250);
    private static final Color COLOR_NAV_BAR = new Color(255, 255, 255);
    private static final Color COLOR_PANEL = new Color(255, 255, 255);
    private static final Color COLOR_TEXT = new Color(30, 30, 40);
    private static final Color COLOR_TEXT_LIGHT = new Color(100, 100, 110);
    private static final Color COLOR_PRIMARY = new Color(0, 123, 255);
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_BORDER = new Color(220, 220, 225);

    // --- Fonts ---
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_NAV = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // --- GUI Components ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private DatabaseManager dbManager; // The backend connection

    // --- "State" ---
    // This stores the ID of the currently "logged in" volunteer.
    // A real app would have a full login system.
    private String currentVolunteerId = null;
    private JLabel statusLabel;

    // --- Panels (Screens) ---
    private EventsPanel eventsPanel;
    private ProfilePanel profilePanel;
    private ManageEventsPanel manageEventsPanel;

    public VolunteerApp() {
        // --- 1. Connect to Backend ---
        dbManager = new DatabaseManager();
        dbManager.createTables();

        // --- 2. Set up Main Window ---
        setTitle("Community Volunteer Hub");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // --- 3. Create Navigation Panel (West) ---
        JPanel navigationPanel = createNavigationPanel();
        add(navigationPanel, BorderLayout.WEST);

        // --- 4. Create Main Content Panel (Center) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(COLOR_BACKGROUND);
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 5. Create and Add Panels (Screens) ---
        eventsPanel = new EventsPanel(this);
        profilePanel = new ProfilePanel(this);
        manageEventsPanel = new ManageEventsPanel(this);

        mainContentPanel.add(eventsPanel, "EVENTS");
        mainContentPanel.add(profilePanel, "PROFILE");
        mainContentPanel.add(manageEventsPanel, "MANAGE");

        add(mainContentPanel, BorderLayout.CENTER);

        // --- 6. Create Status Bar (South) ---
        statusLabel = new JLabel("Welcome! Please register or log in via the 'My Profile' tab.");
        statusLabel.setFont(FONT_BODY);
        statusLabel.setForeground(COLOR_TEXT_LIGHT);
        statusLabel.setBorder(new EmptyBorder(5, 15, 5, 15));
        add(statusLabel, BorderLayout.SOUTH);

        // --- 7. Finalize ---
        cardLayout.show(mainContentPanel, "EVENTS"); // Show events first
        setVisible(true);
    }

    /**
     * Creates the left-side navigation bar with buttons.
     */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(COLOR_NAV_BAR);
        navPanel.setPreferredSize(new Dimension(200, 0));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        JLabel navTitle = new JLabel("Volunteer Hub");
        navTitle.setFont(FONT_HEADER);
        navTitle.setForeground(COLOR_TEXT);
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navTitle.setBorder(new EmptyBorder(20, 10, 20, 10));
        navPanel.add(navTitle);

        // Add buttons
        navPanel.add(createNavButton("Browse Events", "EVENTS"));
        navPanel.add(createNavButton("My Profile", "PROFILE"));
        navPanel.add(createNavButton("Manage Events", "MANAGE"));

        navPanel.add(Box.createVerticalGlue()); // Pushes buttons to the top
        return navPanel;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(FONT_NAV);
        button.setForeground(COLOR_TEXT_LIGHT);
        button.setBackground(COLOR_NAV_BAR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(15, 25, 15, 25));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        button.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));
        return button;
    }

    // --- Helper methods for panels ---
    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public String getCurrentVolunteerId() {
        return currentVolunteerId;
    }

    public void setCurrentVolunteer(String id, String name) {
        this.currentVolunteerId = id;
        if (id != null) {
            statusLabel.setText("Logged in as: " + name + " (ID: " + id + ")");
            statusLabel.setForeground(COLOR_SUCCESS);
        } else {
            statusLabel.setText("Not logged in. Please register or log in via 'My Profile'.");
            statusLabel.setForeground(COLOR_TEXT_LIGHT);
        }
        // Refresh events panel to show what user is signed up for
        eventsPanel.loadEvents();
    }

    // Utility to style buttons
    public static void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Utility to style tables
    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(COLOR_PANEL);
        table.setForeground(COLOR_TEXT);
        table.setGridColor(COLOR_BORDER);
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setSelectionBackground(COLOR_PRIMARY);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_BACKGROUND);
        header.setForeground(COLOR_TEXT);
        header.setFont(FONT_BODY_BOLD);
        header.setBorder(new LineBorder(COLOR_BORDER));

        scrollPane.getViewport().setBackground(COLOR_PANEL);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER));
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        // Set global UI defaults for the white theme
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, default will be used.
        }

        UIManager.put("Panel.background", COLOR_BACKGROUND);
        UIManager.put("Label.foreground", COLOR_TEXT);
        UIManager.put("TextField.background", COLOR_PANEL);
        UIManager.put("TextField.foreground", COLOR_TEXT);

        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(VolunteerApp::new);
    }
}

// =========================================================================
// --- Panel 1: Events Browser ---
// =========================================================================
class EventsPanel extends JPanel {
    private VolunteerApp app;
    private DatabaseManager dbManager;
    private JTable eventsTable;
    private DefaultTableModel tableModel;

    public EventsPanel(VolunteerApp app) {
        this.app = app;
        this.dbManager = app.getDbManager();
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Upcoming Community Events");
        title.setFont(VolunteerApp.FONT_HEADER);
        add(title, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Date", "Location", "Signed Up?"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        eventsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        VolunteerApp.styleTable(eventsTable, scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton signUpButton = new JButton("Sign Up for Selected Event");
        VolunteerApp.styleButton(signUpButton, VolunteerApp.COLOR_SUCCESS);
        signUpButton.addActionListener(e -> onSignUp());
        
        JButton refreshButton = new JButton("Refresh");
        VolunteerApp.styleButton(refreshButton, VolunteerApp.COLOR_PRIMARY);
        refreshButton.addActionListener(e -> loadEvents());

        buttonPanel.add(refreshButton);
        buttonPanel.add(signUpButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadEvents();
    }

    public void loadEvents() {
        tableModel.setRowCount(0);
        List<DatabaseManager.Event> events = dbManager.getAllEvents();
        String currentVolId = app.getCurrentVolunteerId();
        
        List<String> signedUpEventIds = (currentVolId != null) 
            ? dbManager.getEventsForVolunteer(currentVolId) 
            : List.of();

        for (DatabaseManager.Event event : events) {
            Vector<Object> row = new Vector<>();
            row.add(event.id);
            row.add(event.title);
            row.add(event.date);
            row.add(event.location);
            row.add(signedUpEventIds.contains(event.id) ? "Yes" : "No");
            tableModel.addRow(row);
        }
    }

    private void onSignUp() {
        int selectedRow = eventsTable.getSelectedRow();
        if (app.getCurrentVolunteerId() == null) {
            JOptionPane.showMessageDialog(this, "Please log in from the 'My Profile' tab to sign up.", "Not Logged In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to sign up for.", "No Event Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String eventId = (String) tableModel.getValueAt(selectedRow, 0);
        String eventTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String isSignedUp = (String) tableModel.getValueAt(selectedRow, 4);

        if ("Yes".equals(isSignedUp)) {
            JOptionPane.showMessageDialog(this, "You are already signed up for this event.", "Already Registered", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Sign up for '" + eventTitle + "'?", "Confirm Signup", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.signUpForEvent(app.getCurrentVolunteerId(), eventId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Successfully signed up!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEvents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to sign up. You may already be registered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// =========================================================================
// --- Panel 2: Volunteer Profile ---
// =========================================================================
class ProfilePanel extends JPanel {
    private VolunteerApp app;
    private DatabaseManager dbManager;
    private JTextField idField = new JTextField(10);
    private JTextField nameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JTextArea skillsArea = new JTextArea(5, 20);

    public ProfilePanel(VolunteerApp app) {
        this.app = app;
        this.dbManager = app.getDbManager();
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(VolunteerApp.COLOR_BORDER)
        ));

        // --- Login/Load Panel ---
        JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loadPanel.setBackground(Color.WHITE);
        loadPanel.add(new JLabel("Your Volunteer ID:"));
        loadPanel.add(idField);
        JButton loadButton = new JButton("Load Profile");
        VolunteerApp.styleButton(loadButton, VolunteerApp.COLOR_PRIMARY);
        loadButton.addActionListener(e -> loadProfile());
        loadPanel.add(loadButton);
        add(loadPanel, BorderLayout.NORTH);

        // --- Profile Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Skills:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        skillsArea.setLineWrap(true);
        skillsArea.setWrapStyleWord(true);
        skillsArea.setFont(VolunteerApp.FONT_BODY);
        skillsArea.setBorder(new LineBorder(VolunteerApp.COLOR_BORDER));
        formPanel.add(new JScrollPane(skillsArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton registerButton = new JButton("Register as New Volunteer");
        VolunteerApp.styleButton(registerButton, VolunteerApp.COLOR_PRIMARY);
        registerButton.addActionListener(e -> registerNew());

        JButton updateButton = new JButton("Save Profile Changes");
        VolunteerApp.styleButton(updateButton, VolunteerApp.COLOR_SUCCESS);
        updateButton.addActionListener(e -> updateProfile());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(updateButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadProfile() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Volunteer ID to load.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DatabaseManager.Volunteer vol = dbManager.getVolunteer(id);
        if (vol != null) {
            nameField.setText(vol.name);
            emailField.setText(vol.email);
            skillsArea.setText(vol.skills);
            app.setCurrentVolunteer(vol.id, vol.name);
            JOptionPane.showMessageDialog(this, "Profile loaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No volunteer found with ID: " + id, "Not Found", JOptionPane.ERROR_MESSAGE);
            clearForm();
            app.setCurrentVolunteer(null, null);
        }
    }

    private void registerNew() {
        String name = nameField.getText();
        String email = emailField.getText();
        String skills = skillsArea.getText();
        
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required to register.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String newId = dbManager.registerVolunteer(name, email, skills);
        if (newId != null) {
            idField.setText(newId);
            app.setCurrentVolunteer(newId, name);
            JOptionPane.showMessageDialog(this, "Registration successful! Your new Volunteer ID is " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Email may already be in use.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateProfile() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String skills = skillsArea.getText();

        if (id.isEmpty() || !id.equals(app.getCurrentVolunteerId())) {
             JOptionPane.showMessageDialog(this, "Please load your profile before saving changes.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success = dbManager.updateVolunteer(id, name, email, skills);
        if (success) {
            app.setCurrentVolunteer(id, name); // Refresh name in status bar
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Profile update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        // idField is kept
        nameField.setText("");
        emailField.setText("");
        skillsArea.setText("");
    }
}

// =========================================================================
// --- Panel 3: Manage Events (Admin) ---
// =========================================================================
class ManageEventsPanel extends JPanel {
    private VolunteerApp app;
    private DatabaseManager dbManager;
    private JTable eventsTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField idField = new JTextField(10);
    private JTextField titleField = new JTextField(20);
    private JTextField dateField = new JTextField(10);
    private JTextField locationField = new JTextField(20);
    private JTextArea descriptionArea = new JTextArea(5, 20);
    private JButton saveButton;
    private JButton clearButton;
    private JButton deleteButton;

    public ManageEventsPanel(VolunteerApp app) {
        this.app = app;
        this.dbManager = app.getDbManager();
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(Color.WHITE);
        JLabel title = new JLabel("Manage Events");
        title.setFont(VolunteerApp.FONT_HEADER);
        tablePanel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Date", "Location"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        eventsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        VolunteerApp.styleTable(eventsTable, scrollPane);
        
        // Add mouse listener to load form on row click
        eventsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadSelectedEventToForm();
            }
        });
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        idField.setEditable(false);
        dateField.setToolTipText("YYYY-MM-DD");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(dateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(locationField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(VolunteerApp.FONT_BODY);
        descriptionArea.setBorder(new LineBorder(VolunteerApp.COLOR_BORDER));
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        // --- Form Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        saveButton = new JButton("Save (Add New / Update)");
        VolunteerApp.styleButton(saveButton, VolunteerApp.COLOR_SUCCESS);
        saveButton.addActionListener(e -> onSave());
        
        deleteButton = new JButton("Delete");
        VolunteerApp.styleButton(deleteButton, new Color(220, 53, 69)); // Red
        deleteButton.addActionListener(e -> onDelete());

        clearButton = new JButton("Clear Form");
        VolunteerApp.styleButton(clearButton, VolunteerApp.COLOR_TEXT_LIGHT);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);
        
        add(formPanel, BorderLayout.EAST);
        
        loadEvents();
    }
    
    private void loadEvents() {
        tableModel.setRowCount(0);
        List<DatabaseManager.Event> events = dbManager.getAllEvents();
        for (DatabaseManager.Event event : events) {
            Vector<Object> row = new Vector<>();
            row.add(event.id);
            row.add(event.title);
            row.add(event.date);
            row.add(event.location);
            tableModel.addRow(row);
        }
        // Refresh the other panel too
        app.eventsPanel.loadEvents();
    }
    
    private void loadSelectedEventToForm() {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String eventId = (String) tableModel.getValueAt(selectedRow, 0);
        DatabaseManager.Event event = dbManager.getEvent(eventId);
        
        if (event != null) {
            idField.setText(event.id);
            titleField.setText(event.title);
            descriptionArea.setText(event.description);
            dateField.setText(event.date);
            locationField.setText(event.location);
        }
    }
    
    private void clearForm() {
        idField.setText("");
        titleField.setText("");
        descriptionArea.setText("");
        dateField.setText("");
        locationField.setText("");
        eventsTable.clearSelection();
    }
    
    private void onSave() {
        String id = idField.getText();
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String date = dateField.getText();
        String location = locationField.getText();
        
        if (title.isEmpty() || date.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Date, and Location are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success;
        if (id.isEmpty()) {
            // Add new
            success = dbManager.createEvent(title, description, date, location);
        } else {
            // Update existing
            success = dbManager.updateEvent(id, title, description, date, location);
        }
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Event saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEvents();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save event.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onDelete() {
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this event? This will also remove all signups.", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteEvent(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Event deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadEvents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete event.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
