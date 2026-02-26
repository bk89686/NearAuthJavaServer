package com.humansarehuman.blue2factor.authentication.internal;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.ConnectedAndConnectionType;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

@Controller
@RequestMapping(Urls.STATUS)
@SuppressWarnings("ucd")
public class Status extends B2fApi {

	public class GroupStatuses {
		public ArrayList<ConnectionStatus> connectionStatuses;
		public ArrayList<DeviceStatus> deviceStatuses;
		public String name;

		public GroupStatuses(ArrayList<ConnectionStatus> connectionStatuses, ArrayList<DeviceStatus> deviceStatuses,
				String name) {
			this.connectionStatuses = connectionStatuses;
			this.deviceStatuses = deviceStatuses;
			this.name = name;
		}

		public ArrayList<ConnectionStatus> getConnectionStatuses() {
			return connectionStatuses;
		}

		public ArrayList<DeviceStatus> getDeviceStatuses() {
			return deviceStatuses;
		}

		public String getName() {
			return name;
		}

	}

	public class ConnectionStatus {
		public String peripheralType;
		public String centralType;
		public boolean proximate;
		public String lastConnection;

		public ConnectionStatus(String peripheralType, String centralType, boolean proximate, String lastConnection) {
			this.peripheralType = peripheralType;
			this.centralType = centralType;
			this.proximate = proximate;
			this.lastConnection = lastConnection;
		}

		public String getPeripheralType() {
			return peripheralType;
		}

		public String getCentralType() {
			return centralType;
		}

		public boolean isProximate() {
			return proximate;
		}

		public String getLastConnection() {
			return lastConnection;
		}

	}

	public class DeviceStatus {
		public String deviceType;
		public boolean proximate;
		public boolean permGiven;

		public DeviceStatus(String deviceType, boolean proximate, boolean permGiven) {
			this.deviceType = deviceType;
			this.proximate = proximate;
			this.permGiven = permGiven;
		}

		public String getDeviceType() {
			return deviceType;
		}

		public boolean isProximate() {
			return proximate;
		}

		public boolean isPermGiven() {
			return permGiven;
		}

	}

	public class CompanyValues {
		public String companyName;
		public String companyId;

		public CompanyValues(String companyName, String companyId) {
			super();
			this.companyName = companyName;
			this.companyId = companyId;
		}

		public String getCompanyName() {
			return companyName;
		}

		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}

		public String getCompanyId() {
			return companyId;
		}

		public void setCompanyId(String companyId) {
			this.companyId = companyId;
		}

	}

	@RequestMapping(method = RequestMethod.GET)
	public String processStatusGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		// get all users with active connections
		ArrayList<CompanyValues> companyList = getAllActiveCompanyNames();
		model.addAttribute("companies", companyList);
		return "status";
	}

	public ArrayList<CompanyValues> getAllActiveCompanyNames() {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		ArrayList<CompanyDbObj> companies = dataAccess.getAllActiveCompanies();
		CompanyValues compValue;
		ArrayList<CompanyValues> companyNames = new ArrayList<>();
		for (CompanyDbObj company : companies) {
			compValue = new CompanyValues(company.getCompanyName(), company.getCompanyId());
			companyNames.add(compValue);
		}
		return companyNames;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processStatusPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String compId = this.getRequestValue(request, "company");
		ArrayList<CompanyValues> companyList = getAllActiveCompanyNames();
		model.addAttribute("companies", companyList);
		// get all users with active connections
		ArrayList<GroupStatuses> groupStatuses = new ArrayList<>();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		ArrayList<GroupDbObj> groups = dataAccess.getGroupsByCompanyId(compId);
		for (GroupDbObj group : groups) {
			ArrayList<ConnectionStatus> connectionStatuses = new ArrayList<>();
			ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForGroup(group);
			for (DeviceConnectionDbObj connection : connections) {
				boolean connected = dataAccess.isProximateAccessAllowed(connection, false);
				DeviceDbObj perf = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId());
				if (perf != null) {
					if (!perf.getOperatingSystem().toString().equals(OsClass.UNKNOWN.toString())) {
						DeviceDbObj central = dataAccess.getDeviceByDeviceId(connection.getCentralDeviceId());
						String lastConnection = getLastConnection(connection);
						ConnectionStatus connStatus = new ConnectionStatus(perf.getDeviceType(),
								central.getDeviceType(), connected, lastConnection);
						connectionStatuses.add(connStatus);
					}
				}
			}
			ArrayList<DeviceStatus> deviceStatuses = new ArrayList<>();
			ArrayList<DeviceDbObj> devices = dataAccess.getNonTempDevicesByGroupId(group.getGroupId(), true);
			for (DeviceDbObj device : devices) {
				boolean prox = dataAccess.isProximateAccessAllowed(device, false);
				ConnectedAndConnectionType cct = dataAccess.didGiveAccess(device);
				DeviceStatus devStatus = new DeviceStatus(device.getDeviceType(), prox, cct.isConnected());
				deviceStatuses.add(devStatus);
			}
			GroupStatuses grp = new GroupStatuses(connectionStatuses, deviceStatuses, group.getGroupName());
			groupStatuses.add(grp);
		}
		model.addAttribute("groups", groupStatuses);
		return "status";
	}

	private String getLastConnection(DeviceConnectionDbObj conn) {
		Timestamp last;// = null;
		if (conn.getLastSuccess().after(conn.getLastSubscribed())) {
			if (conn.getLastSuccess().after(conn.getLastPeripheralConnectionSuccess())
					|| conn.getLastSuccess().after(conn.getLastCentralConnectionSuccess())) {
				last = conn.getLastSuccess();
			} else {
				if (conn.getLastCentralConnectionSuccess().after(conn.getLastPeripheralConnectionSuccess())) {
					last = conn.getLastCentralConnectionSuccess();
				} else {
					last = conn.getLastPeripheralConnectionSuccess();
				}
			}

		} else {
			if (conn.getLastSubscribed().after(conn.getLastPeripheralConnectionSuccess())
					|| conn.getLastSubscribed().after(conn.getLastCentralConnectionSuccess())) {
				last = conn.getLastSubscribed();
			} else {
				if (conn.getLastCentralConnectionSuccess().after(conn.getLastPeripheralConnectionSuccess())) {
					last = conn.getLastCentralConnectionSuccess();
				} else {
					last = conn.getLastPeripheralConnectionSuccess();
				}
			}
		}
		LocalDate ld = last.toInstant().atZone(ZoneId.of("America/New_York")).toLocalDate();
		LocalTime lt = last.toInstant().atZone(ZoneId.of("America/New_York")).toLocalTime();
		DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yy");
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("h:mma");
		return ld.format(df) + " " + lt.format(dt);
	}

}
