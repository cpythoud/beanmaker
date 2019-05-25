package org.beanmaker.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DbBeanRequiredLanguages {

    private final Set<Long> languageIds;

    public DbBeanRequiredLanguages() {
        languageIds = new HashSet<Long>();
    }

    public DbBeanRequiredLanguages(final DbBeanLanguage... languages) {
        this(languages.length == 0 ? Collections.<DbBeanLanguage>emptyList() : Arrays.asList(languages));
    }

    public DbBeanRequiredLanguages(final Collection<DbBeanLanguage> languages) {
        languageIds = new HashSet<Long>(Ids.getIdSet(languages));
    }

    public DbBeanRequiredLanguages(final DbBeanRequiredLanguages requiredLanguages) {
        languageIds = new HashSet<Long>(requiredLanguages.languageIds);
    }

    public boolean isRequired(final DbBeanLanguage dbBeanLanguage) {
        return languageIds.contains(dbBeanLanguage.getId());
    }

    public void setStatus(final DbBeanLanguage dbBeanLanguage, final boolean required) {
        if (required)
            languageIds.add(dbBeanLanguage.getId());
        else
            languageIds.remove(dbBeanLanguage.getId());
    }
}
