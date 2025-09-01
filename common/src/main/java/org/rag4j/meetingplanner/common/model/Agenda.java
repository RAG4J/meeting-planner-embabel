package org.rag4j.meetingplanner.common.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Agenda {
    private final List<AgendaItem> items = new ArrayList<>();

    /**
     * Checks if the given time slot is available (no overlapping meetings)
     * @param day the day to check
     * @param start the start time
     * @param end the end time
     * @return True if the time slot is available, false otherwise
     */
    public boolean checkAvailability(LocalDate day, LocalTime start, LocalTime end) {
        for (AgendaItem item : items) {
            if (item.day().equals(day)) {
                if (start.isBefore(item.end()) && end.isAfter(item.start())) {
                    return false; // Overlap found
                }
            }
        }
        return true; // No overlap
    }

    public void bookMeeting(LocalDate day, LocalTime start, LocalTime end, String title) {
        this.items.add(new AgendaItem(day, start, end, title));
    }

    /**
     * Returns the time slots that are still available for the given day. Start day at 09:00 and end at 17:00.
     * A time slot is represented as "HH:mm-HH:mm"
     * @param day the day to check
     * @return list of available time slots
     */
    public List<String> availabilityForDay(LocalDate day) {
        List<String> availableSlots = new ArrayList<>();
        LocalTime startOfDay = LocalTime.of(9, 0);
        LocalTime endOfDay = LocalTime.of(17, 0);

        LocalTime currentStart = startOfDay;

        // Collect and sort items for the day
        List<AgendaItem> dayItems = items.stream()
                .filter(item -> item.day().equals(day))
                .sorted(Comparator.comparing(AgendaItem::start))
                .toList();

        // Walk through the day's items, clamped to working hours
        for (AgendaItem item : dayItems) {
            // Clamp meeting times to within working hours
            LocalTime itemStart = item.start().isBefore(startOfDay) ? startOfDay : item.start();
            LocalTime itemEnd = item.end().isAfter(endOfDay) ? endOfDay : item.end();

            // Skip items completely outside working hours
            if (itemEnd.isBefore(startOfDay) || itemStart.isAfter(endOfDay)) {
                continue;
            }

            if (currentStart.isBefore(itemStart)) {
                availableSlots.add(currentStart + "-" + itemStart);
            }
            if (itemEnd.isAfter(currentStart)) {
                currentStart = itemEnd;
            }
        }

        if (currentStart.isBefore(endOfDay)) {
            availableSlots.add(currentStart + "-" + endOfDay);
        }

        return availableSlots;
    }

    public List<AgendaItem> getMeetings() {
        return new ArrayList<>(items);
    }

    public record AgendaItem(LocalDate day, LocalTime start, LocalTime end, String title) {
        // validate if end is after start
        public AgendaItem {
            if (end.isBefore(start) || end.equals(start)) {
                throw new IllegalArgumentException("End time must be after start time");
            }
        }
    }
}
