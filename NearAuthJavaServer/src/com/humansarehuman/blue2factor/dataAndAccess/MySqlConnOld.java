package com.humansarehuman.blue2factor.dataAndAccess;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.utilities.Logging;

class MySqlConnOld {
	private Connection connection = null;
	@SuppressWarnings("unused")
	private Logging logger = new Logging(this.getClass());

	public MySqlConnOld() {
		if (connection == null) {
			connection = getGccConnection();
		}
	}

	private Connection getGccConnection() {
		Connection con = null;
		try {
			InputStream propFile = getClass().getResourceAsStream("/application.properties");
			if (propFile != null) {
				Properties prop = new Properties();
				prop.load(propFile);
				Class.forName(prop.getProperty("spring.datasource.driver-class-name"));
				String dbName = prop.getProperty("db.name");
				String userName = prop.getProperty("db.userName");
				String password = prop.getProperty("db.password");
				String hostname = prop.getProperty("db.ip");
				String port = prop.getProperty("db.port");
				String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName
						+ "&password=" + password + "&useSSL=true&passwordCharacterEncoding=utf8&loglevel=2"
						+ "&useUnicode=true&characterEncoding=UTF-8";
				con = DriverManager.getConnection(jdbcUrl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	@SuppressWarnings("all")
	private static Connection getAwsConnection() {
		if (System.getProperty("RDS_HOSTNAME") != null) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				String dbName = "";
				String userName = "";
				String password = "";
				String hostname = "";
				String port = "";
				String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName
						+ "&password=" + password + "&useSSL=false";
				Connection con = DriverManager.getConnection(jdbcUrl);
				return con;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("the RDS_HOSTNAME was null");
		}
		return null;
	}

	public Connection getConnection() {
		return connection;
	}

	void closeConnection(String source) {
		DataAccess dataAccess = new DataAccess();
		if (!source.equals("none")) {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
					if (!source.equals("addLog")) {
						dataAccess.addLog("closeConnection", "connection closed for " + source, LogConstants.TRACE);
					}
				} else {
					if (!source.equals("addLog")) {
						dataAccess.addLog("closeConnection", "connection null for " + source, LogConstants.WARNING);
					}
				}
			} catch (Exception e) {
				if (!source.equals("addLog")) {
					dataAccess.addLog("closeConnection", e);
				}
			}
		}
	}

	void closeConnection() {
		closeConnection(DataAccess.getMethodNameInLogFn());

	}
}