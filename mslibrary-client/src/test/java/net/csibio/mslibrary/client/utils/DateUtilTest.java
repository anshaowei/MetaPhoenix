package net.csibio.mslibrary.client.utils;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTest {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Test
    void getTodayMatchesCurrentDateFormattedAsYyyyMMdd() {
        assertEquals(FORMAT.format(new Date()), DateUtil.getToday());
    }

    @Test
    void getYesterdayIsOneDayBeforeToday() throws ParseException {
        Date today = FORMAT.parse(DateUtil.getToday());
        Date yesterday = FORMAT.parse(DateUtil.getYesterday());
        long diffDays = (today.getTime() - yesterday.getTime()) / (24 * 60 * 60 * 1000);
        assertEquals(1, diffDays);
    }

    @Test
    void getNextDayAddsOneCalendarDay() throws ParseException {
        Date base = FORMAT.parse("20260101");
        Date next = DateUtil.getNextDay(base);
        assertEquals("20260102", FORMAT.format(next));
    }

    @Test
    void getNextDayRollsOverToNextMonth() throws ParseException {
        Date base = FORMAT.parse("20260131");
        Date next = DateUtil.getNextDay(base);
        assertEquals("20260201", FORMAT.format(next));
    }

    @Test
    void getTodayDateAndGetYesterdayDateDifferByOneDay() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtil.getTodayDate());
        cal.add(Calendar.DATE, -1);
        assertEquals(FORMAT.format(cal.getTime()), FORMAT.format(DateUtil.getYesterdayDate()));
    }
}
