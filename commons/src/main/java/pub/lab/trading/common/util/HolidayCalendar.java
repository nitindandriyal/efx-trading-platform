package pub.lab.trading.common.util;

import java.time.LocalDate;

public class HolidayCalendar {

    // TODO - Get config service to publish value dates for different ccyPairs
    public static long getValueDate() {
        return LocalDate.now().plusDays(2).toEpochDay();
    }
}
