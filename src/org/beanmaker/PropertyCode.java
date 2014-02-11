package org.beanmaker;

import org.dbbeans.util.Strings;

public abstract class PropertyCode {
    protected final String filename;
    protected final Columns columns;

    protected String sourceCode;

    public PropertyCode(final String beanName, final String packageName, final String extension, final Columns columns) {
        filename = Strings.replace(packageName, ".", "-") + "-" + beanName + extension + ".properties";
        this.columns = columns;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getFilename() {
        return filename;
    }

    protected void addComments(StringBuilder buf) {
        buf.append("# ");
        buf.append(filename);
        buf.append("\n");
        buf.append(SourceFiles.getCommentAndVersionForPropertyFile());
    }
}
