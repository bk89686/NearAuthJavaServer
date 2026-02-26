package com.humansarehuman.blue2factor.utilities;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

public class DateTimeUtilities {

	public static Timestamp buildTimestampFromTimeDateOffset(String time, String date, int offsetMinutes) {
		ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetMinutes * 60);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);
		OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime, zoneOffset);
		return Timestamp.from(offsetDateTime.toInstant());
	}

	public static Timestamp buildTimestampFromTimeDateOffsetWithSeconds(String time, int seconds, int millis,
			String date, int offsetMinutes) {
		ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetMinutes * 60);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		String zeroPaddedSeconds = intToZeroPaddedString(seconds, 2);
		String zeroPaddedMillis = intToZeroPaddedString(millis, 3);
		LocalDateTime dateTime = LocalDateTime
				.parse(date + " " + time + ":" + zeroPaddedSeconds + "." + zeroPaddedMillis, formatter);
		OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime, zoneOffset);
		return Timestamp.from(offsetDateTime.toInstant());
	}

	public static String intToZeroPaddedString(int num, int places) {
		String strNum = Integer.toString(num);
		String paddedStr = "";
		if (strNum.length() > places) {
			paddedStr = strNum.substring(0, places);
		} else if (strNum.length() == places) {
			paddedStr = strNum;
		} else {
			int numZeroes = places - strNum.length();
			for (int i = 0; i < numZeroes; i++) {
				paddedStr += "0";
			}
			paddedStr += strNum;
		}
		return paddedStr;

	}

	public String timestampToReadable(Timestamp timestamp, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String date = sdf.format(timestamp);
		return date;
	}

	public String timestampToReadable(Timestamp timestamp) {
		return timestampToReadable(timestamp, "MMMM d, yyyy 'at' h:mm a");
	}

	public static String timestampToReadableTime(Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		String date = sdf.format(timestamp);
		return date;
	}

	public static String timestampToString(Timestamp ts, String format) {
		String timezoneId = "America/New_York";
		Instant instant = ts.toInstant();
		ZoneId zoneId = ZoneId.of(timezoneId);
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		String tsString = zonedDateTime.format(formatter);
		return tsString;
	}

	public static String timestampToString(Timestamp ts, String format, int offsetMinutes) {
		Instant instant = ts.toInstant();
		int offsetSeconds = (int) TimeUnit.MINUTES.toSeconds(offsetMinutes);
		ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSeconds);

		// 3. Combine the Instant and ZoneOffset into an OffsetDateTime
		OffsetDateTime odt = instant.atOffset(offset);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return odt.format(formatter);
	}

	public static String currentTimestampToReadableAltWithNyTimezone() {
		return timestampToReadableAltWithNyTimezone(getCurrentTimestamp());
	}

	public static String timestampToReadableAltWithNyTimezone(Timestamp timestamp) {
		String tsString = null;
		if (timestamp != null) {
			String timezoneId = "America/New_York";
			String format = "h:mm:ssa M/d/yyyy";
			Instant instant = timestamp.toInstant();
			ZoneId zoneId = ZoneId.of(timezoneId);
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			tsString = zonedDateTime.format(formatter);
		}
		return tsString;
	}

	public static String hoursMinutesAndSecondsAgo(Timestamp timestamp) {
		return hoursMinutesAndSecondsTimeSpan(timestamp, getCurrentTimestamp()) + " ago";
	}

	public static String hoursMinutesAndSecondsTimeSpan(Timestamp timestampStart, Timestamp timestampEnd) {
		long td = (long) timeDifferenceInSeconds(timestampStart, timestampEnd);
		return hoursMinutesAndSecondsTimeSpanFromSeconds(td);
	}

	public static String hoursMinutesAndSecondsTimeSpanFromSeconds(long td) {
		return hoursMinutesAndSecondsTimeSpanFromSeconds(td, "--");
	}

	public static String hoursMinutesAndSecondsTimeSpanFromSeconds(long td, String noTimeString) {
		long hours = td / 3600;
		long remainingSeconds = td % 3600;
		long minutes = remainingSeconds / 60;
		long seconds = remainingSeconds % 60;
		String timeString = "";
		String hourString;
		String minString;
		if (minutes == 1) {
			minString = minutes + " minute";
		} else {
			minString = minutes + " minutes";
		}
		if (hours > 0) {
			if (hours == 1) {
				hourString = hours + " hour";
			} else {
				hourString = hours + " hours";
			}
			timeString = hourString + " and " + minString;
		} else {
			if (minutes > 0) {
				timeString += minString + " and ";
			}
			if (seconds == 1) {
				timeString += seconds + " second";
			} else {
				timeString += seconds + " seconds";
			}
		}
		if (timeString.equals("0 seconds")) {
			timeString = noTimeString;
		}
		return timeString;
	}

	public String dateToString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		String sDate = sdf.format(date);
		return sDate;
	}

	public static String dateToTimeString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
		String sDate = sdf.format(date);
		return sDate;
	}

	public static String getLastTimestampString() {
		return utilDateToSqlStringDt(getCurrentTimestamp());
	}

	public static String utilDateToSqlStringDt(Date date) {
		String sDate;
		if (date == null) {
			sDate = null;
		} else {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			sDate = formatter.format(date);
		}
		return sDate;
	}

	public static double timeDifferenceInSecondsFromNow(Date timeStart) {
		return timeDifferenceInSeconds(timeStart, new Date());
	}

	public static long secondsSinceEpochPlusSeconds(int secondsToAdd) {
		long timeSinceEpoch = new Date().getTime();
		return (timeSinceEpoch / 1000) + secondsToAdd;
	}

	private static double timeDifferenceInSeconds(Date timeStart, Date timeEnd) {
		long time1SinceEpoch = 0;
		if (timeStart != null) {
			time1SinceEpoch = timeStart.getTime();
		}
		long time2SinceEpoch = timeEnd.getTime();
		return Math.abs(time2SinceEpoch - time1SinceEpoch) / 1000;
	}

	public static Date nowPlusSeconds(int secondsToAdd) {
		Date now = new Date();
		return addSeconds(now, secondsToAdd);
	}

	public static Date nowMinusSeconds(int secondsToSubtract) {
		Date now = new Date();
		return subtractSeconds(now, secondsToSubtract);
	}

	public static long timestampSecondAgo(Timestamp ts) {
		Timestamp now = getCurrentTimestamp();
		new DataAccess().addLog("timestampDifferenceInSecondsFromNow", "comparing: " + ts + " to " + now,
				LogConstants.TRACE);
		return timestampDifferenceInSeconds(ts, getCurrentTimestamp());
	}

	public static long timestampDifferenceInSeconds(Timestamp start, Timestamp end) {
		long millisDiff = end.getTime() - start.getTime();
		if (millisDiff == 0) {
			millisDiff = start.getTime() - end.getTime();
		}
		new DataAccess().addLog("timestampDifferenceInSecondsFromNow", "diff: " + (millisDiff / 1000),
				LogConstants.TRACE);
		return millisDiff / 1000;
	}

	public static long absoluteTimestampDifferenceInSeconds(Timestamp ts1, Timestamp ts2) {
		long diff;
		if (ts1.after(ts2)) {
			diff = timestampDifferenceInSeconds(ts2, ts1);
		} else {
			diff = timestampDifferenceInSeconds(ts1, ts2);
		}
		return diff;
	}

	public static Timestamp addSeconds(Timestamp ts, int secondsToAdd) {
		return new Timestamp(ts.getTime() + (secondsToAdd * 1000));
	}

	public static Timestamp addSeconds(Timestamp ts, long secondsToAdd) {
		return new Timestamp(ts.getTime() + (secondsToAdd * 1000));
	}

	private static Date addSeconds(Date dt, int secondsToAdd) {
		Date newTime = new Date();
		newTime.setTime(dt.getTime() + TimeUnit.SECONDS.toMillis(secondsToAdd));
		return newTime;
	}

	private static Date subtractSeconds(Date dt, int secondsToSubtract) {
		Date newTime = new Date();
		newTime.setTime(dt.getTime() - TimeUnit.SECONDS.toMillis(secondsToSubtract));
		return newTime;
	}

	@SuppressWarnings("ucd")
	public static Timestamp subtractSeconds(Timestamp ts, int secondsToSubtract) {
		return new Timestamp(ts.getTime() - (secondsToSubtract * 1000));
	}

