package org.beanmaker.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class DbBeanCache<T extends DbBeanInterface> {

    protected final ConcurrentMap<Long, T> cache = new ConcurrentHashMap<Long, T>();

    private final String code;

    public DbBeanCache(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean shouldBeCached(final T bean) {
        return true;
    }

    public boolean submit(final T bean) {
        if (shouldBeCached(bean)) {
            cache.put(bean.getId(), bean);
            return true;
        }

        return remove(bean.getId());
    }

    public T get(final long id) {
        return cache.get(id);
    }

    public boolean remove(final long id) {
        return cache.remove(id) != null;
    }

    public long size() {
        return cache.size();
    }
}
