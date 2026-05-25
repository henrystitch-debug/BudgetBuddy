package com.github.budgetbuddy.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;

public class TimeUtilsTest {

    @Test
    public void toStartOfDay_zerosOutTimeFields() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 27, 15, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);
        long input = cal.getTimeInMillis();

        long result = TimeUtils.toStartOfDay(input);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTimeInMillis(result);

        assertEquals(2023, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH));
        assertEquals(27, resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, resultCal.get(Calendar.MINUTE));
        assertEquals(0, resultCal.get(Calendar.SECOND));
        assertEquals(0, resultCal.get(Calendar.MILLISECOND));
    }

    @Test
    public void toEndOfDay_setsTimeToEndOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 27, 10, 0, 0);
        long input = cal.getTimeInMillis();

        long result = TimeUtils.toEndOfDay(input);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTimeInMillis(result);

        assertEquals(2023, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH));
        assertEquals(27, resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, resultCal.get(Calendar.MINUTE));
        assertEquals(59, resultCal.get(Calendar.SECOND));
        assertEquals(999, resultCal.get(Calendar.MILLISECOND));
    }

    @Test
    public void getStartOfMonth_returnsFirstDayAtMidnight() {
        long result = TimeUtils.getStartOfMonth(0); // Current month
        
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTimeInMillis(result);

        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, resultCal.get(Calendar.MINUTE));
        assertEquals(0, resultCal.get(Calendar.SECOND));
        assertEquals(0, resultCal.get(Calendar.MILLISECOND));
    }

    @Test
    public void getEndOfMonth_returnsLastDayAtEndOfDay() {
        long result = TimeUtils.getEndOfMonth(0); // Current month
        
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTimeInMillis(result);

        assertEquals(resultCal.getActualMaximum(Calendar.DAY_OF_MONTH), resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, resultCal.get(Calendar.MINUTE));
        assertEquals(59, resultCal.get(Calendar.SECOND));
        assertEquals(999, resultCal.get(Calendar.MILLISECOND));
    }
}
