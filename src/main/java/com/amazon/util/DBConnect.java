package com.amazon.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnect {

	public void createConnection() {
		Properties prop = new Properties();
		InputStream file = null;
		String host = null;
		String username = null;
		String password = null;
		try {
			file = new FileInputStream("db.properties");
		} catch (FileNotFoundException e2) {
			System.err.println("Couldn't find db property file");
			e2.printStackTrace();
		}

		try {
			prop.load(file);
			host = prop.getProperty("host");
			username = prop.getProperty("user");
			password = prop.getProperty("password");
		} catch (IOException e1) {
			System.err.println("Couldn't load properties from file");
			e1.printStackTrace();
		}
		try {
			Connection con = DriverManager.getConnection(host, username, password);
			Statement stmt = con.createStatement();
			ResultSet result = stmt.executeQuery("select 1  from dual");
			System.out.println(result.getInt(0));
		} catch (SQLException e) {
			System.err.println("Can't connect to DB");
			e.printStackTrace();
		}
	}
}
