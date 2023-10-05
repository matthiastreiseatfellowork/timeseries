package com.matthiast.timeseries;

import java.time.LocalDateTime;
import java.util.*;

public class TimeSeries {

    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final List<TimeSlice> timeSlices;

    public TimeSeries(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeSlices = new ArrayList<>();
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public List<TimeSlice> getTimeSlices() {
        return timeSlices;
    }

    public void addTimeSlice(TimeSlice timeSliceToAdd) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Startdatum muss vor Enddatum liegen");
        }
        if (!isValidTimeFrame(timeSliceToAdd.startTime(), timeSliceToAdd.endTime())) {
            throw new IllegalArgumentException("Die Zeitscheibe liegt außerhalb des gültigen Zeitraums.");
        }

        for (TimeSlice timeSlice: timeSlices) {
            if (timeSlice.overlapsWith(timeSliceToAdd)) {
                throw new IllegalArgumentException("Die Zeitscheiben dürfen sich nicht überlappen");
            }
        }
        timeSlices.add(timeSliceToAdd);
    }

    public void removeTimeSlice(LocalDateTime startTime, LocalDateTime endTime) {
        timeSlices.removeIf(slice -> slice.startTime().isBefore(endTime) && slice.endTime().isAfter(startTime));
    }

    public double getValueAtTime(LocalDateTime time) {
        for (TimeSlice slice : timeSlices) {
            if (time.isAfter(slice.startTime()) && time.isBefore(slice.endTime())) {
                return slice.value();
            }
        }
        return Double.NaN;
    }

    public double getMinValue(LocalDateTime startTime, LocalDateTime endTime) {
        double minValue = Double.POSITIVE_INFINITY;

        for (TimeSlice slice : timeSlices) {
            if (slice.startTime().isBefore(endTime) && slice.endTime().isAfter(startTime)) {
                double value = slice.value();
                if (value < minValue) {
                    minValue = value;
                }
            }
        }

        if (minValue == Double.POSITIVE_INFINITY) {
            return Double.NaN;
        }

        return minValue;
    }

    public double getMaxValue(LocalDateTime startTime, LocalDateTime endTime) {
        double maxValue = Double.NEGATIVE_INFINITY;

        for (TimeSlice slice : timeSlices) {
            if (slice.startTime().isBefore(endTime) && slice.endTime().isAfter(startTime)) {
                double value = slice.value();
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }

        if (maxValue == Double.NEGATIVE_INFINITY) {
            return Double.NaN;
        }

        return maxValue;
    }

    public double getAverageValue(LocalDateTime startTime, LocalDateTime endTime) {
        double sum = 0.0;
        int count = 0;

        for (TimeSlice slice : timeSlices) {
            if (slice.startTime().isBefore(endTime) && slice.endTime().isAfter(startTime)) {
                sum += slice.value();
                count++;
            }
        }

        if (count == 0) {
            return Double.NaN;
        }

        return sum / count;
    }

    private boolean isValidTimeFrame(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.isEqual(startDate) || startTime.isAfter(startDate) && endTime.isEqual(endDate) || endTime.isBefore(endDate);
    }
}
