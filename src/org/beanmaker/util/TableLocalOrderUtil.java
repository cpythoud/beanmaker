package org.beanmaker.util;

import org.dbbeans.sql.DBQueryRetrieveData;
import org.dbbeans.sql.DBQuerySetup;
import org.dbbeans.sql.DBTransaction;
import org.dbbeans.sql.DBUpdates;

import org.dbbeans.util.Sets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TableLocalOrderUtil {

    public static  <B extends DbBeanInterface> List<B> getBeansInOrder(
            final Collection<B> beans,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        final DBTransaction transaction = context.getDBTransaction();

        final Set<Long> currentlyOrderedIds = getOrderedIds(transaction, orderingTable, context.getId());
        final Set<Long> idToBeOrdered = Ids.getIdSet(beans, new LinkedHashSet<Long>());
        clearItemOrders(
                transaction,
                orderingTable,
                context.getId(),
                Sets.difference(currentlyOrderedIds, idToBeOrdered));
        addItemOrders(
                transaction,
                orderingTable,
                context.getId(),
                Sets.difference(idToBeOrdered, currentlyOrderedIds, new LinkedHashSet<Long>()));
        removeHolesInItemOrderList(transaction, orderingTable, context.getId());

        final Set<Long> orderedIds = getOrderedIds(transaction, orderingTable, context.getId());
        transaction.commit();

        return getSortedBeans(beans, orderedIds);
    }

    private static LinkedHashSet<Long> getOrderedIds(
            final DBTransaction transaction,
            final String orderingTable,
            final long idContext)
    {
        return transaction.addQuery(
                "SELECT id_bean FROM " + orderingTable + " WHERE id_context=? ORDER BY item_order",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                        stat.setLong(1, idContext);
                    }
                },
                new DBQueryRetrieveData<LinkedHashSet<Long>>() {
                    @Override
                    public LinkedHashSet<Long> processResultSet(final ResultSet rs) throws SQLException {
                        final LinkedHashSet<Long> ids = new LinkedHashSet<Long>();

                        while (rs.next())
                            ids.add(rs.getLong(1));

                        return ids;
                    }
                }
        );
    }

    private static void clearItemOrders(
            final DBTransaction transaction,
            final String orderingTable,
            final long idContext,
            final Set<Long> ids)
    {
        transaction.addUpdates(
                "DELETE FROM " + orderingTable + " WHERE id_context=? AND id_bean=?",
                new DBUpdates() {
                    @Override
                    public void execute(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, idContext);
                        for (long idBean : ids) {
                            stat.setLong(2, idBean);
                            stat.executeUpdate();
                        }
                    }
                }
        );
    }

    private static void addItemOrders(
            final DBTransaction transaction,
            final String orderingTable,
            final long idContext,
            final Set<Long> ids)
    {
        final long lastItemOrder = getLastItemOrder(transaction, orderingTable, idContext);

        transaction.addUpdates(
                "INSERT INTO " + orderingTable + " (id_context, id_bean, item_order) " +
                        "VALUES (?, ?, ?)",
                new DBUpdates() {
                    @Override
                    public void execute(final PreparedStatement stat) throws SQLException {
                        long itemOrder = lastItemOrder;
                        for (long idBean : ids) {
                            stat.setLong(1, idContext);
                            stat.setLong(2, idBean);
                            stat.setLong(3, ++itemOrder);
                            stat.executeUpdate();
                        }
                    }
                }
        );
    }

    private static long getLastItemOrder(
            final DBTransaction transaction,
            final String orderingTable,
            final long idContext)
    {
        return transaction.addQuery(
                "SELECT MAX(item_order) FROM " + orderingTable + " WHERE id_context=?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, idContext);
                    }
                },
                new DBQueryRetrieveData<Long>() {
                    @Override
                    public Long processResultSet(final ResultSet rs) throws SQLException {
                        if (rs.next())
                            return rs.getLong(1);

                        return 0L;
                    }
                }
        );
    }

    private static void removeHolesInItemOrderList(
            final DBTransaction transaction,
            final  String orderingTable,
            final long idContext)
    {
        final TreeMap<Long, Long> itemOrderMap = getOldNewItemOrderMap(transaction, orderingTable, idContext);

        transaction.addUpdates(
                "UPDATE " + orderingTable + " SET item_order=? " +
                        "WHERE id_context=? AND item_order=?",
                new DBUpdates() {
                    @Override
                    public void execute(final PreparedStatement stat) throws SQLException {
                        stat.setLong(2, idContext);
                        for (long oldItemOrder: itemOrderMap.keySet()) {
                            final long newItemOrder = itemOrderMap.get(oldItemOrder);
                            if (newItemOrder != oldItemOrder) {
                                stat.setLong(1, newItemOrder);
                                stat.setLong(3, oldItemOrder);
                                stat.executeUpdate();
                            }
                        }
                    }
                }
        );
    }

    private static TreeMap<Long, Long> getOldNewItemOrderMap(
            final DBTransaction transaction,
            final String orderingTable,
            final long idContext)
    {
        return transaction.addQuery(
                "SELECT item_order FROM " + orderingTable +
                        " WHERE id_context=? ORDER BY item_order",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, idContext);
                    }
                },
                new DBQueryRetrieveData<TreeMap<Long, Long>>() {
                    @Override
                    public TreeMap<Long, Long> processResultSet(final ResultSet rs) throws SQLException {
                        final TreeMap<Long, Long> itemOrderMap = new TreeMap<Long, Long>();

                        long index = 0;
                        while (rs.next())
                            itemOrderMap.put(rs.getLong(1), ++index);

                        return itemOrderMap;
                    }
                }
        );
    }


    private static  <B extends DbBeanInterface> List<B> getSortedBeans(
            final Collection<B> beans,
            final Set<Long> orderedIds)
    {
        final Map<Long, B> idMap = new HashMap<Long, B>();
        for (B bean: beans)
            idMap.put(bean.getId(), bean);

        final List<B> sortedBeans = new ArrayList<B>();
        for (long id: orderedIds)
            sortedBeans.add(idMap.get(id));

        return sortedBeans;
    }

    private static boolean isFirstItemOrder(
            final DBTransaction transaction,
            final long idBean,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        return transaction.addQuery(
                "SELECT id_bean FROM " + orderingTable +
                        " WHERE id_context=? AND id_bean=? AND item_order=1",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, context.getId());
                        stat.setLong(2, idBean);
                    }
                },
                new DBQueryRetrieveData<Boolean>() {
                    @Override
                    public Boolean processResultSet(final ResultSet rs) throws SQLException {
                        return rs.next();
                    }
                }
        );
    }


    public static void itemOrderMoveUp(
            final long itemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        final DBTransaction transaction = context.getDBTransaction();
        if (itemOrder < 1)
            throw new IllegalArgumentException("item order < 1");

        final long lastItemOrder = getLastItemOrder(transaction, orderingTable, context.getId());
        if (itemOrder >= lastItemOrder)
            throw new IndexOutOfBoundsException("item order >= max = " + lastItemOrder);

        doItemOrderMoveAfter(transaction, itemOrder, itemOrder + 1, context, orderingTable);

        transaction.commit();
    }

    public static void itemOrderMoveDown(
            final long itemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        final DBTransaction transaction = context.getDBTransaction();
        if (itemOrder < 2)
            throw new IllegalArgumentException("item order < 2");

        final long lastItemOrder = getLastItemOrder(transaction, orderingTable, context.getId());
        if (itemOrder > lastItemOrder)
            throw new IndexOutOfBoundsException("item order > max = " + lastItemOrder);

        doItemOrderMoveAfter(transaction, itemOrder, itemOrder - 2, context, orderingTable);

        transaction.commit();
    }

    public static void itemOrderMoveAfter(
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        final DBTransaction transaction = context.getDBTransaction();
        checkItemOrderValues(transaction, itemOrder, companionItemOrder, context, orderingTable);
        doItemOrderMoveAfter(transaction, itemOrder, companionItemOrder, context, orderingTable);
        transaction.commit();
    }

    private static void checkItemOrderValues(
            final DBTransaction transaction,
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        if (itemOrder < 1)
            throw new IllegalArgumentException("item order < 1");
        if (companionItemOrder < 1)
            throw new IllegalArgumentException("companion item order < 1");


        final long lastItemOrder = getLastItemOrder(transaction, orderingTable, context.getId());
        if (itemOrder > lastItemOrder)
            throw new IndexOutOfBoundsException("item order > max = " + lastItemOrder);
        if (companionItemOrder > lastItemOrder)
            throw new IndexOutOfBoundsException("companion item order > max = " + lastItemOrder);

        if (itemOrder == companionItemOrder)
            throw new IllegalArgumentException("item order = companion item order / no move to be performed");
    }

    private static void doItemOrderMoveAfter(
            final DBTransaction transaction,
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        if (itemOrder > companionItemOrder)
            itemOrderMoveAfterUp(transaction, itemOrder, companionItemOrder, context, orderingTable);
        else
            itemOrderMoveAfterDown(transaction, itemOrder, companionItemOrder, context, orderingTable);
    }

    private static void itemOrderMoveAfterUp(
            final DBTransaction transaction,
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        changeItemOrderValue(transaction, itemOrder, context, orderingTable, 0L);
        incrementItemOrderValues(transaction, companionItemOrder, itemOrder, context, orderingTable);
        changeItemOrderValue(transaction, 0L, context, orderingTable, companionItemOrder + 1);
    }

    private static void itemOrderMoveAfterDown(
            final DBTransaction transaction,
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        changeItemOrderValue(transaction, itemOrder, context, orderingTable, 0L);
        decrementItemOrderValues(transaction, itemOrder, companionItemOrder, context, orderingTable);
        changeItemOrderValue(transaction, 0L, context, orderingTable, companionItemOrder);
    }

    private static void changeItemOrderValue(
            final DBTransaction transaction,
            final long itemOrder,
            final TableLocalOrderContext context,
            final String orderingTable,
            final long value)
    {
        transaction.addUpdate(
                "UPDATE " + orderingTable + " SET item_order=? WHERE id_context=? AND item_order=?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, value);
                        stat.setLong(2, context.getId());
                        stat.setLong(3, itemOrder);
                    }
                }
        );
    }

    private static void incrementItemOrderValues(
            final DBTransaction transaction,
            final long lowBound,
            final long highBound,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        transaction.addUpdate(
                "UPDATE " + orderingTable + " SET item_order=item_order + 1 " +
                        "WHERE id_context=? AND item_order > ? AND item_order < ?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, context.getId());
                        stat.setLong(2, lowBound);
                        stat.setLong(3, highBound);
                    }
                }
        );
    }

    private static void decrementItemOrderValues(
            final DBTransaction transaction,
            final long lowBound,
            final long highBound,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        transaction.addUpdate(
                "UPDATE " + orderingTable + " SET item_order=item_order - 1 " +
                        "WHERE id_context=? AND item_order > ? AND item_order <= ?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, context.getId());
                        stat.setLong(2, lowBound);
                        stat.setLong(3, highBound);
                    }
                }
        );
    }

    public static void itemOrderMoveBefore(
            final long itemOrder,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        final DBTransaction transaction = context.getDBTransaction();
        checkItemOrderValues(transaction, itemOrder, companionItemOrder, context, orderingTable);

        if (companionItemOrder == 1)
            itemOrderMoveToFirstPlace(transaction, itemOrder, context, orderingTable);
        else
            doItemOrderMoveAfter(transaction, itemOrder, companionItemOrder - 1, context, orderingTable);

        transaction.commit();
    }

    private static void itemOrderMoveToFirstPlace(
            final DBTransaction transaction,
            final long itemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        changeItemOrderValue(transaction, itemOrder, context, orderingTable, 0L);
        incrementItemOrderValues(transaction, itemOrder, context, orderingTable);
    }

    private static void incrementItemOrderValues(
            final DBTransaction transaction,
            final long highBound,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        transaction.addUpdate(
                "UPDATE " + orderingTable + " SET item_order=item_order + 1 " +
                        "WHERE id_context=? AND item_order < ?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, context.getId());
                        stat.setLong(2, highBound);
                    }
                }
        );
    }
}
