package org.beanmaker.util;

public abstract class CacheSortableKeyBase<T extends Comparable<T>> implements Comparable<CacheSortableKeyBase<T>> {

    private final long id;
    private final T sortOrder;

    public CacheSortableKeyBase(final DbBeanInterface bean, final T sortOrder) {
        this.id = bean.getId();
        this.sortOrder = sortOrder;
    }

    public long getId() {
        return id;
    }

    public T getSortOrder() {
        return sortOrder;
    }

    @Override
    public int compareTo(CacheSortableKeyBase<T> key) {
        return sortOrder.compareTo(key.sortOrder);
    }

    // MUST IMPLEMENT EQUALS() HERE
    @Override
    public boolean equals(final Object key) {
        throw new UnsupportedOperationException("Must implement equals in derived class");
    }

    // IMPLEMENTATION MODEL :
    /*public boolean equals(final Object key) {
        if (key == this)
            return true;

        if (key instanceof <your key class>)
            return ((<your key class>) key).getId() == getId();

        return false;
    }*/

    @Override
    public int hashCode() {
        int result = 17;

        final int idEvaluationHash = (int) (id ^ (id >>> 32));
        result = 31 * result + idEvaluationHash;

        return result;
    }
}
