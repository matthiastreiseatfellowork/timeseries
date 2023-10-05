package com.matthiast.timeseries;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeSeriesAdder {

    public TimeSeries addTimeSeries(TimeSeries timeSeriesA, TimeSeries timeSeriesB) {
        var combinedStart = calculateCombinedStart(timeSeriesA, timeSeriesB);
        var combinedEnd = calculateCombinedEnd(timeSeriesA, timeSeriesB);
        var result = new TimeSeries(combinedStart, combinedEnd);

        var mergedSlices = new ArrayList<TimeSlice>();
        mergeTimeSlices(timeSeriesA, timeSeriesB, mergedSlices);
        mergedSlices.sort(Comparator.comparing(TimeSlice::startTime));
        joinConsecutiveTimeSlices(mergedSlices, result);

        return result;
    }

    private static void mergeTimeSlices(TimeSeries timeSeriesA, TimeSeries timeSeriesB, ArrayList<TimeSlice> mergedSlices) {
        for (TimeSlice thisSlice : timeSeriesA.getTimeSlices()) {
            boolean overlap = false;
            for (TimeSlice otherSlice : timeSeriesB.getTimeSlices()) {
                if (slicesOverlap(thisSlice, otherSlice)) {
                    overlap = true;
                    LocalDateTime start = getCombinedIntervallStart(thisSlice, otherSlice);
                    LocalDateTime end = getCombinedIntervallEnd(thisSlice, otherSlice);
                    double mergedValue = thisSlice.value() + otherSlice.value();
                    mergedSlices.add(new TimeSlice(start, end, mergedValue));
                }
            }
            if (!overlap) {
                mergedSlices.add(thisSlice);
            }
        }

        for (TimeSlice otherSlice : timeSeriesB.getTimeSlices()) {
            boolean overlap = false;
            for (TimeSlice thisSlice : timeSeriesA.getTimeSlices()) {
                if (slicesOverlap(otherSlice, thisSlice)) {
                    overlap = true;
                    if (otherSlice.startTime().isBefore(thisSlice.startTime())) {
                        mergedSlices.add(new TimeSlice(otherSlice.startTime(), thisSlice.startTime(), otherSlice.value()));
                    }
                    if (otherSlice.endTime().isAfter(thisSlice.endTime())) {
                        mergedSlices.add(new TimeSlice(thisSlice.endTime(), otherSlice.endTime(), otherSlice.value()));
                    }
                }
            }
            if (!overlap) {
                mergedSlices.add(otherSlice);
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

    private static LocalDateTime getCombinedIntervallEnd(TimeSlice thisSlice, TimeSlice otherSlice) {
        return thisSlice.endTime().isBefore(otherSlice.endTime()) ? thisSlice.endTime() : otherSlice.endTime();
    }

    private static LocalDateTime getCombinedIntervallStart(TimeSlice thisSlice, TimeSlice otherSlice) {
        return thisSlice.startTime().isAfter(otherSlice.startTime()) ? thisSlice.startTime() : otherSlice.startTime();
    }

    private static LocalDateTime calculateCombinedEnd(TimeSeries timeSeriesA, TimeSeries timeSeriesB) {
        return timeSeriesA.getEndDate().isAfter(timeSeriesB.getEndDate()) ? timeSeriesA.getEndDate() : timeSeriesB.getEndDate();
    }

    private static LocalDateTime calculateCombinedStart(TimeSeries timeSeriesA, TimeSeries timeSeriesB) {
        return timeSeriesA.getStartDate().isBefore(timeSeriesB.getStartDate()) ? timeSeriesA.getStartDate() : timeSeriesB.getStartDate();
    }

    private static boolean slicesOverlap(TimeSlice thisSlice, TimeSlice otherSlice) {
        return thisSlice.startTime().isBefore(otherSlice.endTime()) &&
                thisSlice.endTime().isAfter(otherSlice.startTime());
    }
}
