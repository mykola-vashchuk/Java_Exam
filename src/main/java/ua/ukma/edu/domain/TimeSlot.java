package ua.ukma.edu.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public record TimeSlot(LocalDateTime start, LocalDateTime end) {

    public TimeSlot {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Time slot boundaries must not be null.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Time slot start must be before end.");
        }
    }

    public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            return false;
        }
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean isInFuture() {
        return start.isAfter(LocalDateTime.now());
    }

    public TimeSlot shift(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration must not be null.");
        }
        return new TimeSlot(start.plus(duration), end.plus(duration));
    }
}
