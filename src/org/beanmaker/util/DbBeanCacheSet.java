package org.beanmaker.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DbBeanCacheSet<T extends DbBeanInterface> {

    private final ConcurrentMap<String, DbBeanCache<T>> caches = new ConcurrentHashMap<String, DbBeanCache<T>>();

    public void register(final DbBeanCache<T> cache) {
        caches.put(cache.getCode(), cache);
    }

    public void deregister(final String code) {
        caches.remove(code);
    }

    public void deregister(final DbBeanCache<T> cache) {
        deregister(cache.getCode());
    }

    public boolean contains(final String code) {
        return caches.containsKey(code);
    }

    public boolean contains(final DbBeanCache<T> cache) {
        return contains(cache.getCode());
    }

    public DbBeanCache<T> getCache(final String code) {
        if (!contains(code))
            throw new IllegalArgumentException("No cache with code: " + code);

        return caches.get(code);
    }

    public void clear() {
        caches.clear();
    }

    public void submit(final T bean) {
        for (DbBeanCache<T> cache: caches.values())
            cache.submit(bean);
    }

    public void delete(final long id) {
        for (DbBeanCache<T> cache: caches.values())
            cache.remove(id);
    }
}
