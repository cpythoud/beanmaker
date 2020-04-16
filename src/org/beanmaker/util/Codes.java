package org.beanmaker.util;

public class Codes {

    public static void adjustCopyCode(DbBeanWithCode copy) {
        copy.setCode(copy.getCode() + "_copy");
        if (!copy.isCodeUnique()) {
            String baseCode = copy.getCode();
            int index = 2;
            do {
                copy.setCode(baseCode + index);
                ++index;
            } while (!copy.isCodeUnique());
        }
    }
}
