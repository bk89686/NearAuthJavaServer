package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;
//	private static String DB_NAME = null;
//	private static String PW = null;
//	private static String USER_NAME = null;
//	private static String HOST_NAME = null;
//	private static String PORT = null;

	static {
		config.setJdbcUrl("jdbc_url");
		config.setUsername("database_username");
		config.setPassword("database_password");
		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
	}

	private DataSource() {
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

//	private static String getJdbcUrl() {
//		if (JDBC_URL == null) {
//			setupProperties();
//			JDBC_URL = "jdbc:mysql://" + HOST_NAME + ":" + PORT + "/" + DB_NAME + "?user=" + USER_NAME + "&password="
//					+ PW + "&useSSL=true&passwordCharacterEncoding=utf8&loglevel=2"
//					+ "&useUnicode=true&characterEncoding=UTF-8";
//		}
//		return JDBC_URL;
//	}
//
//	private static void setupProperties() {
//		try {
//			InputStream propFile = getClass().getResourceAsStream("/application.properties");
//			if (propFile != null) {
//				Properties prop = new Properties();
//				prop.load(propFile);
//				Class.forName(prop.getProperty("spring.datasource.driver-class-name"));
//				DB_NAME = prop.getProperty("db.name");
//				USER_NAME = prop.getProperty("db.userName");
//				PW = prop.getProperty("db.password");
//				HOST_NAME = prop.getProperty("db.ip");
//				PORT = prop.getProperty("db.port");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
