package org.rag4j.meetingplanner.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();
    }

    @Test
    @DisplayName("Returns all locations when getAllLocations is called")
    void returnsAllLocationsWhenGetAllLocationsIsCalled() {
        LocationResponse response = locationService.getAllLocations();

        assertNotNull(response);
        assertNotNull(response.locations());
        assertFalse(response.locations().isEmpty());

        // Verify some expected locations are present
        List<String> locationIds = response.locations().stream()
                .map(Location::id)
                .toList();

        assertTrue(locationIds.contains("luminis"));
        assertTrue(locationIds.contains("techhub"));
        assertTrue(locationIds.contains("cityview"));
        assertTrue(locationIds.contains("greenspace"));
    }

    @Test
    @DisplayName("Returns expected number of predefined locations")
    void returnsExpectedNumberOfPredefinedLocations() {
        LocationResponse response = locationService.getAllLocations();

        // Based on the initialization, we expect 10 locations
        assertEquals(11, response.locations().size());
    }

    @Test
    @DisplayName("All locations have valid properties")
    void allLocationsHaveValidProperties() {
        LocationResponse response = locationService.getAllLocations();

        for (Location location : response.locations()) {
            assertNotNull(location.id());
            assertNotNull(location.name());
            assertNotNull(location.description());
            assertFalse(location.id().trim().isEmpty());
            assertFalse(location.name().trim().isEmpty());
            assertFalse(location.description().trim().isEmpty());
        }
    }

    @Test
    @DisplayName("Finds available room when capacity and time requirements are met")
    void findsAvailableRoomWhenCapacityAndTimeRequirementsAreMet() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(10, 0);
        int duration = 60;

        RoomAvailableRequest request = new RoomAvailableRequest(
                "luminis", 4, date, startTime, duration
        );

        RoomAvailableResponse response = locationService.checkRoomAvailability(request);

        assertNotNull(response);
        assertEquals("luminis", response.locationId());
        assertTrue(response.available());
        assertNotNull(response.roomId());
        assertEquals(startTime, response.startTime());
        assertEquals(duration, response.durationInMinutes());
    }

    @Test
    @DisplayName("Returns smallest suitable room when multiple rooms match capacity")
    void returnsSmallestSuitableRoomWhenMultpleRoomsMatchCapacity() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(14, 0);
        int duration = 30;

        // Request for 2 people - should get the smallest room that fits (room-a with capacity 4)
        RoomAvailableRequest request = new RoomAvailableRequest(
                "luminis", 2, date, startTime, duration
        );

        RoomAvailableResponse response = locationService.checkRoomAvailability(request);

        assertTrue(response.available());
        assertEquals("room-a", response.roomId()); // Should get the smallest room
    }

    @Test
    @DisplayName("Returns no room when capacity exceeds all available rooms")
    void returnsNoRoomWhenCapacityExceedsAllAvailableRooms() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(16, 0);
        int duration = 45;

        // Request for 50 people - exceeds all room capacities in any location
        RoomAvailableRequest request = new RoomAvailableRequest(
                "luminis", 50, date, startTime, duration
        );

        RoomAvailableResponse response = locationService.checkRoomAvailability(request);

        assertNotNull(response);
        assertEquals("luminis", response.locationId());
        assertFalse(response.available());
        assertNull(response.roomId());
        assertNull(response.startTime());
        assertEquals(0, response.durationInMinutes());
    }

    @Test
    @DisplayName("Returns no room when location does not exist")
    void returnsNoRoomWhenLocationDoesNotExist() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(11, 0);
        int duration = 60;

        RoomAvailableRequest request = new RoomAvailableRequest(
                "nonexistent", 5, date, startTime, duration
        );

        // This should cause a NullPointerException or similar when trying to access rooms
        assertThrows(Exception.class, () -> {
            locationService.checkRoomAvailability(request);
        });
    }

    @Test
    @DisplayName("Handles edge case with zero capacity request")
    void handlesEdgeCaseWithZeroCapacityRequest() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(9, 0);
        int duration = 30;

        RoomAvailableRequest request = new RoomAvailableRequest(
                "techhub", 0, date, startTime, duration
        );

        RoomAvailableResponse response = locationService.checkRoomAvailability(request);

        // Should find a room since all rooms have capacity >= 0
        assertTrue(response.available());
        assertNotNull(response.roomId());
    }

    @Test
    @DisplayName("Handles request for exact room capacity")
    void handlesRequestForExactRoomCapacity() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        LocalTime startTime = LocalTime.of(13, 0);
        int duration = 90;

        // Request for exactly 4 people (should match room-a in luminis)
        RoomAvailableRequest request = new RoomAvailableRequest(
                "luminis", 4, date, startTime, duration
        );

        RoomAvailableResponse response = locationService.checkRoomAvailability(request);

        assertTrue(response.available());
        assertEquals("room-a", response.roomId());
    }

    @Test
    @DisplayName("Works with different location types")
    void worksWithDifferentLocationTypes() {
        LocalDate date = LocalDate.of(2025, 9, 2);
        LocalTime startTime = LocalTime.of(15, 30);
        int duration = 120;

        // Test multiple different locations
        String[] testLocations = {"like-home", "meet-nature", "harbor", "library"};

        for (String locationId : testLocations) {
            RoomAvailableRequest request = new RoomAvailableRequest(
                    locationId, 5, date, startTime, duration
            );

            RoomAvailableResponse response = locationService.checkRoomAvailability(request);

            assertNotNull(response);
            assertEquals(locationId, response.locationId());
            // Should find available room for 5 people in each location
            assertTrue(response.available(), "Should find available room in " + locationId);
        }
    }

    @Test
    @DisplayName("Verifies specific location details are correct")
    void verifiesSpecificLocationDetailsAreCorrect() {
        LocationResponse response = locationService.getAllLocations();

        // Find specific locations and verify their details
        Location luminis = response.locations().stream()
                .filter(loc -> "luminis".equals(loc.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(luminis);
        assertEquals("Luminis", luminis.name());
        assertTrue(luminis.description().contains("Business meeting rooms"));

        Location techHub = response.locations().stream()
                .filter(loc -> "techhub".equals(loc.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(techHub);
        assertEquals("TechHub", techHub.name());
        assertTrue(techHub.description().contains("Modern tech-focused"));
    }

    @Test
    @DisplayName("Successfully books a room when available")
    void successfullyBooksRoomWhenAvailable() {
        LocalDate date = LocalDate.of(2025, 10, 1);
        LocalTime startTime = LocalTime.of(10, 0);
        int duration = 60;
        String reference = "REF-123";
        String description = "Team meeting";

        BookRoomRequest request = new BookRoomRequest(
                "luminis", "room-a", date, startTime, duration, reference, description
        );

        BookRoomResponse response = locationService.bookRoom(request);

        assertNotNull(response);
        assertEquals("luminis", response.locationId());
        assertEquals("room-a", response.roomId());
        assertTrue(response.success());
        assertTrue(response.message().contains("Booking confirmed"));
    }

    @Test
    @DisplayName("Fails to book a room if location does not exist")
    void failsToBookRoomIfLocationDoesNotExist() {
        LocalDate date = LocalDate.of(2025, 10, 1);
        LocalTime startTime = LocalTime.of(11, 0);
        int duration = 30;

        BookRoomRequest request = new BookRoomRequest(
                "nonexistent", "room-x", date, startTime, duration, "REF-404", "No such location"
        );

        BookRoomResponse response = locationService.bookRoom(request);

        assertNotNull(response);
        assertEquals("nonexistent", response.locationId());
        assertEquals("room-x", response.roomId());
        assertFalse(response.success());
        assertTrue(response.message().contains("unknown location"));
    }

    @Test
    @DisplayName("Fails to book a room if room does not exist in location")
    void failsToBookRoomIfRoomDoesNotExistInLocation() {
        LocalDate date = LocalDate.of(2025, 10, 1);
        LocalTime startTime = LocalTime.of(12, 0);
        int duration = 45;

        BookRoomRequest request = new BookRoomRequest(
                "luminis", "room-x", date, startTime, duration, "REF-404", "No such room"
        );

        BookRoomResponse response = locationService.bookRoom(request);

        assertNotNull(response);
        assertEquals("luminis", response.locationId());
        assertEquals("room-x", response.roomId());
        assertFalse(response.success());
        assertTrue(response.message().contains("unknown room"));
    }

    @Test
    @DisplayName("Fails to book a room if time slot is already booked")
    void failsToBookRoomIfTimeSlotAlreadyBooked() {
        LocalDate date = LocalDate.of(2025, 10, 2);
        LocalTime startTime = LocalTime.of(9, 0);
        int duration = 60;
        String reference1 = "REF-1";
        String reference2 = "REF-2";

        // First booking should succeed
        BookRoomRequest firstRequest = new BookRoomRequest(
                "luminis", "room-b", date, startTime, duration, reference1, "Morning meeting"
        );
        BookRoomResponse firstResponse = locationService.bookRoom(firstRequest);
        assertTrue(firstResponse.success());

        // Second booking for the same slot should fail
        BookRoomRequest secondRequest = new BookRoomRequest(
                "luminis", "room-b", date, startTime, duration, reference2, "Conflict meeting"
        );
        BookRoomResponse secondResponse = locationService.bookRoom(secondRequest);

        assertNotNull(secondResponse);
        assertEquals("luminis", secondResponse.locationId());
        assertEquals("room-b", secondResponse.roomId());
        assertFalse(secondResponse.success());
        assertTrue(secondResponse.message().contains("No capacity"));
    }

    @Test
    @DisplayName("Allows booking different rooms at the same time in the same location")
    void allowsBookingDifferentRoomsAtSameTimeInSameLocation() {
        LocalDate date = LocalDate.of(2025, 10, 3);
        LocalTime startTime = LocalTime.of(15, 0);
        int duration = 30;

        BookRoomRequest requestA = new BookRoomRequest(
                "luminis", "room-a", date, startTime, duration, "REF-A", "Meeting A"
        );
        BookRoomRequest requestB = new BookRoomRequest(
                "luminis", "room-b", date, startTime, duration, "REF-B", "Meeting B"
        );

        BookRoomResponse responseA = locationService.bookRoom(requestA);
        BookRoomResponse responseB = locationService.bookRoom(requestB);

        assertTrue(responseA.success());
        assertTrue(responseB.success());
    }

    @Test
    @DisplayName("Allows booking same room for non-overlapping times")
    void allowsBookingSameRoomForNonOverlappingTimes() {
        LocalDate date = LocalDate.of(2025, 10, 4);
        LocalTime startTime1 = LocalTime.of(10, 0);
        LocalTime startTime2 = LocalTime.of(11, 0);
        int duration = 60;

        BookRoomRequest request1 = new BookRoomRequest(
                "luminis", "room-c", date, startTime1, duration, "REF-1", "First meeting"
        );
        BookRoomRequest request2 = new BookRoomRequest(
                "luminis", "room-c", date, startTime2, duration, "REF-2", "Second meeting"
        );

        BookRoomResponse response1 = locationService.bookRoom(request1);
        BookRoomResponse response2 = locationService.bookRoom(request2);

        assertTrue(response1.success());
        assertTrue(response2.success());
    }
}
