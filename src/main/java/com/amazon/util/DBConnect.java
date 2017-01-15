package com.amazon.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.driver.OracleDriver;

import com.amazon.dao.FileHandler;

public class DBConnect {

	private Statement stmt;
	public Statement createConnection() {
		String host = null;
		String username = null;
		String password = null;
		FileHandler file = new FileHandler();
		Properties prop = file.readFile("./resources/db.properties");
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e1) {
			System.err.println("Couldn't find the oracle driver");
			e1.printStackTrace();
		}

		host = prop.getProperty("host");
		username = prop.getProperty("user");
		password = prop.getProperty("password");
		try {
			Connection con = DriverManager.getConnection(host, username, password);
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.err.println("Can't connect to DB");
			e.printStackTrace();
		}
		return stmt;
	}
}
