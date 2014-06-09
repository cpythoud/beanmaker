package org.beanmaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Columns {
	
	public Columns(final DatabaseServer server, final String db, final String table) {
		this.server = server;
		this.db = db;
		this.table = table;
		columns = server.getColumns(db, table);
		oneToManyRelationships = server.getDetectedOneToManyRelationship(db, table);
	}
	
	public Columns(final Columns cols) {
		server = cols.server;
		db = cols.db;
		table = cols.table;
		columns = cols.getList();
	}
	
	public List<Column> getList() {
		List<Column> copy = new ArrayList<Column>();
		
		for (Column column: columns)
			copy.add(new Column(column));
		
		return copy;
	}
	
	public Column getColumn(final int index) {
		if (index < 1 || index > columns.size())
			throw new IndexOutOfBoundsException("There is no column number " + index);
		
		return new Column(columns.get(index - 1));
	}
	
	public void setJavaName(final int index, final String name) {
		if (index < 1 || index > columns.size())
			throw new IndexOutOfBoundsException("There is no column number " + index);
		
		columns.get(index - 1).setJavaName(name);
	}
	
	public void setJavaType(final int index, final String type) {
		if (index < 1 || index > columns.size())
			throw new IndexOutOfBoundsException("There is no column number " + index);
		
		columns.get(index - 1).setJavaType(type);
	}
	
	public void setRequired(final int index, final boolean required) {
		if (index < 1 || index > columns.size())
			throw new IndexOutOfBoundsException("There is no column number " + index);
		
		columns.get(index - 1).setRequired(required);
	}
	
	public void resetRequired() {
		for (Column column: columns)
			if (!column.isSpecial())
				column.setRequired(false);
	}

    public void setUnique(final int index, final boolean unique) {
        if (index < 1 || index > columns.size())
            throw new IndexOutOfBoundsException("There is no column number " + index);

        columns.get(index - 1).setUnique(unique);
    }

    public void resetUnique() {
        for (Column column: columns)
            if (!column.isSpecial())
                column.setUnique(false);
    }
	
	public void setAssociatedBeanClass(final int index, final String associatedBeanClass) {
		if (index < 1 || index > columns.size())
			throw new IndexOutOfBoundsException("There is no column number " + index);
		
		if (columns.get(index - 1).couldHaveAssociatedBean())
			columns.get(index - 1).setAssociatedBeanClass(associatedBeanClass);
		else
			throw new IllegalArgumentException("Column #" + index + " cannot have an associated bean.");
	}

    public void setItemOrderAssociatedField(final int index, final String itemOrderAssociatedField) {
        if (index < 1 || index > columns.size())
            throw new IndexOutOfBoundsException("There is no column number " + index);

        if (columns.get(index - 1).isItemOrder())
            columns.get(index - 1).setItemOrderAssociatedField(itemOrderAssociatedField);
        else
            throw new IllegalArgumentException("Column #" + index + " is not an item order field.");
    }
	
	public boolean hasBadField() {
		for (Column column: columns) {
			if (column.isBad())
				return true;
		}
		
		return false;
	}
	
	public boolean hasId() {
		for (Column column: columns) {
			if (column.isId())
				return true;
		}
		
		return false;
	}
	
	public boolean hasLastUpdate() {
		for (Column column: columns) {
			if (column.isLastUpdate())
				return true;
		}
		
		return false;
	}
	
	public boolean hasModifiedBy() {
		for (Column column: columns) {
			if (column.isModifiedBy())
				return true;
		}
		
		return false;
	}
	
	public boolean hasItemOrder() {
		for (Column column: columns) {
			if (column.isItemOrder())
				return true;
		}
		
		return false;
	}
	
	public boolean hasDuplicateSpecial() {
		int idCount = 0;
		int lastUpdateCount = 0;
		int modifiedByCount = 0;
		int itemOrderCount = 0;
		
		for (Column column: columns) {
			if (column.isId())
				idCount++;
			if (column.isLastUpdate())
				lastUpdateCount++;
			if (column.isModifiedBy())
				modifiedByCount++;
			if (column.isItemOrder())
				itemOrderCount++;
		}

        return idCount > 1 || lastUpdateCount > 1 || modifiedByCount > 1 || itemOrderCount > 1;
	}
	
	public boolean isOK() {
        return hasId() && !hasBadField() && !hasDuplicateSpecial();
    }
	
	public Set<String> getJavaTypes() {
		Set<String> types = new HashSet<String>();
		
		for (Column column: columns)
			types.add(column.getJavaType());
		
		return types;
	}

    public boolean containNumericalData() {
        for (Column column: columns)
            if (column.getJavaType().equals("int") || column.getJavaType().equals("long"))
                if (!column.isSpecial() && !column.getJavaName().startsWith("id"))
                    return true;

        return false;
    }
	
	public Set<String> getSqlTypes() {
		Set<String> types = new HashSet<String>();
		
		for (Column column: columns)
			types.add(column.getSqlTypeName());
		
		return types;
	}
	
	public List<String> getJavaFieldNames() {
		List<String> names = new ArrayList<String>();
		
		for (Column column: columns)
			names.add(column.getJavaName());
		
		return names;
	}
	
	public void addOneToManyRelationship(final OneToManyRelationship rel) {
		if (getJavaFieldNames().contains(rel.getJavaName()))
			throw new IllegalArgumentException("The bean already contains a field named " + rel.getJavaName());
		if (!server.getTables(db).contains(rel.getTable()))
			throw new IllegalArgumentException("Database " + db + " doesn't contain a table named " + rel.getTable());
		
		oneToManyRelationships.add(rel);
	}
	
	public void changeOneToManyRelationship(final int index, final OneToManyRelationship rel) {
		if (index < 0 || index > oneToManyRelationships.size())
			throw new IndexOutOfBoundsException("Bounds : 0-" + oneToManyRelationships.size() + ", index : " + index);
		if (getJavaFieldNames().contains(rel.getJavaName()))
			throw new IllegalArgumentException("The bean already contains a field named " + rel.getJavaName());
		if (!server.getTables(db).contains(rel.getTable()))
			throw new IllegalArgumentException("Database " + db + " doesn't contain a table named " + rel.getTable());
		
		oneToManyRelationships.set(index, rel);
	}
	
	public void removeOneToManyRelationship(final int index) {
		if (index < 0 || index > oneToManyRelationships.size())
			throw new IndexOutOfBoundsException("Bounds : 0-" + oneToManyRelationships.size() + ", index : " + index);
		
		oneToManyRelationships.remove(index);
	}
	
	public void clearOneToManyRelationships() {
		oneToManyRelationships.clear();
	}
	
	public List<OneToManyRelationship> getOneToManyRelationships() {
		return Collections.unmodifiableList(oneToManyRelationships);
	}
	
	public boolean hasOneToManyRelationships() {
		return oneToManyRelationships.size() > 0;
	}
	
	public String getOrderByField() {
		for (String candidate: ORDER_BY_CANDIDATE_FIELDS)
			for (Column col: columns)
				if (col.getSqlName().toLowerCase().equals(candidate.toLowerCase()))
					return candidate;
		
		return "id";
	}

    public Column getItemOrderField() {
        for (Column column: columns)
            if (column.isItemOrder())
                return new Column(column);

        throw new IllegalArgumentException("Column set does not contain an item order field.");
    }
	
	private DatabaseServer server;
	private String db;
	private String table;
	
	private List<Column> columns;
	private List<OneToManyRelationship> oneToManyRelationships;
	
	private static final List<String> ORDER_BY_CANDIDATE_FIELDS = Arrays.asList("name", "description");
}

