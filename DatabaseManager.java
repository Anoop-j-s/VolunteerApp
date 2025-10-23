import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DatabaseManager (Backend)
 * This class handles all SQLite database operations.
 * It connects to the DB, creates tables, and performs all
 * CRUD (Create, Read, Update, Delete) actions.
 * It contains NO Swing code.
 */
public class DatabaseManager {

    // Connection string for SQLite. This will create a file named 'volunteer_hub.db'
    // in the same directory where the app is run.
    private static final String DATABASE_URL = "jdbc:sqlite:volunteer_hub.db";

    /**
     * Establishes a connection to the SQLite database.
     * @return a Connection object
     */
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Creates all necessary tables if they don't already exist.
     */
    public void createTables() {
        String sqlVolunteers = """
            CREATE TABLE IF NOT EXISTS volunteers (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                skills TEXT
            );
            """;
        String sqlEvents = """
            CREATE TABLE IF NOT EXISTS events (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                location TEXT NOT NULL
            );
            """;
        String sqlSignups = """
            CREATE TABLE IF NOT EXISTS event_signups (
                event_id TEXT NOT NULL,
                volunteer_id TEXT NOT NULL,
                PRIMARY KEY (event_id, volunteer_id),
                FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
                FOREIGN KEY (volunteer_id) REFERENCES volunteers (id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVolunteers);
            stmt.execute(sqlEvents);
            stmt.execute(sqlSignups);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // --- Volunteer Data Classes (used to pass data to frontend) ---
    public static class Volunteer {
        public String id, name, email, skills;
        public Volunteer(String id, String name, String email, String skills) {
            this.id = id; this.name = name; this.email = email; this.skills = skills;
        }
    }
    
    public static class Event {
        public String id, title, description, date, location;
        public Event(String id, String title, String description, String date, String location) {
            this.id = id; this.title = title; this.description = description; 
            this.date = date; this.location = location;
        }
    }

    // --- Volunteer Methods ---

    public String registerVolunteer(String name, String email, String skills) {
        String newId = "v-" + UUID.randomUUID().toString().substring(0, 8);
        String sql = "INSERT INTO volunteers(id, name, email, skills) VALUES(?,?,?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newId);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, skills);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            System.err.println("Error registering volunteer: " + e.getMessage());
            return null; // Email might be duplicate
        }
    }

    public boolean updateVolunteer(String id, String name, String email, String skills) {
        String sql = "UPDATE volunteers SET name = ?, email = ?, skills = ? WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, skills);
            pstmt.setString(4, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating volunteer: " + e.getMessage());
            return false;
        }
    }

    public Volunteer getVolunteer(String id) {
        String sql = "SELECT * FROM volunteers WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Volunteer(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("skills")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting volunteer: " + e.getMessage());
        }
        return null;
    }

    // --- Event Methods ---

    public boolean createEvent(String title, String description, String date, String location) {
        String newId = "e-" + UUID.randomUUID().toString().substring(0, 8);
        String sql = "INSERT INTO events(id, title, description, event_date, location) VALUES(?,?,?,?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, date);
            pstmt.setString(5, location);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating event: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEvent(String id, String title, String description, String date, String location) {
        String sql = "UPDATE events SET title = ?, description = ?, event_date = ?, location = ? WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, date);
            pstmt.setString(4, location);
            pstmt.setString(5, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEvent(String id) {
        // ON DELETE CASCADE will also remove signups
        String sql = "DELETE FROM events WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return false;
        }
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                events.add(new Event(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("event_date"),
                    rs.getString("location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all events: " + e.getMessage());
        }
        return events;
    }
    
    public Event getEvent(String id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Event(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("event_date"),
                    rs.getString("location")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting event: " + e.getMessage());
        }
        return null;
    }

    // --- Signup Methods ---

    public boolean signUpForEvent(String volunteerId, String eventId) {
        String sql = "INSERT INTO event_signups(volunteer_id, event_id) VALUES(?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, volunteerId);
            pstmt.setString(2, eventId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error signing up for event: " + e.getMessage());
            return false; // Already signed up?
        }
    }
    
    public List<String> getEventsForVolunteer(String volunteerId) {
        List<String> eventIds = new ArrayList<>();
        String sql = "SELECT event_id FROM event_signups WHERE volunteer_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, volunteerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                eventIds.add(rs.getString("event_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting volunteer's events: " + e.getMessage());
        }
        return eventIds;
    }
}
