package com.matthiast.timeseries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

public class TimeSeriesTest {
    private static final LocalDateTime TIME_SERIES_START = LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0);
    private static final LocalDateTime TIME_SERIES_END = LocalDateTime.of(2023, Month.JANUARY, 11, 0, 0);
    private static final LocalDateTime VALID_START1 = LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0);
    private static final LocalDateTime TIME_WITHIN_TIME_SLICE = LocalDateTime.of(2023, Month.JANUARY, 2, 0, 0);
    private static final LocalDateTime VALID_END1 = LocalDateTime.of(2023, Month.JANUARY, 3, 0, 0);
    private static final double TEST_VALUE1 = 5.0;
    private static final LocalDateTime VALID_START2 = LocalDateTime.of(2023, Month.JANUARY, 3, 0, 0);
    private static final LocalDateTime VALID_END2 = LocalDateTime.of(2023, Month.JANUARY, 5, 0, 0);
    private static final double TEST_VALUE2 = 3.0;
    private static final LocalDateTime INVALID_START = LocalDateTime.of(2023, Month.JANUARY, 12, 0, 0);
    private static final LocalDateTime INVALID_END = LocalDateTime.of(2023, Month.JANUARY, 13, 0, 0);
    private static final LocalDateTime OVERLAPPING_START = LocalDateTime.of(2023, Month.JANUARY, 2, 0, 0);
    private static final LocalDateTime OVERLAPPING_END = LocalDateTime.of(2023, Month.JANUARY, 3, 0, 0);

    private TimeSeries timeSeries;
    private TimeSeriesPrinter timeSeriesPrinter;
    private TimeSeriesAdder timeSeriesAdder;
    private TimeSeriesSubtractor timeSeriesSubtractor;

    @BeforeEach
    public void setUp() {
        timeSeries = new TimeSeries(TIME_SERIES_START, TIME_SERIES_END);
        timeSeriesPrinter = new TimeSeriesPrinter();
        timeSeriesAdder = new TimeSeriesAdder();
        timeSeriesSubtractor = new TimeSeriesSubtractor();
    }

    @Test
    public void testValidTimeSlice() {
        assertDoesNotThrow(() -> timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1)));
    }

    @Test()
    public void testInvalidTimeSlice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> timeSeries.addTimeSlice(new TimeSlice(INVALID_START, INVALID_END, 7)));

        String expectedMessage = "Die Zeitscheibe liegt außerhalb des gültigen Zeitraums.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test()
    public void testOverLappingTimeSlices() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));
            timeSeries.addTimeSlice(new TimeSlice(OVERLAPPING_START, OVERLAPPING_END, TEST_VALUE1));
        });

        String expectedMessage = "Die Zeitscheiben dürfen sich nicht überlappen";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testRemoveTimeSlice() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));
        timeSeries.addTimeSlice(new TimeSlice(VALID_START2, VALID_END2, TEST_VALUE2));

        timeSeries.removeTimeSlice(VALID_START2, VALID_END2);

        assertTrue(Double.isNaN(timeSeries.getValueAtTime(VALID_START2)));
        assertTrue(Double.isNaN(timeSeries.getValueAtTime(VALID_END2)));
    }

    @Test
    public void testGetValueAtTimeWithinTimeSlice() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));

        double value = timeSeries.getValueAtTime(TIME_WITHIN_TIME_SLICE);

        assertEquals(TEST_VALUE1, value, 0.0);
    }

    @Test
    public void testGetValueAtTimeOutsideTimeSlice() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));

        double value = timeSeries.getValueAtTime(INVALID_END);

        assertTrue(Double.isNaN(value));
    }

    @Test
    public void testGetMinValueWithValidTimeRange() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));

        timeSeries.addTimeSlice(new TimeSlice(VALID_START2, VALID_END2, TEST_VALUE2));

        double minValue = timeSeries.getMinValue(VALID_START1, VALID_END2);

        assertEquals(TEST_VALUE2, minValue, 0.0);
    }

    @Test
    public void testGetMinValueWithNoValidTimeRange() {
        double minValue = timeSeries.getMinValue(INVALID_START, INVALID_END);

        assertTrue(Double.isNaN(minValue));
    }

    @Test
    public void testGetMaxValueWithValidTimeRange() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));
        timeSeries.addTimeSlice(new TimeSlice(VALID_START2, VALID_END2, TEST_VALUE2));

        double maxValue = timeSeries.getMaxValue(VALID_START1, VALID_END2);

        assertEquals(TEST_VALUE1, maxValue, 0.0);
    }

    @Test
    public void testGetMaxValueWithNoValidTimeRange() {
        double maxValue = timeSeries.getMaxValue(INVALID_START, INVALID_END);

        assertTrue(Double.isNaN(maxValue));
    }

    @Test
    public void testGetAverageValueWithValidTimeRange() {
        timeSeries.addTimeSlice(new TimeSlice(VALID_START1, VALID_END1, TEST_VALUE1));
        timeSeries.addTimeSlice(new TimeSlice(VALID_START2, VALID_END2, TEST_VALUE2));

        double avgValue = timeSeries.getAverageValue(VALID_START1, VALID_END2);

        DoubleStream stream = DoubleStream.of(TEST_VALUE1, TEST_VALUE2);

        assertEquals(stream.average().getAsDouble(), avgValue, 0.0);
    }

    @Test
    public void testGetAverageValueWithNoValidTimeRange() {
        double avgValue = timeSeries.getAverageValue(INVALID_START, INVALID_END);

        assertTrue(Double.isNaN(avgValue));
    }

    @Test
    public void testPrintTimeSeries() {
        var timeSeries = setupTimeSeriesA();

        var printStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printStream));
        timeSeriesPrinter.printTimeSeries(timeSeries);
        var output = printStream.toString();

        var expectedOutput = """
        Zeitreihe von 03.01.2023 00:00 bis 08.01.2023 00:00
        Startdatum: 03.01.2023 00:00, Enddatum: 05.01.2023 00:00, Wert: 2.0
        Startdatum: 06.01.2023 00:00, Enddatum: 07.01.2023 00:00, Wert: 3.0
        """.replace("\n", System.lineSeparator());

        assertEquals(expectedOutput, output);
    }

    @Test
    void testAddTimeSeries() {
        TimeSeries timeSeriesA = setupTimeSeriesA();
        TimeSeries timeSeriesB = setupTimeSeriesB();
        var result = timeSeriesAdder.addTimeSeries(timeSeriesA, timeSeriesB);

        assertEquals(4, result.getTimeSlices().size());
        assertEquals(2.0, result.getTimeSlices().get(0).value());
        assertEquals(4.0, result.getTimeSlices().get(1).value());
        assertEquals(1.0, result.getTimeSlices().get(2).value());
        assertEquals(1.0, result.getTimeSlices().get(3).value());

    }

    @Test
    void testSubtractTimeSeries() {
        TimeSeries timeSeriesA = setupTimeSeriesA();
        TimeSeries timeSeriesB = setupTimeSeriesB();
        var result = timeSeriesSubtractor.subtractTimeSeries(timeSeriesA, timeSeriesB);

        assertEquals(5, result.getTimeSlices().size());
        assertEquals(2.0, result.getTimeSlices().get(0).value());
        assertEquals(-2.0, result.getTimeSlices().get(1).value());
        assertEquals(2.0, result.getTimeSlices().get(2).value());
        assertEquals(-1.0, result.getTimeSlices().get(3).value());
        assertEquals(-1.0, result.getTimeSlices().get(4).value());

    }

    private TimeSeries setupTimeSeriesA() {
        TimeSeries timeSeries = new TimeSeries(LocalDateTime.of(2023, 1, 3, 0, 0), LocalDateTime.of(2023, 1, 8, 0, 0));
        timeSeries.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 3, 0, 0), LocalDateTime.of(2023, 1, 5, 0, 0), 2.0));
        timeSeries.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 6, 0, 0), LocalDateTime.of(2023, 1, 7, 0, 0), 3.0));

        return timeSeries;
    }
    private TimeSeries setupTimeSeriesB() {
        TimeSeries timeSeries = new TimeSeries(LocalDateTime.of(2023, 1, 4, 0, 0), LocalDateTime.of(2023, 1, 10, 0, 0));
        timeSeries.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 5, 0, 0), LocalDateTime.of(2023, 1, 6, 0, 0), 2.0));
        timeSeries.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 6, 0, 0), LocalDateTime.of(2023, 1, 8, 0, 0), 1.0));
        timeSeries.addTimeSlice(new TimeSlice(LocalDateTime.of(2023, 1, 9, 0, 0), LocalDateTime.of(2023, 1, 10, 0, 0), 1.0));

        return timeSeries;
    }
}
