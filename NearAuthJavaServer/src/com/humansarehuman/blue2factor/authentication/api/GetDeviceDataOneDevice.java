package com.humansarehuman.blue2factor.authentication.api;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.threeten.bp.Year;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.DeviceDataOneDevice;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.DeviceDataOneDeviceApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.DEVICE_DATA_ONE_DEVICE)
@SuppressWarnings("ucd")
public class GetDeviceDataOneDevice extends B2fApi {

	private DeviceDataOneDeviceApiResponse deviceData;
	private boolean includeFinalWeek = false;
	private int logLevel = LogConstants.TRACE;
	ZoneId easternTime = ZoneId.of("America/New_York");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/y");
	DateTimeFormatter dayNameFormatter = DateTimeFormatter.ofPattern("EEEE");
	CompanyDataAccess dataAccess = new CompanyDataAccess();
	private static String TOTAL = "total";
	private static String PROX = "proximity";
	private static String OTHER = "other";

	/**
	 * Right now this is only used when we change the device name
	 * 
	 * @param request
	 * @param httpResponse
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String postDeviceDataOneDevice(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		boolean success = false;
		String newName = this.getRequestValue(request, "popupInput1");
		String deviceId = this.getRequestValue(request, "did");
		dataAccess.addLog("deviceId:" + deviceId + ", newName: " + newName, LogConstants.IMPORTANT);
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		String reason = "";
		if (device != null) {
			device.setDeviceType(newName);
			try {
				if (!dataAccess.updateDevice(device, "postDeviceDataOneDevice")) {
					reason = "changing the device name to \"" + newName + "\" failed";
				} else {
					success = true;
				}
			} catch (Exception e) {
				dataAccess.addLog(e);
				reason = e.getLocalizedMessage();
			}
		}
		if (success) {
			model = this.showPage(request, model, device);
		} else {
			DeviceDataOneDeviceApiResponse deviceData = new DeviceDataOneDeviceApiResponse(deviceId);
			deviceData.setOutcome(Outcomes.FAILURE);
			deviceData.setReason(reason);
			model.addAttribute("deviceData", deviceData);
		}
		model.addAttribute("fromPush", true);
		return "deviceTimeline";
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getDeviceDataOneDevice(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String deviceId = this.getRequestValue(request, "did");
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		if (device != null) {
			model = this.showPage(request, model, device);
		} else {
			DeviceDataOneDeviceApiResponse deviceData = new DeviceDataOneDeviceApiResponse(deviceId);
			deviceData.setOutcome(Outcomes.FAILURE);
			deviceData.setReason(Constants.DEV_NOT_FOUND);
			model.addAttribute("deviceData", deviceData);
		}
		model.addAttribute("fromPush", false);
		return "deviceTimeline";
	}

	private ModelMap showPage(HttpServletRequest request, ModelMap model, DeviceDbObj device) {
		TokenDbObj token = this.getPersistentTokenObj(request);
		TimeTracker timeTracker = new TimeTracker();
		String reason = "";
		try {
			if (token != null) {
				GroupDbObj group = dataAccess.getActiveGroupFromToken(token);
				if (group != null && device != null) {
					GroupDbObj deviceGroup = dataAccess.getGroupById(device.getGroupId());
					CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
					try {
						String days = this.getRequestValue(request, "days");
						int iDays = DateTimeUtilities.getCurrentDayOfWeek() + 9;

						if (!TextUtils.isEmpty(days)) {
							iDays = Integer.parseInt(days);
						}
						boolean admin = dataAccess.userIsAdmin(group);
						if (device != null && (admin || device.getGroupId().equals(group.getGroupId())
								|| device.getGroupId().equals(group.getGroupId().substring(2)))) {

							ArrayList<DeviceConnectionDbObj> conns = new ArrayList<>();
							DeviceDbObj central;
							boolean periodIncludesFirstLog;
							String deviceId = device.getDeviceId();
							if (deviceId.startsWith("X-")) {
								deviceId = deviceId.substring(2);
							}
							if (!device.isCentral()) {

								DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(deviceId, true);
								periodIncludesFirstLog = periodIncludesFirstLog(conn, iDays);
								central = dataAccess.getDeviceByDeviceId(conn.getCentralDeviceId());
								conns.add(conn);
							} else {
								conns = dataAccess.getConnectionsForCentral(deviceId);
								periodIncludesFirstLog = periodIncludesFirstLog(conns, iDays);
								central = device;
							}
							while (timeTracker.timePerDay.size() < 1) { // && iDays <= 31) {
								timeTracker = buildTimeTracker(timeTracker, company, conns, device, central, iDays,
										periodIncludesFirstLog);
								iDays += 7;
							}
							model.addAttribute("username", deviceGroup.getUsername());
						} else {
							reason = "You are not authorized";
						}
					} catch (Exception e) {
						dataAccess.addLog(e);
					}
					model = addTrackerToModel(model, timeTracker);
				} else {
					reason = "An error occurred finding your data.";
				}
			} else {
				dataAccess.addLog("browser token not found", LogConstants.WARNING);
				reason = "Your browser needs to be resynchronized with NearAuth.ai to view this data.";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return addAttributesToModel(model, timeTracker, reason);
	}

	private ModelMap addAttributesToModel(ModelMap model, TimeTracker timeTracker, String reason) {
		int outcome;
		if (timeTracker.size() > 0) {
			outcome = Outcomes.SUCCESS;
		} else {
			outcome = Outcomes.FAILURE;
		}
		dataAccess.addLog("setting outcome to " + outcome, logLevel);
		if (deviceData == null) {
			deviceData = new DeviceDataOneDeviceApiResponse("");
		}
		deviceData.setOutcome(outcome);
		deviceData.setReason(reason);
		model.addAttribute("deviceData", deviceData);
		return model;
	}

	private ModelMap addTrackerToModel(ModelMap model, TimeTracker timeTracker) {
		timeTracker = fillDayAndWeekMaps(timeTracker);
//		timeTracker.printAll();
		Collections.sort(timeTracker.timePerWeek, Collections.reverseOrder());
		Collections.sort(timeTracker.timePerDay, Collections.reverseOrder());
		model.addAttribute("dayData", timeTracker.timePerDay);
		model.addAttribute("deviceDataOneDevicesGroupedByDate", timeTracker.groupedByDate);
		model.addAttribute("weekData", timeTracker.timePerWeek);
		model.addAttribute("includeFinalWeek", includeFinalWeek);
		model.addAttribute("showWeeksTable", shouldShowWeeksTable(timeTracker));
		return model;
	}

	private TimeTracker buildTimeTracker(TimeTracker timeTracker, CompanyDbObj company,
			ArrayList<DeviceConnectionDbObj> conns, DeviceDbObj device, DeviceDbObj central, int iDays,
			boolean periodIncludesFirstLog) {
		if (conns.size() > 0) {
			String connId = conns.get(0).getConnectionId();
			deviceData = new DeviceDataOneDeviceApiResponse(device.getDeviceType(), central.getDeviceType(),
					device.getDeviceId(), connId);
			ArrayList<ConnectionLogDbObj> connLogs = dataAccess.getLastDaysConnectionLogs(conns, device, iDays);

			if (periodIncludesFirstLog) {
				connLogs = addTimeBeforeInstall(connLogs);
			} else {
				connLogs = addPreviousConnectionLog(connLogs);
			}
			ArrayList<ConnectionLogDbObj> neededLogs = this.removeUnneededLogs(company, connLogs,
					periodIncludesFirstLog, device.isCentral());
			if (neededLogs.size() > 0) {
				for (int i = 1; i <= neededLogs.size(); i++) {
					timeTracker = addToTimePerDayAndWeek(neededLogs, i, timeTracker);
				}
			} else {
				dataAccess.addLog("log length was 0", LogConstants.WARNING);
			}
		}
		return timeTracker;
	}

	private ArrayList<ConnectionLogDbObj> addTimeBeforeInstall(ArrayList<ConnectionLogDbObj> connLogs) {
		Timestamp newStart = DateTimeUtilities.getTimestampMinusHours(connLogs.get(0).getEventTimestamp(), 24);
		ConnectionLogDbObj newConnLog = new ConnectionLogDbObj(connLogs.get(0).getConnectionId(), null, false, newStart,
				"", "", null);
		connLogs.add(0, newConnLog);
		dataAccess.addLog("adding log starting at " + newStart + "because its the first week", logLevel);
		return connLogs;
	}

	private ArrayList<ConnectionLogDbObj> addPreviousConnectionLog(ArrayList<ConnectionLogDbObj> connLogs) {
		ConnectionLogDbObj previous = dataAccess.getPreviousConnectionLog(connLogs.get(0));
		connLogs.add(0, previous);
		return connLogs;
	}

	private boolean periodIncludesFirstLog(DeviceConnectionDbObj conn, int days) {
		boolean firstIsWithinTime = false;
		Timestamp initialLogTimestamp = dataAccess.getInitialConnectionLogsTimestamp(conn, days);
		if (initialLogTimestamp != null) {
			firstIsWithinTime = DateTimeUtilities.getCurrentTimestampMinusHours(24 * days).before(initialLogTimestamp);
		}
		return firstIsWithinTime;
	}

	private boolean periodIncludesFirstLog(ArrayList<DeviceConnectionDbObj> conns, int days) {
		boolean firstIsWithinTime = false;
		Timestamp initialLogTimestamp = dataAccess.getInitialConnectionLogsTimestamp(conns, days);
		if (initialLogTimestamp != null) {
			firstIsWithinTime = DateTimeUtilities.getCurrentTimestampMinusHours(24 * days).before(initialLogTimestamp);
		}
		return firstIsWithinTime;
	}

	private boolean shouldShowWeeksTable(TimeTracker timeTracker) {
		dataAccess.addLog("totalRecords: " + timeTracker.size(), logLevel);
		return timeTracker.timePerWeek.size() > 1 || (timeTracker.timePerWeek.size() > 0 && includeFinalWeek);
	}

	private boolean shouldShowDate(long dayNumber, TreeMap<Long, ArrayList<DeviceDataOneDevice>> groupedByDate) {
		return dayNumber <= groupedByDate.firstEntry().getKey();
	}

	private TimeTracker fillWeekMap(TimeTracker timeTracker) {
		try {
			Collections.sort(timeTracker.timePerWeek);
			if (timeTracker.timePerWeek.size() > 0 && timeTracker.timePerWeek.get(0) != null) {
				long minWeek = timeTracker.timePerWeek.get(0).weekNumber;
				long maxWeek = DateTimeUtilities.getCurrentWeekOfYear();
				;
				ArrayList<ConnectionEventWithType> connEvents;
				String timeText;
				for (long currWeek = minWeek; currWeek <= maxWeek; currWeek++) {
					TimeWeek thisWeek = getTimeWeekByWeekNumber(timeTracker.timePerWeek, currWeek);
					connEvents = thisWeek.getConnectionEventsWithTypes();
					for (ConnectionEventWithType connEvent : connEvents) {
						timeText = DateTimeUtilities
								.hoursMinutesAndSecondsTimeSpanFromSeconds(connEvent.getElapsedSeconds());
						connEvent.setElapsedTimeText(timeText);
						dataAccess.addLog("adding connType: " + connEvent.getConnectionType() + " = "
								+ connEvent.getElapsedTimeText() + " on " + thisWeek.getWeekString(), logLevel);
					}
					timeTracker.updateTimePerWeekTimeWeek(thisWeek);
				}
			} else {
				dataAccess.addLog("looks like our day data is empty", LogConstants.WARNING);
			}
		} catch (

		Exception e) {
			dataAccess.addLog(e);
		}
		return timeTracker;
	}

	private TimeTracker fillDayAndWeekMaps(TimeTracker timeTracker) {
		timeTracker = fillDayMap(timeTracker);
		return fillWeekMap(timeTracker);
	}

	private TimeTracker fillDayMap(TimeTracker timeTracker) {
		try {
			Collections.sort(timeTracker.timePerDay);
			if (timeTracker.timePerDay.size() > 0 && timeTracker.timePerDay.get(0) != null) {
				long minDay = timeTracker.timePerDay.get(0).dayNumber;
				long today = DateTimeUtilities.daysSinceEpoch();
				ArrayList<ConnectionEventWithType> connEvents;
				String timeText;
				for (long currDay = minDay; currDay <= today; currDay++) {
					if (shouldShowDate(currDay, timeTracker.groupedByDate)) {
						timeTracker = addDayIfNeeded(timeTracker, currDay);
						TimeDay thisDay = getTimeDayByDayNumber(timeTracker, currDay);
						dataAccess.addLog("working with " + thisDay.getDateString(), logLevel);
						connEvents = thisDay.getConnectionEventsWithTypes();
						for (ConnectionEventWithType connEvent : connEvents) {
							timeText = DateTimeUtilities
									.hoursMinutesAndSecondsTimeSpanFromSeconds(connEvent.getElapsedSeconds());
							connEvent.setElapsedTimeText(timeText);
							dataAccess.addLog(
									"adding connType: " + connEvent.getConnectionType() + " = "
											+ connEvent.getElapsedTimeText() + " on " + thisDay.getDateString(),
									logLevel);
						}
						timeTracker.updateTimePerDayTimeDay(thisDay);
					} else {
						dataAccess.addLog("not showing " + currDay + "because it is before the first groupedByDate",
								logLevel);
					}
				}
			} else {
				dataAccess.addLog("looks like our day data is empty", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return timeTracker;
	}

	private String[] longToDateArray(long lDate) {
		LocalDate localDate = LocalDate.ofEpochDay(lDate);
		String[] dateArray = { localDate.format(formatter), localDate.format(dayNameFormatter) };
		return dateArray;
	}

	private TimeTracker addToTimePerDayAndWeek(ArrayList<ConnectionLogDbObj> connLogs, int iteration,
			TimeTracker timeTracker) {
		ConnectionLogDbObj baseRecord = connLogs.get(iteration - 1);
		Timestamp startOfInterval = baseRecord.getEventTimestamp();
		Timestamp endOfInterval;
		if (iteration == connLogs.size()) {
			endOfInterval = DateTimeUtilities.getCurrentTimestamp();
		} else {
			endOfInterval = connLogs.get(iteration).getEventTimestamp();
		}

		Instant startInstant = startOfInterval.toInstant();
		ZonedDateTime zonedStart = ZonedDateTime.ofInstant(startInstant, easternTime);
		dataAccess.addLog("start: " + zonedStart.toString(), logLevel);
		Instant endInstant = endOfInterval.toInstant();
		ZonedDateTime zonedEnd = ZonedDateTime.ofInstant(endInstant, easternTime);
		dataAccess.addLog("end: " + zonedEnd.toString(), logLevel);
		long intervalSeconds;
		while (zonedEnd.isAfter(zonedStart)) {
			if (zonedEnd.getDayOfYear() == zonedStart.getDayOfYear()) {
				intervalSeconds = ChronoUnit.SECONDS.between(zonedStart, zonedEnd);
			} else {
				intervalSeconds = getSecondsToEndOfDay(zonedStart);
			}
			String timespan = DateTimeUtilities.hoursMinutesAndSecondsTimeSpanFromSeconds(intervalSeconds);
			String connTypeNonSpecific = "";
			String connType = "";
			if (baseRecord.getConnectionType() != null) {
				connType = baseRecord.getConnectionType().getStringName();
				if (baseRecord.getConnectionType() == ConnectionType.PROX) {
					connTypeNonSpecific = PROX;
				} else {
					connTypeNonSpecific = OTHER;
				}
			}
			dataAccess.addLog("adding event at " + startInstant + " lasting" + timespan, logLevel);
			DeviceDataOneDevice oneDeviceEvent = new DeviceDataOneDevice(Timestamp.from(startInstant),
					baseRecord.isConnected(), connType, timespan, intervalSeconds, baseRecord.getSrc(),
					baseRecord.getDescription());
			deviceData.addDevice(oneDeviceEvent);
			long lDate = zonedStart.toLocalDate().toEpochDay();

			if (baseRecord.isConnected()) {
				timeTracker = addToTimePerDay(timeTracker, lDate, intervalSeconds, connTypeNonSpecific);
				timeTracker = addToTimePerWeek(timeTracker, zonedStart, intervalSeconds, connTypeNonSpecific);
			}
			ArrayList<DeviceDataOneDevice> currEvents;
			if (timeTracker.groupedByDate.containsKey(lDate)) {
				currEvents = timeTracker.groupedByDate.get(lDate);
			} else {
				currEvents = new ArrayList<>();
			}
			currEvents.add(oneDeviceEvent);
			timeTracker.groupedByDate.put(lDate, currEvents);
			if (DateTimeUtilities.getCurrentTimestampMinusHours(24 * 7).before(connLogs.get(0).getEventTimestamp())) {
				// this is the first week
				includeFinalWeek = true;
			} else {
				if ((connLogs.size() - 1) == iteration) {
					if (zonedStart.getDayOfWeek() == DayOfWeek.SUNDAY) {
						includeFinalWeek = true;
					} else {
						includeFinalWeek = false;
					}
				}
			}
			zonedStart = zonedStart.truncatedTo(ChronoUnit.DAYS).plusDays(1);
			startInstant = zonedStart.toInstant();
		}
		return timeTracker;
	}

	private TimeTracker addToTimePerWeek(TimeTracker timeTracker, ZonedDateTime zonedStart, long intervalSeconds,
			String connTypeString) {
		long weekOfYear = zonedStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
		TimeWeek timeWeek = getTimeWeekByWeekNumber(timeTracker.timePerWeek, weekOfYear);
		ConnectionEventWithType totalConnEvent = timeWeek.getConnectionEventForType(TOTAL);
		totalConnEvent.addElapsedSeconds(intervalSeconds);
		ConnectionEventWithType typeConnEvent = timeWeek.getConnectionEventForType(connTypeString);
		if (typeConnEvent != null) {
			typeConnEvent.addElapsedSeconds(intervalSeconds);
			timeWeek.setConnectionEventWithType(totalConnEvent);
			timeWeek.setConnectionEventWithType(typeConnEvent);
			timeTracker.addTimePerWeek(timeWeek);
		}
		dataAccess.addLog("adding tracker: " + timeTracker.toString());
		return timeTracker;
	}

	private TimeTracker addToTimePerDay(TimeTracker timeTracker, long lDate, long intervalSeconds,
			String connTypeString) {
		addDayIfNeeded(timeTracker, lDate);
		TimeDay timeDay = this.getTimeDayByDayNumber(timeTracker, lDate);
		ConnectionEventWithType totalConnEvent = timeDay.getConnectionEventForType(TOTAL);
		totalConnEvent.addElapsedSeconds(intervalSeconds);
		ConnectionEventWithType typeConnEvent = timeDay.getConnectionEventForType(connTypeString);
		if (typeConnEvent != null) {
			typeConnEvent.addElapsedSeconds(intervalSeconds);
			timeDay.setConnectionEventWithType(totalConnEvent);
			timeDay.setConnectionEventWithType(typeConnEvent);
			timeTracker.addTimePerDay(timeDay);
		}
		dataAccess.addLog("adding tracker: " + timeTracker.toString());
		return timeTracker;
	}

	private String getWeekText(long weekNum) {
		String wkStr;
		long currentWeek = DateTimeUtilities.getCurrentWeekOfYear();
		if (weekNum == currentWeek) {
			wkStr = "this week";
		} else {
			LocalDate firstDay = getDateOfTheFirstDayOfWeek(weekNum);
			LocalDate lastDay = firstDay.plusDays(6);
			wkStr = firstDay.format(formatter) + " - " + lastDay.format(formatter);
		}
		return wkStr;
	}

	private LocalDate getDateOfTheFirstDayOfWeek(long weeknum) {
		int year = Year.now().getValue();
		Locale locale = Locale.UK; // Monday start
		return LocalDate.of(year, 2, 1).with(WeekFields.of(locale).getFirstDayOfWeek())
				.with(WeekFields.of(locale).weekOfWeekBasedYear(), weeknum);
	}

	private long getSecondsToEndOfDay(ZonedDateTime zonedStart) {
		ZonedDateTime endOfDay = zonedStart.truncatedTo(ChronoUnit.DAYS).plusDays(1);
		return ChronoUnit.SECONDS.between(zonedStart, endOfDay);
	}

	private ArrayList<ConnectionLogDbObj> removeUnneededLogs(CompanyDbObj company,
			ArrayList<ConnectionLogDbObj> connLogs, boolean periodIncludesFirstLog, boolean dontSetIgnore) {
		ArrayList<ConnectionLogDbObj> neededLogs = new ArrayList<>();
		ConnectionLogDbObj baseLog;
		ConnectionLogDbObj currentLog;
		ConnectionLogDbObj nextLog;
		long seconds = -1;
		int startIndex = getStartingTime(connLogs, periodIncludesFirstLog);
		neededLogs.add(connLogs.get(startIndex));
		boolean connectionChanged;
		ConnectionLogDbObj finalConnLog = null;
		if (startIndex != -1) {
			baseLog = connLogs.get(startIndex);
			int i = startIndex;
			dataAccess.addLog("starting at: " + i, logLevel);
			while (i < connLogs.size()) {
				int j = 1;
				currentLog = connLogs.get(i);
				if (logStatusHasChanged(baseLog, currentLog, i)) {
					// we may have to add a record if the previousRecord was passkey, txt, or
					// admincode and it timed out
					ConnectionType connType = baseLog.getConnectionType();
					if (connType == ConnectionType.ADMIN_CODE || connType == ConnectionType.PASSKEY
							|| connType == ConnectionType.PUSH || connType == ConnectionType.TXT) {
						dataAccess.addLog("new connType: " + connType.connectionTypeName(), logLevel);
						if (didPreviousEventTimeout(company, baseLog, currentLog)) {
							neededLogs = addDisconnectRecord(company, neededLogs, baseLog);
						}
					}
					// now see if it lasts long enough
					seconds = Constants.minLogRecordLength + 1;
					dataAccess.addLog("comparing: " + currentLog.getEventTimestamp() + " - " + currentLog.isConnected()
							+ " to " + j + " to " + (connLogs.size() - 1), logLevel);
					if (i != connLogs.size() - 1) {
						connectionChanged = false;
						while (!connectionChanged && ((i + j) != (connLogs.size()))) {
							nextLog = connLogs.get(i + j);
							if (nextLog.isConnected() != currentLog.isConnected()) {
								connectionChanged = true;
								seconds = getRecordLength(currentLog, nextLog);
							} else {
								dataAccess.addLog(nextLog.isConnected() + " vs. " + currentLog.isConnected(), logLevel);
							}
							j++;
						}
					} else {
						// this is the last record
						if (currentLog.isConnected()) {
							if (currentLog.getConnectionId() != null) {
								DeviceConnectionDbObj lastConn = dataAccess
										.getConnectionByConnectionId(currentLog.getConnectionId());
								if (!dataAccess.isAccessAllowed(lastConn, false)) {
									finalConnLog = dataAccess.getLastConnectionStatusFromLog(lastConn);
								}
							} else {
								DeviceDbObj device = dataAccess.getDeviceByDeviceId(currentLog.getDeviceId());
								if (!dataAccess.isAccessAllowed(device, "")) {
									finalConnLog = dataAccess.getLastConnectionStatusFromLog(device);
								}
							}
						}
					}
					if (seconds > Constants.minLogRecordLength || i == connLogs.size() - 1) {
						neededLogs.add(currentLog);
						baseLog = currentLog;
						dataAccess.addLog("using connLog @ " + currentLog.getEventTimestamp() + ", connected: "
								+ currentLog.isConnected(), logLevel);
					} else {
						if (!dontSetIgnore) {
							dataAccess.updateIgnoreConnectionRecord(currentLog);
							dataAccess.addLog("not using connLog @ " + currentLog.getEventTimestamp()
									+ " because it is too short: " + seconds + " seconds.", logLevel);
						}
					}
				} else {
					if (!dontSetIgnore) {
						dataAccess.updateIgnoreConnectionRecord(currentLog);
						dataAccess.addLog("not using connLog @ " + currentLog.getEventTimestamp()
								+ " because the connection status for both is " + currentLog.isConnected()
								+ " when compared with " + baseLog.getEventTimestamp(), logLevel);
					}
				}
				i++;
			}
		}
		if (finalConnLog != null) {
			neededLogs.add(finalConnLog);
		}
		dataAccess.addLog("log # before removal: " + connLogs.size() + "; after: " + neededLogs.size(), logLevel);
		printLogs("neededLogs", neededLogs);
		return neededLogs;
	}

	private void printLogs(String name, ArrayList<ConnectionLogDbObj> connLogs) {
		for (ConnectionLogDbObj connLog : connLogs) {
			dataAccess.addLog(name + ": " + connLog.toString(), logLevel);
		}
	}

	private boolean logStatusHasChanged(ConnectionLogDbObj baseLog, ConnectionLogDbObj currentLog, int iteration) {
		dataAccess.addLog("comparing: " + currentLog.getEventTimestamp() + " - " + currentLog.isConnected() + " via "
				+ currentLog.getConnectionType() + " to " + baseLog.getEventTimestamp() + " - " + baseLog.isConnected()
				+ " via " + baseLog.getConnectionType(), logLevel);
		return iteration == 0 || (currentLog.isConnected() != baseLog.isConnected())
				|| ((currentLog.getConnectionType() != baseLog.getConnectionType() && currentLog.isConnected()));
	}

	private ArrayList<ConnectionLogDbObj> addDisconnectRecord(CompanyDbObj company,
			ArrayList<ConnectionLogDbObj> neededLogs, ConnectionLogDbObj previousLog) {
		Timestamp ts = getTimeoutTimestamp(company, previousLog);
		ConnectionLogDbObj newLog = new ConnectionLogDbObj(previousLog.getConnectionId(), previousLog.getDeviceId(),
				false, ts, previousLog.getSrc(), previousLog.getDescription(), null);
		neededLogs.add(newLog);
		return neededLogs;
	}

	private Timestamp getTimeoutTimestamp(CompanyDbObj company, ConnectionLogDbObj previousLog) {
		ConnectionType connType = previousLog.getConnectionType();
		long timeoutSeconds = 0;
		switch (connType) {
		case ADMIN_CODE:
		case PASSKEY:
			timeoutSeconds = company.getPasskeyTimeoutSeconds();
			break;
		case PUSH:
			timeoutSeconds = company.getPushTimeoutSeconds();
			break;
		case TXT:
			timeoutSeconds = company.getTextTimeoutSeconds();
			break;
		default:
			break;
		}
		return DateTimeUtilities.addSeconds(previousLog.getEventTimestamp(), timeoutSeconds);
	}

	private boolean didPreviousEventTimeout(CompanyDbObj company, ConnectionLogDbObj previousLog,
			ConnectionLogDbObj currentLog) {
		boolean timedOut = false;
		long timeDifferenceInSeconds = DateTimeUtilities.timestampDifferenceInSeconds(previousLog.getEventTimestamp(),
				currentLog.getEventTimestamp());
		if (previousLog.getConnectionType() == ConnectionType.ADMIN_CODE) {
			timedOut = timeDifferenceInSeconds > company.getTextTimeoutSeconds();
		} else if (previousLog.getConnectionType() == ConnectionType.PASSKEY) {
			timedOut = timeDifferenceInSeconds > company.getPasskeyTimeoutSeconds();
			dataAccess.addLog(
					"is " + timeDifferenceInSeconds + " > " + company.getPasskeyTimeoutSeconds() + "? " + timedOut,
					logLevel);
		} else if (previousLog.getConnectionType() == ConnectionType.PUSH) {
			timedOut = timeDifferenceInSeconds > company.getPushTimeoutSeconds();
		} else if (previousLog.getConnectionType() == ConnectionType.TXT) {
			timedOut = timeDifferenceInSeconds > company.getTextTimeoutSeconds();
		}
		dataAccess.addLog("connType: " + previousLog.getConnectionType().connectionTypeName() + " timed out: "
				+ timedOut + " because the difference was " + timeDifferenceInSeconds + " seconds between "
				+ previousLog.getEventTimestamp() + " and " + currentLog.getEventTimestamp(), logLevel);
		return timedOut;
	}

	private long getRecordLength(ConnectionLogDbObj firstRecord, ConnectionLogDbObj secondRecord) {
		long seconds = DateTimeUtilities.absoluteTimestampDifferenceInSeconds(firstRecord.getEventTimestamp(),
				secondRecord.getEventTimestamp());
		dataAccess.addLog("difference between " + firstRecord.getEventTimestamp() + " and "
				+ secondRecord.getEventTimestamp() + " = " + seconds + " seconds", logLevel);
		return seconds;
	}

	private int getStartingTime(ArrayList<ConnectionLogDbObj> connLogs, boolean periodIncludesFirstLog) {
		int startIndex = -1;
		ConnectionLogDbObj nextLog;
		ConnectionLogDbObj currentLog;
		for (int i = 0; i < connLogs.size(); i++) {
			currentLog = connLogs.get(i);
			nextLog = connLogs.get(i + 1);
			if (currentLog.isConnected() != nextLog.isConnected() || periodIncludesFirstLog) {
				if (DateTimeUtilities.absoluteTimestampDifferenceInSeconds(currentLog.getEventTimestamp(),
						nextLog.getEventTimestamp()) > 30 || periodIncludesFirstLog) {
					startIndex = i;
					dataAccess.addLog("starting time: " + currentLog.getEventTimestamp() + " at index: " + i, logLevel);
					break;
				}
			}
		}
		return startIndex;
	}

	public class TimeTracker {
		ArrayList<TimeDay> timePerDay;
		ArrayList<TimeWeek> timePerWeek;
		TreeMap<Long, ArrayList<DeviceDataOneDevice>> groupedByDate;
		int logLvl = LogConstants.TRACE;

		public TimeTracker() {
			timePerDay = new ArrayList<>();
			groupedByDate = new TreeMap<Long, ArrayList<DeviceDataOneDevice>>(Collections.reverseOrder());
			timePerWeek = new ArrayList<>();
		}

		public void addTimePerDay(TimeDay newTimeDay) {
			boolean found = false;
			for (TimeDay timeDay : timePerDay) {
				if (timeDay.getDayNumber() == newTimeDay.getDayNumber()) {
					timeDay.setConnectionEventsWithTypes(newTimeDay.getConnectionEventsWithTypes());
					found = true;
					break;
				}
			}
			if (!found) {
				timePerDay.add(newTimeDay);
			}
		}

		public void updateTimePerDayTimeDay(TimeDay timeDay) {
			for (TimeDay td : timePerDay) {
				if (td.getDayNumber() == timeDay.getDayNumber()) {
					td.setDateString(timeDay.getDateString());
					td.setConnectionEventsWithTypes(timeDay.getConnectionEventsWithTypes());
					break;
				}
			}
		}

		public void addTimePerWeek(TimeWeek newTimeWeek) {
			boolean found = false;
			for (TimeWeek timeWeek : timePerWeek) {
				if (timeWeek.getWeekNumber() == newTimeWeek.getWeekNumber()) {
					timeWeek.setConnectionEventsWithTypes(newTimeWeek.getConnectionEventsWithTypes());
					found = true;
					break;
				}
			}
			if (!found) {
				timePerWeek.add(newTimeWeek);
			}
		}

		public void updateTimePerWeekTimeWeek(TimeWeek timeWeek) {
			for (TimeDay td : timePerDay) {
				if (td.getDayNumber() == timeWeek.getWeekNumber()) {
					td.setDateString(timeWeek.getWeekString());
					td.setConnectionEventsWithTypes(timeWeek.getConnectionEventsWithTypes());
					break;
				}
			}
		}

		public int size() {
			return timePerDay.size() + groupedByDate.size() + timePerWeek.size();
		}

		public boolean empty() {
			return size() > 0;
		}

		public void printAll() {
			addLog("allRecords");
			printTimePerDay();
			printTimePerWeek();
			printGroupedByDate();

		}

		boolean sync = false;

		private void addLog(String s) {
			String caller = CompanyDataAccess.getMethodNameInLogFn();
			if (sync) {
				dataAccess.addLogSynchronous(caller, s, logLvl);
			} else {
				dataAccess.addLog(caller, s, logLvl);
			}
		}

		public void printTimePerDay() {
			for (TimeDay timeDay : timePerDay) {
				long day = timeDay.getDayNumber();
				String dateString = timeDay.getDateString();
				ArrayList<ConnectionEventWithType> connEvents = timeDay.getConnectionEventsWithTypes();
				for (ConnectionEventWithType connEvent : connEvents) {
					addLog("day #:" + day + ", dateStr: " + dateString + "; connType: " + connEvent.getConnectionType()
							+ "; seconds: " + connEvent.getElapsedSeconds() + "; timeText: "
							+ connEvent.getElapsedTimeText());
				}
			}
		}

		public void printTimePerWeek() {
			addLog("time per week");
			for (TimeWeek timeWeek : timePerWeek) {
				long weekNumber = timeWeek.getWeekNumber();
				String weekString = timeWeek.getWeekString();
				ArrayList<ConnectionEventWithType> connEvents = timeWeek.getConnectionEventsWithTypes();
				for (ConnectionEventWithType connEvent : connEvents) {
					addLog("week #:" + weekNumber + ", dateStr: " + weekString + "; connType: "
							+ connEvent.getConnectionType() + "; seconds: " + connEvent.getElapsedSeconds()
							+ "; timeText: " + connEvent.getElapsedTimeText());
				}
			}
		}

		public void printGroupedByDate() {
			for (Map.Entry<Long, ArrayList<DeviceDataOneDevice>> entry : groupedByDate.entrySet()) {
				for (DeviceDataOneDevice oneD : entry.getValue()) {
					addLog("dateTime: " + oneD.getsDate() + " " + oneD.getsTime() + "; duration: " + oneD.getTimeSpan()
							+ "; connected: " + oneD.getsConnected());
				}
			}
		}
	}

	public class TimeWeek implements Comparable<TimeWeek> {
		private long weekNumber;
		private String weekString;
		private ArrayList<ConnectionEventWithType> connectionEventsWithTypes;

		public TimeWeek(long weekNumber) {
			connectionEventsWithTypes = new ArrayList<>();
			connectionEventsWithTypes.add(new ConnectionEventWithType(TOTAL, 0));
			connectionEventsWithTypes.add(new ConnectionEventWithType(PROX, 0));
			connectionEventsWithTypes.add(new ConnectionEventWithType(OTHER, 0));
			this.weekNumber = weekNumber;
			this.weekString = getWeekText(weekNumber);
		}

		public long getWeekNumber() {
			return weekNumber;
		}

		public void setWeekNumber(long weekNumber) {
			this.weekNumber = weekNumber;
		}

		public String getWeekString() {
			return weekString;
		}

		public void setWeekString(String weekString) {
			this.weekString = weekString;
		}

		public ArrayList<ConnectionEventWithType> getConnectionEventsWithTypes() {
			return connectionEventsWithTypes;
		}

		public ConnectionEventWithType getConnectionEventForType(String connectionType) {
			ConnectionEventWithType evt = null;
			for (ConnectionEventWithType connEvent : connectionEventsWithTypes) {
				if (connEvent.getConnectionType().equals(connectionType)) {
					evt = connEvent;
				}
			}
			if (evt == null) {
				dataAccess.addLog("connectType: " + connectionType + " was not found for " + weekString,
						LogConstants.ERROR);

			}
			return evt;
		}

		public void setConnectionEventsWithTypes(ArrayList<ConnectionEventWithType> connectionEventsWithTypes) {
			this.connectionEventsWithTypes = connectionEventsWithTypes;
		}

		public void setConnectionEventWithType(ConnectionEventWithType connectionEventWithType) {
			for (ConnectionEventWithType connEvent : connectionEventsWithTypes) {
				if (connEvent.getConnectionType().equals(connectionEventWithType.getConnectionType())) {
					connEvent.setElapsedSeconds(connectionEventWithType.getElapsedSeconds());
					break;
				}
			}
		}

		@Override
		public int compareTo(TimeWeek other) {
			return Long.compare(weekNumber, other.weekNumber);
		}

		@Override
		public String toString() {
			String s = "week: " + weekString + " [";
			for (ConnectionEventWithType event : connectionEventsWithTypes) {
				s += " (type: " + event.getConnectionType() + "; elapsedTime: " + event.getElapsedTimeText() + "), ";
			}
			return s.substring(0, -2) + "]";
		}
	}

	public class TimeDay implements Comparable<TimeDay> {
		private long dayNumber;
		private String dateString;
		private String dayString;
		private ArrayList<ConnectionEventWithType> connectionEventsWithTypes;

		public TimeDay(long dayNumber) {
			connectionEventsWithTypes = new ArrayList<>();
			connectionEventsWithTypes.add(new ConnectionEventWithType(TOTAL, 0));
			connectionEventsWithTypes.add(new ConnectionEventWithType(PROX, 0));
			connectionEventsWithTypes.add(new ConnectionEventWithType(OTHER, 0));
			this.dayNumber = dayNumber;
			String[] dateArray = longToDateArray(dayNumber);
			this.dateString = dateArray[0];
			this.dayString = dateArray[1];
		}

		@Override
		public int compareTo(TimeDay other) {
			return Long.compare(dayNumber, other.dayNumber);
		}

		public String getDayString() {
			return dayString;
		}

		public void setDayString(String dayString) {
			this.dayString = dayString;
		}

		public long getDayNumber() {
			return dayNumber;
		}

		public void setDayNumber(long dayNumber) {
			this.dayNumber = dayNumber;
		}

		public String getDateString() {
			return dateString;
		}

		public void setDateString(String dateString) {
			this.dateString = dateString;
		}

		public ArrayList<ConnectionEventWithType> getConnectionEventsWithTypes() {
			return connectionEventsWithTypes;
		}

		public ConnectionEventWithType getConnectionEventForType(String connectionType) {
			ConnectionEventWithType evt = null;
			for (ConnectionEventWithType connEvent : connectionEventsWithTypes) {
				if (connEvent.getConnectionType().equals(connectionType)) {
					evt = connEvent;
				}
			}
			if (evt == null) {
				dataAccess.addLog("connectType: " + connectionType + " was not found for " + dateString,
						LogConstants.ERROR);

			}
			return evt;
		}

		public void setConnectionEventsWithTypes(ArrayList<ConnectionEventWithType> connectionEventsWithTypes) {
			this.connectionEventsWithTypes = connectionEventsWithTypes;
		}

		public void setConnectionEventWithType(ConnectionEventWithType connectionEventWithType) {
			for (ConnectionEventWithType connEvent : connectionEventsWithTypes) {
				if (connEvent.getConnectionType().equals(connectionEventWithType.getConnectionType())) {
					connEvent.setElapsedSeconds(connectionEventWithType.getElapsedSeconds());
					break;
				}
			}
		}

		@Override
		public String toString() {
			String s = "day: " + dateString + ": [";
			for (ConnectionEventWithType event : connectionEventsWithTypes) {
				s += "(type: " + event.getConnectionType() + ", elapsedTime: " + event.getElapsedTimeText() + "), ";
			}
			return s.substring(0, -2) + "]";
		}
	}

	public class ConnectionEventWithType {
		private String connectionType;
		private long elapsedSeconds;
		private String elapsedTimeText;

		public ConnectionEventWithType(String connectionType, long elapsedSeconds) {
			this.connectionType = connectionType;
			this.elapsedSeconds = elapsedSeconds;
		}

		public void updateElapsedTime() {
			this.elapsedTimeText = DateTimeUtilities.hoursMinutesAndSecondsTimeSpanFromSeconds(elapsedSeconds);
		}

		public void addElapsedSeconds(long elapsedSeconds) {
			this.elapsedSeconds = this.elapsedSeconds + elapsedSeconds;
		}

		public long getElapsedSeconds() {
			return elapsedSeconds;
		}

		public void setElapsedSeconds(long elapsedSeconds) {
			this.elapsedSeconds = elapsedSeconds;
		}

		public String getConnectionType() {
			return connectionType;
		}

		public String getElapsedTimeText() {
			return elapsedTimeText;
		}

		public void setElapsedTimeText(String elapsedTimeText) {
			this.elapsedTimeText = elapsedTimeText;
		}

	}

	TimeWeek getTimeWeekByWeekNumber(ArrayList<TimeWeek> timeWeeks, long weekNumber) {
		TimeWeek timeWeek;
		Optional<TimeWeek> foundTimeWeek = timeWeeks.stream().filter(week -> weekNumber == week.getWeekNumber())
				.findFirst();
		if (foundTimeWeek.isPresent()) {
			timeWeek = foundTimeWeek.get();
		} else {
			timeWeek = new TimeWeek(weekNumber);
			dataAccess.addLog("creating new week for week# " + weekNumber, logLevel);
		}
		return timeWeek;
	}

	TimeTracker addDayIfNeeded(TimeTracker timeTracker, long dayNumber) {
		if (getTimeDayByDayNumber(timeTracker, dayNumber) == null) {
			TimeDay timeDay = new TimeDay(dayNumber);
			timeTracker.timePerDay.add(timeDay);
		}
		return timeTracker;
	}

	TimeDay getTimeDayByDayNumber(TimeTracker timeTracker, long dayNumber) {
		ArrayList<TimeDay> timeDays = timeTracker.timePerDay;
		TimeDay timeDay = null;
		Optional<TimeDay> foundTimeDay = timeDays.stream().filter(day -> dayNumber == day.getDayNumber()).findFirst();
		if (foundTimeDay.isPresent()) {
			timeDay = foundTimeDay.get();
		}
		return timeDay;
	}
}
