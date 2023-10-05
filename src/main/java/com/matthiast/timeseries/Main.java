package com.matthiast.timeseries;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        var timeSeriesAdder = new TimeSeriesAdder();
        var timeSeriesPrinter = new TimeSeriesPrinter();
        TimeSeries timeSeries1 = new TimeSeries(LocalDateTime.of(2023, 1, 3, 0, 0), LocalDateTime.of(2023, 1, 8, 0, 0));
        timeSeries1.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 3, 0, 0), LocalDateTime.of(2023, 1, 5, 0, 0), 2.0));
        timeSeries1.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 6, 0, 0), LocalDateTime.of(2023, 1, 7, 0, 0), 3.0));

        TimeSeries timeSeries2 = new TimeSeries(LocalDateTime.of(2023, 1, 4, 0, 0), LocalDateTime.of(2023, 1, 10, 0, 0));
        timeSeries2.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 5, 0, 0), LocalDateTime.of(2023, 1, 6, 0, 0), 2.0));
        timeSeries2.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 6, 0, 0), LocalDateTime.of(2023, 1, 8, 0, 0), 1.0));
        timeSeries2.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 9, 0, 0), LocalDateTime.of(2023, 1, 10, 0, 0), 1.0));

        TimeSeries result = timeSeriesAdder.addTimeSeries(timeSeries1, timeSeries2);
        timeSeriesPrinter.printTimeSeries(timeSeries1);
        timeSeriesPrinter.printTimeSeries(timeSeries2);
        timeSeriesPrinter.printTimeSeries(result);
    }
}