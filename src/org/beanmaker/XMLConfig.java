package org.beanmaker;

import org.jcodegen.html.xmlbase.XMLElement;

import java.util.List;
import java.util.Set;

public class XMLConfig {

    private static final String XML_CONFIG_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

    private final String beanName;
    private final String packageName;
    private final Columns columns;

    public XMLConfig(final String beanName, final String packageName, final Columns columns) {
        this.beanName = beanName;
        this.packageName = packageName;
        this.columns = columns;
    }

    public XMLElement getFullConfigXMLElement() {
        final XMLElement rootElement = createRootElement();
        addSubElementsTo(rootElement, false, true);
        return rootElement;
    }

    public XMLElement getPartialConfigXMLElement() {
        return getPartialConfigXMLElement(null);
    }

    public XMLElement getPartialConfigXMLElement(final String defaultPackage) {
        final XMLElement rootElement = createRootElement();
        addSubElementsTo(rootElement, true, defaultPackage == null || !defaultPackage.equals(packageName));
        return rootElement;
    }

    private XMLElement createRootElement() {
        XMLElement root = new XMLElement("bean");
        root.addChild(getOneLiner("database-table", columns.getTable()));
        return root;
    }

    private XMLElement getOneLiner(final String name, final String value) {
        final XMLElement element = new XMLElement(name, value);
        element.setOnOneLine(true);
        return element;
    }

    private XMLElement getOneLiner(final String name, final boolean value) {
        final XMLElement element = new XMLElement(name, value ? "true" : "false");
        element.setOnOneLine(true);
        return element;
    }

