import java.util.Scanner;

public class Main {

    static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Library library = new Library();

        while (true) {
            System.out.println("\n===== LIBRARY MENU =====");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Book");
            System.out.println("4. Remove Book");
            System.out.println("5. Register Member");
            System.out.println("6. View Members");
            System.out.println("7. Issue Book");
            System.out.println("8. Return Book");
            System.out.println("9. View Issued Books");
            System.out.println("0. Exit");

            int choice = readInt(sc, "Enter choice: ");

            switch (choice) {
                case 1:
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author: ");
                    String author = sc.nextLine();
                    library.addBook(title, author);
                    break;
                case 2:
                    library.viewBooks();
                    break;
                case 3:
                    System.out.print("Enter title keyword: ");
                    library.searchBook(sc.nextLine());
                    break;
                case 4:
                    library.removeBook(readInt(sc, "Enter book ID to remove: "));
                    break;
                case 5:
                    System.out.print("Name: ");
                    String name = sc.nextLine();
                    System.out.print("Phone: ");
                    String phone = sc.nextLine();
                    library.addMember(name, phone);
                    break;
                case 6:
                    library.viewMembers();
                    break;
                case 7:
                    int bookId = readInt(sc, "Book ID: ");
                    int memberId = readInt(sc, "Member ID: ");
                    library.issueBook(bookId, memberId);
                    break;
                case 8:
                    library.returnBook(readInt(sc, "Book ID to return: "));
                    break;
                case 9:
                    library.viewIssuedBooks();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
