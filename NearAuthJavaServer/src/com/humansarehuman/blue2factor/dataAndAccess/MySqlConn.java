package com.humansarehuman.blue2factor.dataAndAccess;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.utilities.Logging;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

class MySqlConn {

	private static HikariDataSource dataSource;

	static {
		// Configure HikariCP
		setupDataSource();
	}
	
	private static void setupDataSource() {
		HikariConfig config = new HikariConfig();
		try {
			InputStream propFile = new MySqlConn().getClass().getResourceAsStream("/application.properties");
			if (propFile != null) {
				Properties prop = new Properties();
				prop.load(propFile);
				Class.forName(prop.getProperty("spring.datasource.driver-class-name"));
				String dbName = prop.getProperty("db.name");
				String userName = prop.getProperty("db.userName");
				String password = prop.getProperty("db.password");
				String hostname = prop.getProperty("db.ip");
				String port = prop.getProperty("db.port");
				config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + dbName);
				config.setUsername(userName);
				config.setPassword(password);
				config.setMaximumPoolSize(Constants.MAX_CONNECTIONS);
				config.setMinimumIdle(5); // Min idle connections
				config.setConnectionTimeout(30000); // 30 seconds
				config.setIdleTimeout(600000); // 10 minutes
				config.setMaxLifetime(1800000); // 30 minutes
				dataSource = new HikariDataSource(config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			setupDataSource();
		}
		return dataSource.getConnection();
	}

	public static void close(ResultSet rs, PreparedStatement prepStmt, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (prepStmt != null) {
			try {
				prepStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				Logging logger = new Logging(new MySqlConn().getClass());
				logger.error("MySqlConn", e);
			}
		}
	}

	public static void close(PreparedStatement prepStmt, Connection conn) {
		close(null, prepStmt, conn);
	}

}
