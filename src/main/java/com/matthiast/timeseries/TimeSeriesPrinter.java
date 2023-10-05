package com.matthiast.timeseries;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class TimeSeriesPrinter {

    public void printTimeSeries(TimeSeries timeSeries) {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        var startDate = timeSeries.getStartDate();
        var endDate = timeSeries.getEndDate();
        var timeSlices = timeSeries.getTimeSlices();

        timeSlices.sort(Comparator.comparing(TimeSlice::startTime));

        System.out.println("Zeitreihe von " + startDate.format(formatter) + " bis " + endDate.format(formatter));
        for (TimeSlice slice : timeSlices) {
            System.out.println("Startdatum: " + slice.startTime().format(formatter) +
                    ", Enddatum: " + slice.endTime().format(formatter) +
                    ", Wert: " + slice.value());
        }
    }
}