    private void addSubElementsTo(
            final XMLElement rootElement,
            final boolean showNonDefaultOnly,
            final boolean showPackage)
    {
        if (showPackage)
            rootElement.addChild(getOneLiner("package", packageName));

        addPotentialElement(rootElement, "name", beanName, columns.getSuggestedBeanName(), showNonDefaultOnly);

        for (Column column: columns.getList()) {
            if (!column.isSpecial()) {
                final XMLElement field = new XMLElement("field");

                field.addChild(getOneLiner("sql-name", column.getSqlName()));
                boolean hasNonDefaultData =
                        addPotentialElement(field, "java-type", column.getJavaType(), column.getSuggestedType(), showNonDefaultOnly);
                hasNonDefaultData =
                        addPotentialElement(field, "java-name", column.getJavaName(), column.getSuggestedName(), showNonDefaultOnly)
                        || hasNonDefaultData;
                hasNonDefaultData =
                        addPotentialElement(field, "required", column.isRequired(), column.shouldBeRequired(), showNonDefaultOnly)
                        || hasNonDefaultData;
                hasNonDefaultData =
                        addPotentialElement(field, "unique", column.isUnique(), false, showNonDefaultOnly)
                        || hasNonDefaultData;
                if (column.hasAssociatedBean())
                    hasNonDefaultData =
                            addPotentialElement(field, "bean-class", column.getAssociatedBeanClass(),
                                    column.getSuggestedAssociatedBeanClass(), showNonDefaultOnly)
                            || hasNonDefaultData;

                if (showNonDefaultOnly && !hasNonDefaultData)
                    continue;
                rootElement.addChild(field);
            } else if (column.getSqlName().equals("item_order")) {
                if (showNonDefaultOnly && column.isUnique())
                    continue;

                final XMLElement itemOrder = new XMLElement("item-order");
                itemOrder.addChild(getOneLiner("unique", column.isUnique()));
                if (!column.isUnique())
                    itemOrder.addChild(getOneLiner("associated-field", column.getItemOrderAssociatedField()));
                rootElement.addChild(itemOrder);
            }
        }

        final Set<String> oneToManyRelationshipTablesNames = columns.getOneToManyRelationshipTableNames();
        final Set<String> detectedOneToManyRelationshipTablesNames = columns.getDetectedOneToManyRelationshipTableNames();
        final List<OneToManyRelationship> detectedOneToManyRelationships = columns.getDetectedOneToManyRelationships();
        for (OneToManyRelationship oneToManyRelationship: columns.getOneToManyRelationships()) {
            final XMLElement relationshipElement = new XMLElement("relationship");
            relationshipElement.addChild(getOneLiner("database-table", oneToManyRelationship.getTable()));

            if (detectedOneToManyRelationshipTablesNames.contains(oneToManyRelationship.getTable())) {
                final OneToManyRelationship detectedOneToManyRelationship =
                        getRelationship(detectedOneToManyRelationships, oneToManyRelationship);
                boolean hasNonDefaultData =
                        addPotentialElement(relationshipElement, "java-type", oneToManyRelationship.getBeanClass(),
                                detectedOneToManyRelationship.getBeanClass(), showNonDefaultOnly);
                hasNonDefaultData =
                        addPotentialElement(relationshipElement, "java-name", oneToManyRelationship.getJavaName(),
                                detectedOneToManyRelationship.getJavaName(), showNonDefaultOnly) || hasNonDefaultData;
                hasNonDefaultData =
                        addPotentialElement(relationshipElement, "id-field", oneToManyRelationship.getIdSqlName(),
                                detectedOneToManyRelationship.getJavaName(), showNonDefaultOnly) || hasNonDefaultData;
                hasNonDefaultData =
                        addPotentialElement(relationshipElement, "list-only", oneToManyRelationship.isListOnly(),
                                detectedOneToManyRelationship.isListOnly(), showNonDefaultOnly) || hasNonDefaultData;
                if (showNonDefaultOnly && !hasNonDefaultData)
                    continue;

            } else {
                relationshipElement.addChild(getOneLiner("java-type", oneToManyRelationship.getBeanClass()));
                relationshipElement.addChild(getOneLiner("java-name", oneToManyRelationship.getJavaName()));
                relationshipElement.addChild(getOneLiner("id-field", oneToManyRelationship.getIdSqlName()));
                relationshipElement.addChild(getOneLiner("list-only", oneToManyRelationship.isListOnly()));
            }

            rootElement.addChild(relationshipElement);
        }
        for (OneToManyRelationship detectedOneToManyRelationship: detectedOneToManyRelationships)
            if (!oneToManyRelationshipTablesNames.contains(detectedOneToManyRelationship.getTable())) {
                final XMLElement relationshipElement = new XMLElement("relationship");
                relationshipElement.addChild(getOneLiner("database-table", detectedOneToManyRelationship.getTable()));
                relationshipElement.addChild(new XMLElement("deleted", true));
                rootElement.addChild(relationshipElement);
            }

        for (ExtraField extraField: columns.getExtraFields()) {
            final XMLElement extraFieldElement = new XMLElement("extra-field");
            extraFieldElement.addChild(getOneLiner("java-type", extraField.getType()));
            extraFieldElement.addChild(getOneLiner("java-name", extraField.getName()));
            extraFieldElement.addChild(getOneLiner("initialization-code", extraField.getInitializationExpression()));
            extraFieldElement.addChild(getOneLiner("final", extraField.isFinal()));
            if (extraField.requiresAnyImport()) {
                final XMLElement importElements = new XMLElement("imports");
                if (extraField.requiresImport())
                    importElements.addChild(getOneLiner("import", extraField.getRequiredImport()));
                if (extraField.requiresSecondaryImport())
                    importElements.addChild(getOneLiner("import", extraField.getSecondaryRequiredImport()));
                if (extraField.requiresTernaryImport())
                    importElements.addChild(getOneLiner("import", extraField.getTernaryRequiredImport()));
                extraFieldElement.addChild(importElements);
            }
            rootElement.addChild(extraFieldElement);
        }
    }

    private boolean addPotentialElement(
            final XMLElement rootElement,
            final String element,
            final String value,
            final String defaultValue,
            final boolean includeOnlyNonDefautParameters)
    {
        if (includeOnlyNonDefautParameters && value.equals(defaultValue))
            return false;

        rootElement.addChild(getOneLiner(element, value));
        return true;
    }

    private boolean addPotentialElement(
            final XMLElement rootElement,
            final String element,
            final boolean value,
            final boolean defaultValue,
            final boolean includeOnlyNonDefautParameters)
    {
        if (includeOnlyNonDefautParameters && value == defaultValue)
            return false;

        rootElement.addChild(getOneLiner(element, value));
        return true;
    }

    private OneToManyRelationship getRelationship(
            final List<OneToManyRelationship> detectedOneToManyRelationships,
            final OneToManyRelationship oneToManyRelationship)
    {
        for (OneToManyRelationship detectedOneToManyRelationship: detectedOneToManyRelationships)
            if (detectedOneToManyRelationship.getTable().equals(oneToManyRelationship.getTable()))
                return detectedOneToManyRelationship;

        throw new IllegalStateException("Cannot find detected relationship for table: " + oneToManyRelationship.getTable());
    }

    public String getFilename() {
        return beanName + ".xml";
    }

    public String getSourceCode() {
        return XML_CONFIG_PREFIX + getFullConfigXMLElement().toString();
    }

}
