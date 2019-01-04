package org.beanmaker;

public class OneToManyRelationship {

	private final String beanClass;
	private final String javaName;
	private final String table;
	private final String idJavaType;
	private final String idSqlName;
	private final boolean listOnly;
	
	public OneToManyRelationship(final String beanClass, final String javaName, final String table, final String idJavaType, final String idSqlName, final boolean listOnly) {
		this.beanClass = beanClass;
		this.javaName = javaName;
		this.table = table;
		this.idJavaType = idJavaType;
		this.idSqlName = idSqlName;
		this.listOnly = listOnly;
	}
	
	public String getBeanClass() {
		return beanClass;
	}
	
	public String getJavaName() {
		return javaName;
	}
	
	public String getTable() {
		return table;
	}
	
	public String getIdJavaType() {
		return idJavaType;
	}
	
	public String getIdSqlName() {
		return idSqlName;
	}
	
	public boolean isListOnly() { 
		return listOnly;
	}
	

}

