package org.beanmaker;

import org.dbbeans.util.Strings;

public abstract class BeanCode extends BaseCode {
    protected final String beanName;
    protected final String packageName;
    protected final String beanVarName;
    protected final String bundleName;

    public BeanCode(final String beanName, final String packageName) {
        super(beanName,  packageName);
        this.beanName = beanName;
        this.packageName = packageName;
        beanVarName = getBeanVarName(beanName);
        bundleName = getBundleName(beanName,  packageName);
    }

    public BeanCode(final String beanName, final String packageName, final String nameExtension) {
        super(beanName + nameExtension,  packageName);
        this.beanName = beanName;
        this.packageName = packageName;
        beanVarName = getBeanVarName(beanName);
        bundleName = getBundleName(beanName,  packageName);
    }

    private static String getBeanVarName(final String beanName) {
        return Strings.uncapitalize(beanName);
    }

    private static String getBundleName(final String beanName, final String packageName) {
        return Strings.replace(packageName, ".", "-") + "-" + beanName;
    }
}
