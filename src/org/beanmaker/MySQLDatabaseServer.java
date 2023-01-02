package org.beanmaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dbbeans.sql.DBUtils;

public class MySQLDatabaseServer extends AbstractDatabaseServer {
	
	public MySQLDatabaseServer(final String serverFQDN, final int serverPort, final String username, final String password) {
		super(serverFQDN, serverPort, username, password, "mysql", DRIVER_NAME);
	}
	
	public MySQLDatabaseServer(final String serverFQDN, final String username, final String password) {
		super(serverFQDN, DEFAULT_PORT, username, password, "mysql", DRIVER_NAME);
	}

    @Override
	public List<String> getAvailableDatabases() {
		final List<String> dbList = new ArrayList<String>();
		
		Connection conn = null;
		try {
			conn = getConnection("mysql");
			final PreparedStatement stat = conn.prepareStatement("SHOW DATABASES");
            try {
                final ResultSet rs = stat.executeQuery();
                while (rs.next()) {
                    if (!OFF_LIMIT_DBS.contains(rs.getString(1)))
                        dbList.add(rs.getString(1));
                }
                stat.close();
            } finally {
                DBUtils.preparedStatementSilentClose(stat);
            }
            conn.close();
		} catch (final SQLException ex) {
            throw new RuntimeException(ex.getMessage());
		} finally {
            DBUtils.connectionSilentClose(conn);
        }
		
		return dbList;
	}

    @Override
	public List<String> getTables(final String dbName) {
		if (!getAvailableDatabases().contains(dbName))
			throw new IllegalArgumentException("La base de données " + dbName + " est introuvable sur ce serveur.");
		
		final List<String> tableList = new ArrayList<String>();
		
		Connection conn = null;
		try {
			conn = getConnection(dbName);
			final PreparedStatement stat = conn.prepareStatement("SHOW TABLES");
            try {
                final ResultSet rs = stat.executeQuery();
                while (rs.next()) {
                    tableList.add(rs.getString(1));
                }
                stat.close();
            } finally {
                DBUtils.preparedStatementSilentClose(stat);
            }
            conn.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            DBUtils.connectionSilentClose(conn);
        }
		
		return tableList;
	}

    @Override
	public List<Column> getColumns(final String dbName, final String tableName) {
		if (!getAvailableDatabases().contains(dbName))
			throw new IllegalArgumentException("La base de données " + dbName + " est introuvable sur ce serveur.");
		
		if (!getTables(dbName).contains(tableName))
			throw new IllegalArgumentException("La base de données " + dbName + " ne contient pas de table " + tableName + ".");
		
		List<Column> cols = new ArrayList<Column>();
		
		Connection conn = null;
		try {
			conn = getConnection(dbName);
			final PreparedStatement stat = conn.prepareStatement("SELECT * FROM " + tableName);
            try {
                final ResultSetMetaData md = stat.executeQuery().getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++)
                    cols.add(new Column(
                            md.getColumnTypeName(i), md.getColumnName(i), md.getColumnDisplaySize(i), md.getPrecision(i), md.getScale(i),
                            md.isAutoIncrement(i), md.isNullable(i) == ResultSetMetaData.columnNoNulls));
                stat.close();
            } finally {
                DBUtils.preparedStatementSilentClose(stat);
            }
            conn.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            DBUtils.connectionSilentClose(conn);
        }

        return cols;
	}

    @Override
	public List<OneToManyRelationship> getDetectedOneToManyRelationship(final String dbName, final String tableName) {
        return new ArrayList<>();

		/*final int tableNameLength = tableName.length();
		
		final List<String> likes = new ArrayList<String>();
		likes.add(tableName);
		for (int i = 1; i < 4; i++) {
			if (tableNameLength > i)
				likes.add(tableName.substring(0, tableNameLength - i));
		}
		
		final List<String> tables = getTables(dbName);
		
		final List<OneToManyRelationship> relationships = new ArrayList<OneToManyRelationship>();
		
		Connection conn = null;
		try {
			conn = getConnection(dbName);
			for (String table: tables) {
				final PreparedStatement stat = conn.prepareStatement("SELECT * FROM " + table);
                try {
                    final ResultSetMetaData md = stat.executeQuery().getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        final String sqlName = md.getColumnName(i);
                        if (sqlName.startsWith("id_")) {
                                final String fieldName = sqlName.substring(3);
                                for (String like: likes) {
                                    if (fieldName.startsWith(like)) {
                                        final String idSqlType = md.getColumnTypeName(i).split(" ")[0];
                                        String idJavaType = null;
                                        if (idSqlType.endsWith("INT")) {
                                            if (idSqlType.equals("BIGINT") || (idSqlType.equals("INT") && (md.getColumnTypeName(i).contains("UNSIGNED")))) {
                                                idJavaType = "long";
                                            } else {
                                                if (!(md.getColumnTypeName(i).equals("TINYINT UNSIGNED") && md.getPrecision(i) == 1))
                                                    idJavaType = "int";
                                            }
                                        }
                                        if (idJavaType != null)
                                            relationships.add(new OneToManyRelationship(suggestBeanClass(table), suggestJavaName(table), table, idJavaType, sqlName, true));
                                        break;
                                    }
                                }
                        }
                    }
                    stat.close();
                } finally {
                    DBUtils.preparedStatementSilentClose(stat);
                }
			}
            conn.close();
		} catch (final SQLException ex) {
            throw new RuntimeException(ex.getMessage());
		} finally {
            DBUtils.connectionSilentClose(conn);
        }
		
		return relationships;*/
	}
			
	
	private static final int DEFAULT_PORT = 3306;
	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	
	private static final List<String> OFF_LIMIT_DBS = Arrays.asList("information_schema", "mysql");
}

