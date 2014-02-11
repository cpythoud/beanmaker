package org.beanmaker;

import org.jcodegen.java.Assignment;
import org.jcodegen.java.FunctionDeclaration;
import org.jcodegen.java.ObjectCreation;
import org.jcodegen.java.ReturnStatement;
import org.jcodegen.java.StaticBlock;
import org.jcodegen.java.VarDeclaration;
import org.jcodegen.java.Visibility;

import org.dbbeans.util.Strings;

public class DbBeanSourceFile extends BaseCode {

    private final String database;

	public DbBeanSourceFile(final String database, final String packageName) {
        super("DbBean", packageName);

		if (Strings.isEmpty(database))
			throw new IllegalArgumentException("database empty");
		
		this.database = database;

		createSourceCode();
	}
	
	private void addImports() {
        importsManager.addImport("org.dbbeans.sql.DB");
        importsManager.addImport("org.dbbeans.sql.DBAccess");
        importsManager.addImport("org.dbbeans.sql.DBFromDataSource");
        importsManager.addImport("org.dbbeans.sql.DBTransaction");
	}
	
    private void addProperties() {
        javaClass.addContent(
                new VarDeclaration("DB", "db").markAsStatic().markAsFinal().visibility(Visibility.PROTECTED)
        ).addContent(
                new VarDeclaration("DBAccess", "dbAccess").markAsStatic().markAsFinal().visibility(Visibility.PROTECTED)
        ).addContent(EMPTY_LINE).addContent(
                new VarDeclaration("String", "DATA_SOURCE",
                        Strings.quickQuote("java:comp/env/jdbc/" + database)).markAsStatic().markAsFinal().visibility(Visibility.PRIVATE)
        ).addContent(EMPTY_LINE);
    }

    private void addStaticInitialization() {
        javaClass.addContent(
                new StaticBlock().addContent(
                        new Assignment("db", new ObjectCreation("DBFromDataSource").addArgument("DATA_SOURCE"))
                ).addContent(
                        new Assignment("dbAccess", new ObjectCreation("DBAccess").addArgument("db"))
                )
        ).addContent(EMPTY_LINE);
    }

    private void addTransactionGetter() {
        javaClass.addContent(
                new FunctionDeclaration("createDBTransaction", "DBTransaction").markAsStatic().addContent(
                        new ReturnStatement(new ObjectCreation("DBTransaction").addArgument("db"))
                )
        );
    }
	
	private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

		addImports();
        javaClass.markAsAbstract();
        addProperties();
        addStaticInitialization();
        addTransactionGetter();
	}
}

