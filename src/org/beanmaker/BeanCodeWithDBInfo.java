package org.beanmaker;

import org.jcodegen.java.Condition;
import org.jcodegen.java.FunctionCall;
import org.jcodegen.java.IfBlock;

import org.dbbeans.util.Strings;

import java.util.Arrays;
import java.util.List;

public class BeanCodeWithDBInfo extends BeanCode {

    protected final Columns columns;
    protected final String tableName;

    protected static final List<String> JAVA_TEMPORAL_TYPES = Arrays.asList("Date", "Time", "Timestamp");

    public BeanCodeWithDBInfo(final String beanName, final String packageName, final String nameExtension, final Columns columns, final String tableName) {
        super(beanName, packageName, nameExtension);

        if (!columns.isOK())
            throw new IllegalArgumentException("columns not ok");

        if (Strings.isEmpty(tableName))
            throw new IllegalArgumentException("tableName empty");

        this.columns = columns;
        this.tableName = tableName;
    }

    protected IfBlock ifNotDataOK() {
        return ifNotDataOK(false);
    }

    protected IfBlock ifNotDataOK(final boolean fromBean) {
        final FunctionCall functionCall;
        if (fromBean)
            functionCall = new FunctionCall("isDataOK", beanVarName);
        else
            functionCall = new FunctionCall("isDataOK");

        return new IfBlock(new Condition(functionCall, true));
    }

    protected FunctionCall getFilenameFunctionCall(final String bean, final String field) {
        return new FunctionCall("getFilename", "LocalFiles")
                .addArgument(new FunctionCall("get" + Strings.capitalize(field), bean));
    }
}
