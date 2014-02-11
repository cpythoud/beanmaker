package org.beanmaker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.List;

import org.dbbeans.util.Strings;

public abstract class AbstractDatabaseServer implements DatabaseServer {
	
	public AbstractDatabaseServer(final String serverFQDN, final int serverPort, final String username, final String password, final String engineName, final String driverName) {
		this.serverFQDN = serverFQDN;
		this.serverPort = serverPort;
		this.username = username;
		this.password = password;
		this.engineName = engineName;
		this.driverName = driverName;
	}
	
	public abstract List<String> getAvailableDatabases();
	
	public abstract List<String> getTables(final String dbName);
	
	public abstract List<Column> getColumns(final String dbName, final String tableName);
	
	public abstract List<OneToManyRelationship> getDetectedOneToManyRelationship(final String dbName, final String tableName);

	
	protected Connection getConnection(final String databaseName) {
		Connection conn;
		
		try {
			Class.forName(driverName);
			conn = DriverManager.getConnection(getURL(databaseName));
		} catch (final ClassNotFoundException cnfex) {
			throw new RuntimeException(cnfex);
		} catch (final SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
		
		return conn;
	}
	
	protected String suggestBeanClass(final String tableName) {
		final String base = Strings.capitalize(Strings.camelize(tableName));
		
		if (base.endsWith("ies"))
			return base.substring(0, base.length() - 3) + "y";
		
		if (base.endsWith("s"))
			return base.substring(0, base.length() - 1);
		
		return base;
	}
	
	protected String suggestJavaName(final String tableName) {
		return Strings.uncapitalize(Strings.camelize(tableName));
	}
	
	private String getURL(final String databaseName) {
        return "jdbc:" + engineName + "://" + serverFQDN + ":" + serverPort + "/" + databaseName + "?user=" + username + "&password=" + password;
	}
	
	private final String serverFQDN;
	private final int serverPort;
	private final String username;
	private final String password;
	private final String engineName;
	private final String driverName;
}

