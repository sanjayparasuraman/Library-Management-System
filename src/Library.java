import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Library {
    private static final int FINE_PER_DAY = 2;
    private static final int LOAN_DAYS = 14;

    private void printError(SQLException e) {
        System.out.println("Database error: " + e.getMessage());
    }

    private void printBookRow(ResultSet rs) throws SQLException {
        System.out.println(rs.getInt("id") + " | " + rs.getString("title") + " | "
                + rs.getString("author") + " | "
                + (rs.getBoolean("available") ? "Available" : "Issued"));
    }

    public void addBook(String title, String author) {
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();
            System.out.println("Book added successfully.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void viewBooks() {
        String sql = "SELECT id, title, author, available FROM books";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                printBookRow(rs);
            }
            if (!any) System.out.println("No books in the library.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void searchBook(String keyword) {
        String sql = "SELECT id, title, author, available FROM books WHERE title LIKE ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printBookRow(rs);
                }
                if (!found) System.out.println("No matching book found.");
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void removeBook(int id) {
        try (Connection con = DBConnection.getConnection()) {
            boolean exists = false;
            boolean available = false;
            try (PreparedStatement ps = con.prepareStatement("SELECT available FROM books WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        available = rs.getBoolean("available");
                    }
                }
            }
            if (!exists) {
                System.out.println("Book ID not found.");
                return;
            }
            if (!available) {
                System.out.println("Cannot remove. This book is currently issued.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Book removed.");
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Cannot remove. This book has issue history.");
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void addMember(String name, String phone) {
        String sql = "INSERT INTO members (name, phone) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.executeUpdate();
            System.out.println("Member registered successfully.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void viewMembers() {
        String sql = "SELECT id, name, phone FROM members";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println(rs.getInt("id") + " | " + rs.getString("name")
                        + " | " + rs.getString("phone"));
            }
            if (!any) System.out.println("No members registered.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void issueBook(int bookId, int memberId) {
        try (Connection con = DBConnection.getConnection()) {
            Boolean available = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT available FROM books WHERE id = ?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) available = rs.getBoolean("available");
                }
            }
            String memberName = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT name FROM members WHERE id = ?")) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) memberName = rs.getString("name");
                }
            }

            if (available == null) {
                System.out.println("Book ID not found.");
                return;
            }
            if (memberName == null) {
                System.out.println("Member ID not found.");
                return;
            }
            if (!available) {
                System.out.println("Sorry, this book is already issued.");
                return;
            }

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(LOAN_DAYS);

            con.setAutoCommit(false);
            try (PreparedStatement ins = con.prepareStatement(
                         "INSERT INTO issues (book_id, member_id, issue_date, due_date) VALUES (?, ?, ?, ?)");
                 PreparedStatement upd = con.prepareStatement(
                         "UPDATE books SET available = FALSE WHERE id = ?")) {
                ins.setInt(1, bookId);
                ins.setInt(2, memberId);
                ins.setDate(3, java.sql.Date.valueOf(issueDate));
                ins.setDate(4, java.sql.Date.valueOf(dueDate));
                ins.executeUpdate();

                upd.setInt(1, bookId);
                upd.executeUpdate();

                con.commit();
                System.out.println("Book issued to " + memberName);
                System.out.println("Due date: " + dueDate);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void returnBook(int bookId) {
        try (Connection con = DBConnection.getConnection()) {
            int issueId = -1;
            LocalDate dueDate = null;
            String sql = "SELECT id, due_date FROM issues WHERE book_id = ? AND return_date IS NULL";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        issueId = rs.getInt("id");
                        dueDate = rs.getDate("due_date").toLocalDate();
                    }
                }
            }

            if (issueId == -1) {
                System.out.println("This book is not currently issued.");
                return;
            }

            LocalDate today = LocalDate.now();
            long lateDays = ChronoUnit.DAYS.between(dueDate, today);

            con.setAutoCommit(false);
            try (PreparedStatement upIssue = con.prepareStatement(
                         "UPDATE issues SET return_date = ? WHERE id = ?");
                 PreparedStatement upBook = con.prepareStatement(
                         "UPDATE books SET available = TRUE WHERE id = ?")) {
                upIssue.setDate(1, java.sql.Date.valueOf(today));
                upIssue.setInt(2, issueId);
                upIssue.executeUpdate();

                upBook.setInt(1, bookId);
                upBook.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

            if (lateDays > 0) {
                System.out.println("Book returned late by " + lateDays + " day(s).");
                System.out.println("Fine: Rs. " + (lateDays * FINE_PER_DAY));
            } else {
                System.out.println("Book returned on time. No fine.");
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void viewIssuedBooks() {
        String sql = "SELECT b.title, m.name, i.issue_date, i.due_date "
                + "FROM issues i "
                + "JOIN books b ON i.book_id = b.id "
                + "JOIN members m ON i.member_id = m.id "
                + "WHERE i.return_date IS NULL";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                LocalDate due = rs.getDate("due_date").toLocalDate();
                String status = LocalDate.now().isAfter(due) ? " (OVERDUE)" : "";
                System.out.println(rs.getString("title") + " -> " + rs.getString("name")
                        + " | Issued: " + rs.getDate("issue_date")
                        + " | Due: " + due + status);
            }
            if (!any) System.out.println("No books are currently issued.");
        } catch (SQLException e) {
            printError(e);
        }
    }
}