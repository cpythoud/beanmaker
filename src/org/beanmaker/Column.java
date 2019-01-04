package org.beanmaker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbbeans.util.Strings;

public class Column {

    public static final List<String> JAVA_TYPES = Arrays.asList("boolean", "int", "long", "String", "Date", "Time", "Timestamp", "Money");

    private final String sqlTypeName;
    private final String sqlName;
    private final int displaySize;
    private final int precision;
    private final int scale;
    private final boolean autoincrement;

    private String javaType;
    private String javaName;

    private boolean required;
    private final boolean shouldBeRequired;
    private boolean unique = false;

    private String associatedBeanClass;
    private String itemOrderAssociatedField;

    private final boolean id;
    private final boolean lastUpdate;
    private final boolean modifiedBy;
    private final boolean itemOrder;

    private final boolean special;
    private final boolean bad;

    private static final List<String> SPECIAL_CASES = Arrays.asList("id", "last_update", "modified_by", "item_order");
    private static final Map<String, List<String>> SPECIAL_CASE_TYPES;

    static {
        SPECIAL_CASE_TYPES = new HashMap<String, List<String>>();
        SPECIAL_CASE_TYPES.put("id", Arrays.asList("TINYINT UNSIGNED", "SMALLINT UNSIGNED", "MEDIUMINT UNSIGNED", "INT UNSIGNED"));
        SPECIAL_CASE_TYPES.put("last_update", Arrays.asList("BIGINT UNSIGNED"));
        SPECIAL_CASE_TYPES.put("modified_by", Arrays.asList("CHAR", "VARCHAR"));
        SPECIAL_CASE_TYPES.put("item_order", Arrays.asList("TINYINT UNSIGNED", "SMALLINT UNSIGNED", "MEDIUMINT UNSIGNED", "INT UNSIGNED"));
    }

	
	public Column(final String sqlTypeName, final String sqlName, final int displaySize, final int precision, final int scale, final boolean autoincrement, final boolean required) {
		this.sqlTypeName = sqlTypeName;
		this.sqlName = sqlName;
		this.displaySize = displaySize;
		this.precision = precision;
		this.scale = scale;
		this.autoincrement = autoincrement;
        this.required = required;
        shouldBeRequired = required;
		
		if (SPECIAL_CASES.contains(sqlName)) {
			special = true;
            bad = !SPECIAL_CASE_TYPES.get(sqlName).contains(sqlTypeName);
			if (sqlName.equals("id")) {
				id = true;
                unique = true;
            } else {
				id = false;
            }
            lastUpdate = sqlName.equals("last_update");
            modifiedBy = sqlName.equals("modified_by");
			if (sqlName.equals("item_order")) {
				itemOrder = true;
                unique = true;  // ???
            } else {
				itemOrder = false;
            }
		} else {
			special = false;
			bad = false;
			id = false;
			lastUpdate = false;
			modifiedBy = false;
			itemOrder = false;
		}
		
		suggestType();
		suggestName();
		suggestAssociatedBeanClass();
	}
	
	public Column(final Column col) {
		this.sqlTypeName = col.sqlTypeName;
		this.sqlName = col.sqlName;
		this.displaySize = col.displaySize;
		this.precision = col.precision;
		this.scale = col.scale;
		this.autoincrement = col.autoincrement;
		this.special = col.special;
		this.bad = col.bad;
		this.id = col.id;
		this.lastUpdate = col.lastUpdate;
		this.modifiedBy = col.modifiedBy;
		this.itemOrder = col.itemOrder;
		this.javaType = col.javaType;
		this.javaName = col.javaName;
		this.required = col.required;
		this.shouldBeRequired = col.shouldBeRequired;
        this.unique = col.unique;
		this.associatedBeanClass = col.associatedBeanClass;
        this.itemOrderAssociatedField = col.itemOrderAssociatedField;
	}
	
	public String getSqlTypeName() {
		return sqlTypeName;
	}
	
	public String getSqlName() {
		return sqlName;
	}
	
	public int getDisplaySize() {
		return displaySize;
	}
	
	public int getPrecision() {
		return precision;
	}
	
	public int getScale() {
		return scale;
	}
	
	public boolean isAutoIncrement() {
		return autoincrement;
	}
	
