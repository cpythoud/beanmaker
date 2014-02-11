package org.beanmaker;

import java.util.List;

public interface DatabaseServer {
	
	public List<String> getAvailableDatabases();
	
	public List<String> getTables(final String dbName);
	
	public List<Column> getColumns(final String dbName, final String tableName);
	
	public List<OneToManyRelationship> getDetectedOneToManyRelationship(final String dbName, final String tableName);
}

