package org.beanmaker;

import org.jcodegen.java.ConstructorDeclaration;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.GenericType;
import org.jcodegen.java.Visibility;

public class BeanSourceFile extends BeanCode {

    private final Columns columns;

	public BeanSourceFile(final String beanName, final String packageName, final Columns columns) {
        super(beanName, packageName);

        this.columns = columns;

		createSourceCode();
	}
	
    private void addClassModifiers() {
        javaClass.extendsClass(beanName + "Base");
    }
	
	private void addConstructors() {
        javaClass.addContent(getBaseConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getIdArgumentConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getCopyConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getFieldConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getResultSetConstructor()).addContent(EMPTY_LINE);
	}

    private ConstructorDeclaration getBaseConstructor() {
        return javaClass.createConstructor();
    }

    private ConstructorDeclaration getIdArgumentConstructor() {
        return getBaseConstructor().addArgument(new FunctionArgument("long", "id")).addContent(
                new FunctionCall("super").addArgument("id").byItself()
        );
    }

    private ConstructorDeclaration getCopyConstructor() {
        return getBaseConstructor().addArgument(new FunctionArgument(beanName, "model")).addContent(
                new FunctionCall("super").addArgument("model").byItself()
        );
    }

    private ConstructorDeclaration getFieldConstructor() {
        final ConstructorDeclaration fieldConstructor = getBaseConstructor().visibility(Visibility.PROTECTED);
        for (Column column: columns.getList()) {
            final String javaType = column.getJavaType();
            if (javaType.equals("Date") || javaType.equals("Time") || javaType.equals("Timestamp"))
                importsManager.addImport("java.sql." + javaType);
            if (javaType.equals("Money"))
                importsManager.addImport("org.dbbeans.util.Money");
            fieldConstructor.addArgument(new FunctionArgument(javaType, column.getJavaName()));
        }

        final FunctionCall superCall = new FunctionCall("super").byItself();
        for (Column column: columns.getList())
            superCall.addArgument(column.getJavaName());

        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly()) {
                final String beanClass = relationship.getBeanClass();
                final String javaName = relationship.getJavaName();
                fieldConstructor.addArgument(new FunctionArgument(new GenericType("List", beanClass).toString(), javaName));
                importsManager.addImport("java.util.List");
                superCall.addArgument(javaName);
            }

        return fieldConstructor.addContent(superCall);
    }

    private ConstructorDeclaration getResultSetConstructor() {
        importsManager.addImport("java.sql.SQLException");
        importsManager.addImport("java.sql.ResultSet");

        final ConstructorDeclaration rsConstructor = getBaseConstructor().visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("ResultSet", "rs")).addException("SQLException");
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                rsConstructor.addArgument(new FunctionArgument(new GenericType("List", relationship.getBeanClass()).toString(), relationship.getJavaName()));

        final FunctionCall superCall = new FunctionCall("super").byItself().addArgument("rs");
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly())
                superCall.addArgument(relationship.getJavaName());

        return rsConstructor.addContent(superCall);
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addClassModifiers();
		addConstructors();
	}
}