	public boolean isSigned() {
		return !sqlTypeName.contains("UNSIGNED");
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public String getJavaName() {
		return javaName;
	}
	
	public boolean isRequired() {
		return required;
	}

    public boolean isUnique() {
        return unique;
    }
	
	public void setJavaType(final String javaType) {
		if (!JAVA_TYPES.contains(javaType))
			throw new IllegalArgumentException("Le type Java " + javaType + " n'est pas pris en charge pas BeanMaker !");
		
		this.javaType = javaType;
	}
	
	public void setJavaName(final String javaName) {
		if (Strings.isEmpty(javaName))
			throw new IllegalArgumentException("Le nom Java ne peut pas être vide.");
		
		this.javaName = javaName;
	}
	
	public void setRequired(final boolean required) {
        if (special && !required)
            throw new IllegalArgumentException("Un champs spécial est toujours requis.");

		this.required = required;
	}

    public void setUnique(final boolean unique) {
        if (special) {
            if (id && !unique)
                throw new IllegalArgumentException("Le champ ID est toujours unique.");
            if (lastUpdate && unique)
                throw new IllegalArgumentException("Un champ last_update ne peut pas être spécifié unique.");
            if (modifiedBy && unique)
                throw new IllegalArgumentException("Un champ modified_by ne peut pas être spécifié unique.");
        }

        this.unique = unique;
    }
	
	public boolean isId() {
		return id;
	}
	
	public boolean isLastUpdate() {
		return lastUpdate;
	}
	
	public boolean isModifiedBy() {
		return modifiedBy;
	}
	
	public boolean isItemOrder() {
		return itemOrder;
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public boolean isBad() {
		return bad;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append(sqlTypeName);
		buf.append(" ");
		buf.append(sqlName);
		buf.append(" (");
		buf.append(displaySize);
		buf.append(" / ");
		buf.append(precision);
		buf.append(",");
		buf.append(scale);
		if (autoincrement)
			buf.append(" autoincrement");
		buf.append(") = ");
		buf.append(javaType);
		buf.append(" ");
		buf.append(javaName);
		if (required)
			buf.append(" required");
        if (unique)
            buf.append(" unique");
		
		return buf.toString();
	}
	
	private void suggestType() {
    	javaType = getSuggestedType();
	}

	public String getSuggestedType() {
		if (id || itemOrder || sqlName.startsWith("id_"))
			return "long";

		return getSuggestedType(sqlTypeName, precision);
	}

    public static String getSuggestedType(final String sqlTypeName, final int precision) {
        final String type = sqlTypeName.split(" ")[0];

        if (type.endsWith("INT")) {
            if (type.equals("BIGINT") || (type.equals("INT") && (sqlTypeName.contains("UNSIGNED"))))
                return "long";
            if (sqlTypeName.equals("TINYINT UNSIGNED") && precision == 1)
                return "boolean";
            return "int";
        }

        if (type.equals("DATE"))
            return "Date";

        if (type.equals("TIME"))
            return "Time";

        if (type.equals("DATETIME") || type.equals("TIMESTAMP"))
            return "Timestamp";

        return "String";
    }

    public String getSuggestedName() {
    	return Strings.uncapitalize(Strings.camelize(sqlName));
	}
	
	private void suggestName() {
		javaName = getSuggestedName();
	}
	
	public boolean couldHaveAssociatedBean() {
		return !special && sqlName.startsWith("id_");
	}

	public boolean couldBeLabelReference() {
    	return couldHaveAssociatedBean() && sqlName.endsWith("_label");
	}

	public boolean couldBeFileReference() {
		return couldHaveAssociatedBean() && sqlName.endsWith("_file");
	}

	private static final String DEFAULT_LABEL_CLASS = "DbBeanLabel";

	private static final String DEFAULT_FILE_CLASS  = "DbBeanFile";

	public boolean isLabelReference() {
		return associatedBeanClass != null && associatedBeanClass.equals(DEFAULT_LABEL_CLASS);
	}

	public boolean isFileReference() {
		return associatedBeanClass != null && associatedBeanClass.equals(DEFAULT_FILE_CLASS);
	}

	public String getSuggestedAssociatedBeanClass() {
		if (!couldHaveAssociatedBean())
			return "";

		if (couldBeLabelReference())
			return DEFAULT_LABEL_CLASS;

		if (couldBeFileReference())
			return DEFAULT_FILE_CLASS;

		return Strings.camelize(sqlName.substring(3));
	}

	private void suggestAssociatedBeanClass() {
		if (!couldHaveAssociatedBean())
			return;

		associatedBeanClass = getSuggestedAssociatedBeanClass();
	}
	
	public boolean hasAssociatedBean() {
		return !Strings.isEmpty(associatedBeanClass);
	}
	
	public String getAssociatedBeanClass() {
		if (Strings.isEmpty(associatedBeanClass))
			return "";
		
		return associatedBeanClass;
	}
	
	public void setAssociatedBeanClass(final String associatedBeanClass) {
		if (!couldHaveAssociatedBean())
			throw new IllegalArgumentException("AssociatedBean " + associatedBeanClass + " not allowed for column " + sqlName);
		
		this.associatedBeanClass = associatedBeanClass;
	}

    public String getItemOrderAssociatedField() {
        if (Strings.isEmpty(itemOrderAssociatedField))
            return "";

        return itemOrderAssociatedField;
    }

    public void setItemOrderAssociatedField(final String itemOrderAssociatedField) {
        if (!isItemOrder())
            throw new IllegalArgumentException("This column is not an item order field.");

        if (Strings.isEmpty(itemOrderAssociatedField)) {
            this.itemOrderAssociatedField = "";
            unique = true;
        } else {
            this.itemOrderAssociatedField = itemOrderAssociatedField;
            unique = false;
        }
    }

    public boolean shouldBeRequired() {
		return shouldBeRequired;
	}
}

