package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner sc = new Scanner(System.in);
        int option = 0;

        UserBookingService userBookingService;
        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            return;
        }
        Train trainSelectedForBooking = null;

        while (option != 8) { // Updated exit option to 9
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Delete My Account"); // New option
            System.out.println("8. Exit the App");

            try {
                option = sc.nextInt();
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); // Clear the invalid input
                continue;
            }

            switch (option) {
                case 1:
                    System.out.println("Enter the username to signup");
                    String nameToSignUp = sc.next();
                    System.out.println("Enter the password to signup");
                    String passwordToSignUp = sc.next();
                    User userToSignup = new User(nameToSignUp, passwordToSignUp, UserServiceUtil.hashPassword(passwordToSignUp), new ArrayList<>(), UUID.randomUUID().toString());
                    userBookingService.signUp(userToSignup);
                    break;
                case 2:
                    System.out.println("Enter the username to Login");
                    String nameToLogin = sc.next();
                    System.out.println("Enter the password to login");
                    String passwordToLogin = sc.next();
                    User userToLogin = new User(nameToLogin, passwordToLogin, UserServiceUtil.hashPassword(passwordToLogin), new ArrayList<>(), UUID.randomUUID().toString());
                    try {
                        userBookingService = new UserBookingService(userToLogin);
                        if (userBookingService.loginUser()) {
                            System.out.println("Login successful!");
                        } else {
                            System.out.println("Invalid username or password.");
                        }
                    } catch (IOException ex) {
                        System.out.println("There is something wrong: " + ex.getMessage());
                        ex.printStackTrace();
                        return;
                    }
                    break;
                case 3:
                    System.out.println("Fetching your bookings");
                    userBookingService.fetchBookings();
                    break;
                case 4:
                    System.out.println("Type your source station");
                    String source = sc.next();
                    System.out.println("Type your destination station");
                    String dest = sc.next();
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    if (trains.isEmpty()) {
                        System.out.println("No trains found between " + source + " and " + dest);
                        break;
                    }
                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + " Train id : " + t.getTrainId());
                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("station " + entry.getKey() + " time: " + entry.getValue());
                        }
                    }
                    System.out.println("Select a train by typing 1,2,3...");
                    int trainIndex = sc.nextInt() - 1;
                    if (trainIndex >= 0 && trainIndex < trains.size()) {
                        trainSelectedForBooking = trains.get(trainIndex);
                    } else {
                        System.out.println("Invalid selection.");
                    }
                    break;
                case 5:
                    if (trainSelectedForBooking == null) {
                        System.out.println("No train selected. Please search and select a train first.");
                        break;
                    }
                    System.out.println("Select a seat out of these seats");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    for (List<Integer> row : seats) {
                        for (Integer val : row) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                    }
                    System.out.println("Select the seat by typing the row and column");
                    System.out.println("Enter the row");
                    int row = sc.nextInt();
                    System.out.println("Enter the column");
                    int col = sc.nextInt();
                    System.out.println("Booking your seat....");
                    Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                    if (booked.equals(Boolean.TRUE)) {
                        System.out.println("Booked! Enjoy your journey");
                    } else {
                        System.out.println("Can't book this seat");
                    }
                    break;
                case 6:
                    System.out.println("Fetching your bookings to display ticket IDs...");
                    userBookingService.fetchBookings(); // Display bookings with ticket IDs
                    System.out.println("Enter the ticket ID to cancel:");
                    String ticketIdToCancel = sc.next();
                    boolean isCancelled = userBookingService.cancelBooking(ticketIdToCancel);
                    if (isCancelled) {
                        System.out.println("Ticket with ID " + ticketIdToCancel + " has been successfully canceled.");
                    } else {
                        System.out.println("Failed to cancel the ticket. Please check the ticket ID and try again.");
                    }
                    break;
                case 7:
                    System.out.println("Enter your user ID to delete your account:");
                    String userIdToDelete = sc.next();
                    boolean isDeleted = userBookingService.deleteUser(userIdToDelete);
                    if (isDeleted) {
                        System.out.println("Your account has been successfully deleted.");
                    } else {
                        System.out.println("Failed to delete your account. Please check your user ID and try again.");
                    }
                    break;
                case 8:
                    System.out.println("Exiting the app. Thank you!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose a valid option.");
                    break;
            }
        }
        sc.close();
    }

    public String getGreeting() {
        return "Hello, welcome to the Train Booking System!";
    }
}