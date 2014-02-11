package org.beanmaker;

import org.jcodegen.java.ConstructorDeclaration;
import org.jcodegen.java.FunctionArgument;
import org.jcodegen.java.FunctionCall;

public class BeanSourceFile extends BeanCode {

	public BeanSourceFile(final String beanName, final String packageName) {
        super(beanName, packageName);

		createSourceCode();
	}
	
    private void addClassModifiers() {
        javaClass.extendsClass(beanName + "Base");
    }
	
	private void addConstructors() {
        javaClass.addContent(getBaseConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getIdArgumentConstructor()).addContent(EMPTY_LINE);
        javaClass.addContent(getCopyConstructor());
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
	
	private void createSourceCode() {
        sourceFile.setStartComment(SourceFiles.getCommentAndVersion());

        addClassModifiers();
		addConstructors();
	}
}

