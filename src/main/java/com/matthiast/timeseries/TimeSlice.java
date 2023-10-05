package com.matthiast.timeseries;

import java.time.LocalDateTime;

public record TimeSlice(LocalDateTime startTime, LocalDateTime endTime, double value) {

    public boolean overlapsWith(TimeSlice timeSlice) {
        return timeSlice.startTime().isBefore(this.endTime()) && timeSlice.endTime().isAfter(this.startTime());
    }
}
