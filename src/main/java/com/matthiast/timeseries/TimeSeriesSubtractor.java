package com.matthiast.timeseries;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeSeriesSubtractor {

    public TimeSeries subtractTimeSeries(TimeSeries timeSeries, TimeSeries subtrahend) {
        var combinedStart = calculateCombinedStart(timeSeries, subtrahend);
        var combinedEnd = calculateCombinedEnd(timeSeries, subtrahend);
        var result = new TimeSeries(combinedStart, combinedEnd);

        var mergedSlices = new ArrayList<TimeSlice>();

        mergeTimeSlices(timeSeries, subtrahend, result);

        result.getTimeSlices().sort(Comparator.comparing(TimeSlice::startTime));

        joinConsecutiveTimeSlices(mergedSlices, result);

        return result;
    }

    private static LocalDateTime calculateCombinedEnd(TimeSeries timeSeries, TimeSeries subtrahend) {
        return timeSeries.getEndDate().isAfter(subtrahend.getEndDate()) ? timeSeries.getEndDate() : subtrahend.getEndDate();
    }

    private static LocalDateTime calculateCombinedStart(TimeSeries timeSeries, TimeSeries subtrahend) {
        return timeSeries.getStartDate().isBefore(subtrahend.getStartDate()) ? timeSeries.getStartDate() : subtrahend.getStartDate();
    }

    private static void mergeTimeSlices(TimeSeries timeSeries, TimeSeries subtrahend, TimeSeries result) {
        for (TimeSlice thisSlice : timeSeries.getTimeSlices()) {
            boolean overlap = false;
            for (TimeSlice otherSlice : subtrahend.getTimeSlices()) {
                if (thisSlice.startTime().isBefore(otherSlice.endTime()) &&
                        thisSlice.endTime().isAfter(otherSlice.startTime())) {
                    overlap = true;
                    LocalDateTime start = thisSlice.startTime().isAfter(otherSlice.startTime()) ? thisSlice.startTime() : otherSlice.startTime();
                    LocalDateTime end = thisSlice.endTime().isBefore(otherSlice.endTime()) ? thisSlice.endTime() : otherSlice.endTime();
                    double mergedValue = thisSlice.value() - otherSlice.value();
                    result.addTimeSlice(new TimeSlice(start, end, mergedValue));
                }
            }
            if (!overlap) {
                result.addTimeSlice(new TimeSlice(thisSlice.startTime(), thisSlice.endTime(), thisSlice.value()));
            }
        }

        for (TimeSlice otherSlice : subtrahend.getTimeSlices()) {
            boolean overlap = false;
            for (TimeSlice thisSlice : timeSeries.getTimeSlices()) {
                if (otherSlice.startTime().isBefore(thisSlice.endTime()) &&
                        otherSlice.endTime().isAfter(thisSlice.startTime())) {
                    overlap = true;
                    if (otherSlice.startTime().isBefore(thisSlice.startTime())) {
                        result.addTimeSlice(new TimeSlice(otherSlice.startTime(), thisSlice.startTime(), otherSlice.value()));
                    }
                    if (otherSlice.endTime().isAfter(thisSlice.endTime())) {
                        result.addTimeSlice(new TimeSlice(thisSlice.endTime(), otherSlice.endTime(), -otherSlice.value()));
                    }
                }
            }
            if (!overlap) {
                result.addTimeSlice(new TimeSlice(otherSlice.startTime(), otherSlice.endTime(), -otherSlice.value()));
            }
        }
    }

    private static void joinConsecutiveTimeSlices(List<TimeSlice> timeSlices, TimeSeries result) {
        if (!timeSlices.isEmpty()) {
            TimeSlice currentSlice = timeSlices.get(0);
            for (int i = 1; i < timeSlices.size(); i++) {
                TimeSlice nextSlice = timeSlices.get(i);
                if (currentSlice.endTime().equals(nextSlice.startTime()) && currentSlice.value() == nextSlice.value()) {
                    currentSlice = new TimeSlice(currentSlice.startTime(), nextSlice.endTime(), currentSlice.value());
                } else {
                    result.addTimeSlice(currentSlice);
                    currentSlice = nextSlice;
                }
            }
            // Füge die letzte Zeitscheibe hinzu
            result.addTimeSlice(currentSlice);
        }
    }
}
