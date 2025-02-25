package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.entities.Ticket;
import ticket.booking.util.UserServiceUtil;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {
    private User user;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService() throws IOException {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        loadUserListFromFile();
        System.out.println("User list loaded successfully.");
    }

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();

        if (foundUser.isPresent()) {
            this.user = foundUser.get(); // Update the user object with the actual user data
            System.out.println("Logged in user: " + user.getName());
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBookings() {
        if (user == null) {
            System.out.println("No user logged in. Please log in first.");
            return;
        }
        if (user.getTicketsBooked().isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("Your bookings:");
        for (Ticket ticket : user.getTicketsBooked()) {
            System.out.println("Ticket ID: " + ticket.getTicketId() + " | " + ticket.getTicketInfo());
        }
    }

    public Boolean cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketId));
        if (removed) {
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    // Book the seat
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);

                    // Create a new ticket
                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(), // Generate a unique ticket ID
                            user.getUserId(), // User ID
                            train.getStationTimes().keySet().iterator().next(), // Source station (first station)
                            train.getStationTimes().keySet().toArray(new String[0])[train.getStationTimes().size() - 1], // Destination station (last station)
                            "2023-10-15", // Example date of travel (you can modify this to accept user input)
                            train // Train object
                    );

                    // Add the ticket to the user's bookings
                    user.getTicketsBooked().add(ticket);

                    // Save the updated user list to the file
                    saveUserListToFile();

                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    public Boolean deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            System.out.println("User ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        // Find the user in the userList
        Optional<User> userToDelete = userList.stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst();

        if (userToDelete.isPresent()) {
            // Remove the user from the list
            userList.remove(userToDelete.get());
            try {
                // Save the updated user list to the file
                saveUserListToFile();
                System.out.println("User with ID " + userId + " has been deleted.");
                return Boolean.TRUE;
            } catch (IOException ex) {
                System.out.println("Failed to save user list after deletion: " + ex.getMessage());
                return Boolean.FALSE;
            }
        } else {
            System.out.println("No user found with ID " + userId);
            return Boolean.FALSE;
        }
    }
}