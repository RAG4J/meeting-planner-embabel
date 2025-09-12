package org.rag4j.meetingplanner.location;

import org.rag4j.meetingplanner.location.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class McpDocumentationController {

    private final LocationService locationService;

    public McpDocumentationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Get all locations to display in the documentation
        LocationResponse locationResponse = locationService.getAllLocations();
        List<Location> locations = locationResponse.locations();
        List<LocationBookingStats> locationBookingStats = locationService.getLocationBookingStats();
        BookingStats bookingStats = new BookingStats(locationBookingStats);
        
        model.addAttribute("locations", locations);
        model.addAttribute("bookingStats", bookingStats);
        model.addAttribute("serverName", "Location MCP Server");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "This server provides meeting location suggestions based on user preferences and can book rooms at the location.");
        
        return "index";
    }

    @GetMapping("/docs")
    public String documentation(Model model) {
        return "redirect:/";
    }
    
    @GetMapping("/bookings")
    public String bookings(Model model) {
        List<BookingInfo> allBookings = locationService.getAllBookings();
        List<LocationBookingStats> locationBookingStats = locationService.getLocationBookingStats();
        BookingStats bookingStats = new BookingStats(locationBookingStats);
        
        model.addAttribute("bookings", allBookings);
        model.addAttribute("bookingStats", bookingStats);
        model.addAttribute("totalBookings", allBookings.size());
        
        return "bookings";
    }
    
    @GetMapping("/location/{locationId}/bookings")
    public String locationBookings(@PathVariable String locationId, Model model) {
        List<BookingInfo> bookings = locationService.getBookingsForLocation(locationId);
        LocationResponse locationResponse = locationService.getAllLocations();
        Location location = locationResponse.locations().stream()
                .filter(loc -> loc.id().equals(locationId))
                .findFirst()
                .orElse(null);
        
        if (location == null) {
            return "redirect:/bookings";
        }
        
        model.addAttribute("location", location);
        model.addAttribute("bookings", bookings);
        model.addAttribute("rooms", locationService.getRoomsForLocation(locationId));
        
        return "location-bookings";
    }
    
    @GetMapping("/location/{locationId}/week")
    public String locationWeeklyCalendar(@PathVariable String locationId, 
                                        @RequestParam(required = false) String date,
                                        Model model) {
        LocalDate weekStart;
        if (date != null) {
            LocalDate requestedDate = LocalDate.parse(date);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekStart = requestedDate.with(weekFields.dayOfWeek(), 1);
        } else {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekStart = LocalDate.now().with(weekFields.dayOfWeek(), 1);
        }
        
        LocationResponse locationResponse = locationService.getAllLocations();
        Location location = locationResponse.locations().stream()
                .filter(loc -> loc.id().equals(locationId))
                .findFirst()
                .orElse(null);
        
        if (location == null) {
            return "redirect:/bookings";
        }
        
        List<BookingInfo> weekBookings = locationService.getBookingsForWeek(locationId, weekStart);
        
        // Pre-process bookings by day for the template
        Map<LocalDate, List<BookingInfo>> bookingsByDay = weekBookings.stream()
                .collect(Collectors.groupingBy(BookingInfo::date));
        
        model.addAttribute("location", location);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekStart.plusDays(6));
        model.addAttribute("bookings", weekBookings);
        model.addAttribute("bookingsByDay", bookingsByDay);
        model.addAttribute("rooms", locationService.getRoomsForLocation(locationId));
        model.addAttribute("totalHoursBooked", weekBookings.stream()
                .mapToInt(b -> (int) Duration.between(b.startTime(), b.endTime()).toHours())
                .sum());

        return "weekly-calendar";
    }
    
    @GetMapping("/calendar")
    public String monthlyCalendar(@RequestParam(required = false) String month, Model model) {
        YearMonth currentMonth;
        if (month != null) {
            currentMonth = YearMonth.parse(month);
        } else {
            currentMonth = YearMonth.now();
        }
        
        List<BookingInfo> monthBookings = locationService.getBookingsForMonth(currentMonth);
        LocationResponse locationResponse = locationService.getAllLocations();
        
        // Pre-process bookings by day for the template
        Map<LocalDate, List<BookingInfo>> bookingsByDay = monthBookings.stream()
                .collect(Collectors.groupingBy(BookingInfo::date));

        int totalHours = monthBookings.stream()
                .mapToInt(b -> (int) Duration.between(b.startTime(), b.endTime()).toHours())
                .sum();
        
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("bookings", monthBookings);
        model.addAttribute("bookingsByDay", bookingsByDay);
        model.addAttribute("locations", locationResponse.locations());
        model.addAttribute("monthName", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("totalHours", totalHours);
        
        return "monthly-calendar";
    }
}