//    public static Date subtractSeconds(Date dt, int secondsToSubtract) {
//        Date newTime = new Date();
//        newTime.setTime(dt.getTime() - TimeUnit.SECONDS.toMillis(secondsToSubtract));
//        return newTime;
//    }

	public static Timestamp dateTimeToTimestamp(DateTime datetime) {
		return new Timestamp(datetime.getMillis());
	}

	public static Timestamp instantToTimestamp(Instant instant) {
		return Timestamp.from(instant);
	}

	public static DateTime timestampToDateTime(Timestamp ts) {
		return new DateTime(ts);
	}

	public static boolean withInXMinutes(DateTime datetime, int xMinutes) {
		Timestamp samlTs = new Timestamp(datetime.getMillis());
		Timestamp now = getCurrentTimestamp();
		long diff = absoluteTimestampDifferenceInSeconds(samlTs, now);
		return diff < (xMinutes * 60);
	}

	public static boolean withInXMinutes(Timestamp timestamp, int xMinutes) {
		Timestamp now = getCurrentTimestamp();
		long diff = absoluteTimestampDifferenceInSeconds(timestamp, now);
		return diff < (xMinutes * 60);
	}

	public static Timestamp getCurrentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static Timestamp getCurrentTimestampMinusMinutes(int minutes) {
		new DataAccess().addLog("subtracting " + minutes + " minutes or " + (60 * minutes) + " seconds");
		return getCurrentTimestampMinusSeconds(60 * minutes);
	}

	public static Timestamp getCurrentTimestampMinusHours(int hours) {
		return getCurrentTimestampMinusMinutes(60 * hours);
	}

	public static Timestamp getCurrentTimestampMinusSeconds(int seconds) {
		return getTimestampMinusSeconds(getCurrentTimestamp(), seconds);
	}

	public static Timestamp getTimestampMinusSeconds(Timestamp ts, int seconds) {
		Instant instant = ts.toInstant();
		Instant secondsBefore = instant.minus(seconds, ChronoUnit.SECONDS);
		return Timestamp.from(secondsBefore);
	}

	public static Timestamp getTimestampMinusMinutes(Timestamp ts, int minutes) {
		return getTimestampMinusSeconds(ts, minutes * 60);
	}

	public static Timestamp getTimestampMinusHours(Timestamp ts, int hours) {
		return getTimestampMinusMinutes(ts, hours * 60);
	}

	public static Timestamp getCurrentTimestampPlusSeconds(int seconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.SECOND, seconds);
		Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("getCurrentTimestampPlusSeconds",
				"adding " + seconds + " seconds to " + new Date() + " =  new timestamp: " + timestamp);
		return timestamp;
	}

	public static Timestamp getCurrentTimestampPlusDays(int days) {
		return getCurrentTimestampPlusSeconds(days * 60 * 60 * 24);
	}

	public static Timestamp getBaseTimestamp() {
		return Timestamp.valueOf("1970-01-01 00:00:01");
	}

	public static void main(String[] args) {
		System.out.println(getCurrentTimestamp());
		System.out.println(getCurrentTimestampPlusDays(24));
	}

	public static int getCurrentWeekOfYear() {
		ZonedDateTime currDate = ZonedDateTime.now(ZoneId.of("America/New_York"));
		return currDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
	}

	public static int getDayOfWeek(Timestamp ts) {
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	public static int getCurrentDayOfWeek() {
		return getDayOfWeek(getCurrentTimestamp());
	}

	public static long daysSinceEpoch() {
		LocalDate specificDate = LocalDate.now();
		return specificDate.toEpochDay();
	}

	public static long daysSinceEpoch(ZoneId zone) {
		long daysSinceEpoch;
		LocalDate specificDate = LocalDate.now();
		ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
		if (specificDate.getYear() > (zonedDateTime.getYear())) {
			daysSinceEpoch = specificDate.toEpochDay() - 1;
		} else if (specificDate.getYear() == zonedDateTime.getYear()
				&& specificDate.getDayOfYear() > zonedDateTime.getDayOfYear()) {
			daysSinceEpoch = specificDate.toEpochDay() - 1;
		} else {
			daysSinceEpoch = specificDate.toEpochDay();
		}
		return daysSinceEpoch;
	}
}
