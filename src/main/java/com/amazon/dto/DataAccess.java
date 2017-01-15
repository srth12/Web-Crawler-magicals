package com.amazon.dto;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amazon.util.DBConnect;

public class DataAccess {
	private Statement stmt;
	final static Logger logger = Logger.getLogger(DataAccess.class);

	public DataAccess() {
		DBConnect con = new DBConnect();
		stmt = con.createConnection();
	}

	public void insertData(String tableName, List<String> columnNames, List<String> values) {
		String sql = " insert into " + tableName + " (" + StringUtils.join(columnNames, ",") + ") values( "
				+ StringUtils.join(values, ",") + " )";
		try {
			if(sql.contains("&")){
				sql.replace("&", "' || chr(38) || '");
			}
			stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.err.println("Error in sql"+sql);
			logger.error("Error in sql", e);
			e.printStackTrace();
		}
	}

	public void dropTable(String tableName) {
		
		String sql="delete "+tableName;
		try {
			stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.err.println("Failed to drop table: "+tableName);
			logger.error("Failed to drop table: "+tableName,e);
			e.printStackTrace();
		}
	}
}
