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

        javaClass.markAsFinal();

		createSourceCode();
	}
	
    private void addClassModifiers() {
        javaClass.extendsClass(beanName + "Base");
    }
	
	private void addConstructors() {
        javaClass.addContent(getBaseConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getIdArgumentConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getCopyConstructor()).addContent(EMPTY_LINE);
        addProtectedConstructors();
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
        final String modelVarName = beanVarName + "Model";

        return getBaseConstructor().addArgument(new FunctionArgument(beanName, modelVarName)).addContent(
                new FunctionCall("super").addArgument(modelVarName).byItself()
        );
    }

    private ConstructorDeclaration getCommonFieldConstructor() {
        final ConstructorDeclaration fieldConstructor = getBaseConstructor().visibility(Visibility.PROTECTED);
        for (Column column: columns.getList()) {
            final String javaType = column.getJavaType();
            if (javaType.equals("Date") || javaType.equals("Time") || javaType.equals("Timestamp"))
                importsManager.addImport("java.sql." + javaType);
            if (javaType.equals("Money"))
                importsManager.addImport("org.dbbeans.util.Money");
            fieldConstructor.addArgument(new FunctionArgument(javaType, column.getJavaName()));
        }

        return fieldConstructor;
    }

    private FunctionCall getCommonFieldConstructorSuperCall() {
        final FunctionCall superCall = new FunctionCall("super").byItself();
        for (Column column: columns.getList())
            superCall.addArgument(column.getJavaName());

        return superCall;
    }

    private void addProtectedConstructors() {
        javaClass.addContent(
                getCommonFieldConstructor()
                        .addContent(getCommonFieldConstructorSuperCall()))
                .addContent(EMPTY_LINE);

        final ConstructorDeclaration extraFieldConstructor = getCommonFieldConstructor();
        final FunctionCall extraFieldConstructorSuperCall = getCommonFieldConstructorSuperCall();
        boolean needExtraConstructors = false;
        for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
            if (!relationship.isListOnly()) {
                needExtraConstructors = true;
                final String beanClass = relationship.getBeanClass();
                final String javaName = relationship.getJavaName();
                extraFieldConstructor.addArgument(new FunctionArgument(new GenericType("List", beanClass).toString(), javaName));
                importsManager.addImport("java.util.List");
                extraFieldConstructorSuperCall.addArgument(javaName);
            }

        if (needExtraConstructors)
            javaClass.addContent(
                    extraFieldConstructor
                            .addContent(extraFieldConstructorSuperCall))
                    .addContent(EMPTY_LINE);


        importsManager.addImport("java.sql.SQLException");
        importsManager.addImport("java.sql.ResultSet");

        javaClass.addContent(
                getCommonResultSetConstructor()
                        .addContent(getCommonResultSetConstructorSuperCall()))
                .addContent(EMPTY_LINE);

        if (needExtraConstructors) {
            final ConstructorDeclaration extraRsConstructor = getCommonResultSetConstructor();
            final FunctionCall extraRsConstructorSuperCall = getCommonResultSetConstructorSuperCall();

            for (OneToManyRelationship relationship: columns.getOneToManyRelationships())
                if (!relationship.isListOnly()) {
                    extraRsConstructor.addArgument(
                            new FunctionArgument(
                                    new GenericType(
                                            "List",
                                            relationship.getBeanClass()).toString(),
                                    relationship.getJavaName()));
                    extraRsConstructorSuperCall.addArgument(relationship.getJavaName());
                }

            javaClass.addContent(
                    extraRsConstructor
                            .addContent(extraRsConstructorSuperCall))
                    .addContent(EMPTY_LINE);
        }
    }

    private ConstructorDeclaration getCommonResultSetConstructor() {
        return getBaseConstructor()
                .visibility(Visibility.PROTECTED)
                .addArgument(new FunctionArgument("ResultSet", "rs"))
                .addException("SQLException");
    }

    private FunctionCall getCommonResultSetConstructorSuperCall() {
        return new FunctionCall("super").byItself().addArgument("rs");
    }

    private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addClassModifiers();
		addConstructors();
	}
}

