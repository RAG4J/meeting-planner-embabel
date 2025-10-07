package org.rag4j.meetingplanner.location;

import org.rag4j.meetingplanner.common.model.Agenda;
import org.rag4j.meetingplanner.location.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final Map<String, Location> locations = new HashMap<>();
    private final Map<String, Map<String, Room>> locationRooms = new HashMap<>();

    public LocationService() {
        initializeLocations();
        addSampleBookings();
    }

    @Tool(
            name = "all-locations",
            description = "Get all available meeting locations."
    )
    public LocationResponse getAllLocations() {
        logger.info("Fetching all available locations.");

        return new LocationResponse(new ArrayList<>(locations.values()));
    }

    @Tool(
            name = "check-room-availability",
            description = "Check room availability for a specific location, date, time, duration and number of people."
    )
    public RoomAvailableResponse checkRoomAvailability(RoomAvailableRequest request) {
        logger.info("Checking room availability {}", request);

        Map<String, Room> availableRooms = locationRooms.get(request.locationId());
        // Check if one of the rooms has capacity equals to or higher than the requested capacity for the given date
        // and time
        // Return the room with the lowest capacity that matches the request
        Optional<Room> bestAvailableRoom = availableRooms.values().stream()
                .filter(room -> room.capacity() >= request.requestedNumberOfPeople())
                .filter(room -> room.agenda().checkAvailability(request.date(), request.startTime(),
                        request.startTime().plusMinutes(request.durationInMinutes())))
                .min(Comparator.comparingInt(Room::capacity));

        RoomAvailableResponse response;
        if (bestAvailableRoom.isPresent()) {
            Room room = bestAvailableRoom.get();
            response = new RoomAvailableResponse(request.locationId(), true, room.roomId(), request.date(), request.startTime(),
                    request.durationInMinutes());
        } else {
            response = new RoomAvailableResponse(request.locationId(), false, null, null, null, 0);
        }

        logger.info("Response for check room availability {}", response);
        return response;
    }

    @Tool(
            name = "book-room",
            description = "Book a room at a specific location on a day and time."
    )
    public BookRoomResponse bookRoom(BookRoomRequest request) {
        logger.info("Book a room {}", request);
        Map<String, Room> stringRoomMap = locationRooms.get(request.locationId());
        if (stringRoomMap == null) {
            return new BookRoomResponse(request.locationId(), request.roomId(), false, "You requested an unknown location");
        }
        Room room = stringRoomMap.get(request.roomId());
        if (room == null) {
            return new BookRoomResponse(request.locationId(), request.roomId(), false, "You requested an unknown room");
        }

        boolean b = room.agenda().checkAvailability(request.date(), request.startTime(), request.startTime().plusMinutes(request.durationInMinutes()));
        if (!b) {
            return new BookRoomResponse(request.locationId(), request.roomId(), false, "No capacity at the requested time");
        }

        room.agenda().bookMeeting(request.date(),
                request.startTime(),
                request.startTime().plusMinutes(request.durationInMinutes()),
                String.format("Reference %s - Description %s", request.reference(), request.description()));
        BookRoomResponse bookRoomResponse = new BookRoomResponse(request.locationId(), request.roomId(), true, String.format("Booking confirmed for %s", request.reference()));
        logger.info("Booking confirmed for location {}", bookRoomResponse);
        return bookRoomResponse;
    }

    private void initializeLocations() {
        // Luminis
        locations.put("luminis", new Location("luminis", "Luminis", "Business meeting rooms across different " +
                "locations in the Netherlands."));
        Map<String, Room> roomsLuminis = new HashMap<>();
        roomsLuminis.put("room-a", new Room("luminis", "room-a", 4, new Agenda()));
        roomsLuminis.put("room-b", new Room("luminis", "room-b", 8, new Agenda()));
        roomsLuminis.put("room-c", new Room("luminis", "room-c", 12, new Agenda()));
        locationRooms.put("luminis", roomsLuminis);

        // Like Home
        locations.put("like-home", new Location("like-home", "Meeting like Home", "Homely settings for a relaxing " +
                "meeting."));
        Map<String, Room> roomsLikeHome = new HashMap<>();
        roomsLikeHome.put("living", new Room("like-home", "living", 6, new Agenda()));
        roomsLikeHome.put("kitchen", new Room("like-home", "kitchen", 10, new Agenda()));
        roomsLikeHome.put("garden", new Room("like-home", "garden", 5, new Agenda()));
        locationRooms.put("like-home", roomsLikeHome);

        // Meet Nature
        locations.put("meet-nature", new Location("meet-nature", "Meeting in Nature", "Combine meetings with outdoor " +
                "activities."));
        Map<String, Room> roomsNature = new HashMap<>();
        roomsNature.put("forest", new Room("meet-nature", "forest", 8, new Agenda()));
        roomsNature.put("lake", new Room("meet-nature", "lake", 14, new Agenda()));
        roomsNature.put("meadow", new Room("meet-nature", "meadow", 6, new Agenda()));
        locationRooms.put("meet-nature", roomsNature);

        // TechHub
        locations.put("techhub", new Location("techhub", "TechHub", "Modern tech-focused meeting spaces."));
        Map<String, Room> roomsTechHub = new HashMap<>();
        roomsTechHub.put("alpha", new Room("techhub", "alpha", 5, new Agenda()));
        roomsTechHub.put("beta", new Room("techhub", "beta", 9, new Agenda()));
        roomsTechHub.put("gamma", new Room("techhub", "gamma", 15, new Agenda()));
        locationRooms.put("techhub", roomsTechHub);

        // CityView
        locations.put("cityview", new Location("cityview", "CityView", "Panoramic city views for inspiring meetings."));
        Map<String, Room> roomsCityView = new HashMap<>();
        roomsCityView.put("sky", new Room("cityview", "sky", 7, new Agenda()));
        roomsCityView.put("cloud", new Room("cityview", "cloud", 12, new Agenda()));
        roomsCityView.put("sun", new Room("cityview", "sun", 20, new Agenda()));
        locationRooms.put("cityview", roomsCityView);

        // GreenSpace
        locations.put("greenspace", new Location("greenspace", "GreenSpace", "Eco-friendly meeting rooms surrounded " +
                "by plants."));
        Map<String, Room> roomsGreenSpace = new HashMap<>();
        roomsGreenSpace.put("ivy", new Room("greenspace", "ivy", 4, new Agenda()));
        roomsGreenSpace.put("fern", new Room("greenspace", "fern", 8, new Agenda()));
        roomsGreenSpace.put("moss", new Room("greenspace", "moss", 10, new Agenda()));
        locationRooms.put("greenspace", roomsGreenSpace);

        // Harbor
        locations.put("harbor", new Location("harbor", "Harbor", "Meetings with a view of the water and ships."));
        Map<String, Room> roomsHarbor = new HashMap<>();
        roomsHarbor.put("dock", new Room("harbor", "dock", 6, new Agenda()));
        roomsHarbor.put("pier", new Room("harbor", "pier", 11, new Agenda()));
        roomsHarbor.put("cabin", new Room("harbor", "cabin", 8, new Agenda()));
        locationRooms.put("harbor", roomsHarbor);

        // Library
        locations.put("library", new Location("library", "Library", "Quiet spaces for focused meetings."));
        Map<String, Room> roomsLibrary = new HashMap<>();
        roomsLibrary.put("study", new Room("library", "study", 3, new Agenda()));
        roomsLibrary.put("archive", new Room("library", "archive", 7, new Agenda()));
        roomsLibrary.put("reading", new Room("library", "reading", 10, new Agenda()));
        locationRooms.put("library", roomsLibrary);

        // Loft
        locations.put("loft", new Location("loft", "Loft", "Trendy loft-style meeting rooms."));
        Map<String, Room> roomsLoft = new HashMap<>();
        roomsLoft.put("brick", new Room("loft", "brick", 5, new Agenda()));
        roomsLoft.put("beam", new Room("loft", "beam", 9, new Agenda()));
        roomsLoft.put("glass", new Room("loft", "glass", 13, new Agenda()));
        locationRooms.put("loft", roomsLoft);

        // Villa
        locations.put("villa", new Location("villa", "Villa", "Luxurious villa for exclusive meetings."));
        Map<String, Room> roomsVilla = new HashMap<>();
        roomsVilla.put("salon", new Room("villa", "salon", 8, new Agenda()));
        roomsVilla.put("terrace", new Room("villa", "terrace", 16, new Agenda()));
        roomsVilla.put("suite", new Room("villa", "suite", 6, new Agenda()));
        locationRooms.put("villa", roomsVilla);

        // Campus
        locations.put("campus", new Location("campus", "Campus", "Academic-style meeting rooms for workshops and " +
                "seminars."));
        Map<String, Room> roomsCampus = new HashMap<>();
        roomsCampus.put("lab", new Room("campus", "lab", 10, new Agenda()));
        roomsCampus.put("hall", new Room("campus", "hall", 18, new Agenda()));
        roomsCampus.put("class", new Room("campus", "class", 7, new Agenda()));
        locationRooms.put("campus", roomsCampus);
    }
    
    private void addSampleBookings() {
        // Add some sample bookings for demonstration
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        LocalDate nextWeek = today.plusDays(7);
        
        // Get Monday of current week for better weekly view
        LocalDate currentMonday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate currentTuesday = currentMonday.plusDays(1);
        LocalDate currentWednesday = currentMonday.plusDays(2);
        LocalDate currentThursday = currentMonday.plusDays(3);
        LocalDate currentFriday = currentMonday.plusDays(4);
        
        // Current week bookings for better weekly calendar view
        
        // Monday bookings
        locationRooms.get("luminis").get("room-a").agenda()
                .bookMeeting(currentMonday, LocalTime.of(9, 0), LocalTime.of(10, 30), "Monday Team Stand-up");
        locationRooms.get("techhub").get("alpha").agenda()
                .bookMeeting(currentMonday, LocalTime.of(14, 0), LocalTime.of(15, 30), "Code Review Monday");
                
        // Tuesday bookings  
        locationRooms.get("luminis").get("room-b").agenda()
                .bookMeeting(currentTuesday, LocalTime.of(10, 0), LocalTime.of(11, 30), "Sprint Planning");
        locationRooms.get("like-home").get("living").agenda()
                .bookMeeting(currentTuesday, LocalTime.of(15, 0), LocalTime.of(16, 0), "Casual Coffee Meeting");
                
        // Wednesday bookings
        locationRooms.get("cityview").get("sky").agenda()
                .bookMeeting(currentWednesday, LocalTime.of(9, 30), LocalTime.of(11, 0), "Midweek Strategy");
        locationRooms.get("greenspace").get("ivy").agenda()
                .bookMeeting(currentWednesday, LocalTime.of(13, 0), LocalTime.of(14, 0), "Green Meeting");
                
        // Thursday bookings
        locationRooms.get("techhub").get("beta").agenda()
                .bookMeeting(currentThursday, LocalTime.of(11, 0), LocalTime.of(12, 30), "Architecture Discussion");
        locationRooms.get("harbor").get("dock").agenda()
                .bookMeeting(currentThursday, LocalTime.of(14, 30), LocalTime.of(16, 0), "Waterfront Meeting");
                
        // Friday bookings
        locationRooms.get("luminis").get("room-c").agenda()
                .bookMeeting(currentFriday, LocalTime.of(10, 0), LocalTime.of(11, 30), "Friday Client Presentation");
        locationRooms.get("villa").get("salon").agenda()
                .bookMeeting(currentFriday, LocalTime.of(15, 0), LocalTime.of(17, 0), "End of Week VIP Meeting");
                
        // Some bookings for today/yesterday/tomorrow for variety
        locationRooms.get("cityview").get("cloud").agenda()
                .bookMeeting(today, LocalTime.of(16, 0), LocalTime.of(17, 0), "Today's Executive Briefing");
        locationRooms.get("library").get("study").agenda()
                .bookMeeting(yesterday, LocalTime.of(10, 0), LocalTime.of(11, 0), "Yesterday's Focus Session");
        locationRooms.get("loft").get("brick").agenda()
                .bookMeeting(tomorrow, LocalTime.of(14, 0), LocalTime.of(15, 30), "Tomorrow's Brainstorm");
                
        // Next week bookings
        locationRooms.get("campus").get("lab").agenda()
                .bookMeeting(nextWeek, LocalTime.of(9, 30), LocalTime.of(11, 30), "Next Week Research Workshop");
        locationRooms.get("like-home").get("kitchen").agenda()
                .bookMeeting(nextWeek.plusDays(1), LocalTime.of(13, 0), LocalTime.of(15, 0), "Team Lunch & Learn");
        locationRooms.get("techhub").get("gamma").agenda()
                .bookMeeting(nextWeek.plusDays(2), LocalTime.of(10, 0), LocalTime.of(12, 0), "All Hands Meeting");
    }

    // Non-tool methods for web UI functionality
    
    /**
     * Get all bookings across all locations
     */
    public List<BookingInfo> getAllBookings() {
        List<BookingInfo> allBookings = new ArrayList<>();
        
        for (Map.Entry<String, Location> locationEntry : locations.entrySet()) {
            String locationId = locationEntry.getKey();
            Location location = locationEntry.getValue();
            Map<String, Room> rooms = locationRooms.get(locationId);
            
            for (Room room : rooms.values()) {
                List<Agenda.AgendaItem> meetings = room.agenda().getMeetings();
                for (Agenda.AgendaItem meeting : meetings) {
                    allBookings.add(new BookingInfo(
                            locationId,
                            location.name(),
                            room.roomId(),
                            meeting.day(),
                            meeting.start(),
                            meeting.end(),
                            meeting.title()
                    ));
                }
            }
        }
        
        return allBookings.stream()
                .sorted((b1, b2) -> {
                    int dateComparison = b1.date().compareTo(b2.date());
                    if (dateComparison != 0) return dateComparison;
                    return b1.startTime().compareTo(b2.startTime());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get bookings for a specific location
     */
    public List<BookingInfo> getBookingsForLocation(String locationId) {
        return getAllBookings().stream()
                .filter(booking -> booking.locationId().equals(locationId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get booking statistics for all locations
     */
    public List<LocationBookingStats> getLocationBookingStats() {
        List<LocationBookingStats> stats = new ArrayList<>();
        
        for (Map.Entry<String, Location> locationEntry : locations.entrySet()) {
            String locationId = locationEntry.getKey();
            Location location = locationEntry.getValue();
            Map<String, Room> rooms = locationRooms.get(locationId);
            
            int totalBookings = rooms.values().stream()
                    .mapToInt(room -> room.agenda().getMeetings().size())
                    .sum();
            
            stats.add(new LocationBookingStats(
                    locationId,
                    location.name(),
                    location.description(),
                    totalBookings,
                    rooms.size()
            ));
        }
        
        return stats;
    }
    
    /**
     * Get bookings for a specific week (Monday to Sunday)
     */
    public List<BookingInfo> getBookingsForWeek(String locationId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getBookingsForLocation(locationId).stream()
                .filter(booking -> !booking.date().isBefore(weekStart) && !booking.date().isAfter(weekEnd))
                .collect(Collectors.toList());
    }
    
    /**
     * Get bookings for a specific month
     */
    public List<BookingInfo> getBookingsForMonth(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        return getAllBookings().stream()
                .filter(booking -> !booking.date().isBefore(monthStart) && !booking.date().isAfter(monthEnd))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all rooms for a location
     */
    public Map<String, Room> getRoomsForLocation(String locationId) {
        return locationRooms.getOrDefault(locationId, new HashMap<>());
    }
}
