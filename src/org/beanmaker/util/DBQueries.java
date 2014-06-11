package org.beanmaker.util;

import org.dbbeans.sql.DB;
import org.dbbeans.sql.DBAccess;
import org.dbbeans.sql.DBQueryProcess;
import org.dbbeans.sql.DBQueryRetrieveData;
import org.dbbeans.sql.DBQuerySetup;
import org.dbbeans.sql.DBQuerySetupRetrieveData;
import org.dbbeans.sql.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 */
public class DBQueries {

    public static int getIntCount(final DB db, final String table) {
        return new DBAccess(db).processQuery("SELECT COUNT(id) FROM " + table, new DBQueryRetrieveData<Integer>() {
            @Override
            public Integer processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    public static long getLongCount(final DB db, final String table) {
        return new DBAccess(db).processQuery("SELECT COUNT(id) FROM " + table, new DBQueryRetrieveData<Long>() {
            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }


    public static boolean isIdOK(final DB db, final String table, final long id) {
        return new DBAccess(db).processQuery("SELECT id FROM " + table + " WHERE id=?", new DBQuerySetupRetrieveData<Boolean>() {
            @Override
            public Boolean processResultSet(ResultSet rs) throws SQLException {
                return rs.next();
            }

            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, id);
            }
        });
    }


    public static String getHumanReadableTitle(final DB db, final String table, final long id, final List<String> fields) {
        class Query extends HumanReadableNameProcess {
            Query(final int fields) {
                super(fields);
            }

            @Override
            public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                stat.setLong(1, id);
            }
        }

        if (!isIdOK(db, table, id))
            throw new IllegalArgumentException("No such id (" + id + ") in database for table " + table + ".");
        if (fields.size() == 0)
            throw new IllegalArgumentException("Field list is empty. It must contain at least one valid field name.");

        final Query query = new Query(fields.size());
        return new DBAccess(db).processQuery(getHumanReadableTitleSQLQuery(table, fields), query);
    }

    private static abstract class HumanReadableNameProcess implements DBQuerySetupRetrieveData<String> {
        private final int fieldCount;

        HumanReadableNameProcess(final int fieldCount) {
            this.fieldCount = fieldCount;
        }

        @Override
        public String processResultSet(final ResultSet rs) throws SQLException {
            final StringBuilder name = new StringBuilder();
            rs.next();
            for (int i = 0; i < fieldCount; i++) {
                name.append(rs.getString(1 + i));
                if (i < fieldCount - 1)
                    name.append(" ");
            }
            return name.toString();
        }
    }

    private static String getHumanReadableTitleSQLQuery(final String table, final List<String> fields) {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        for (String field: fields) {
            query.append(field);
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" FROM ");
        query.append(table);
        query.append(" WHERE id=?");

        return query.toString();
    }


    public static List<IdNamePair> getIdNamePairs(final DB db, final String table, final String whereClause, final List<String> dataFields, final List<String> orderingFields) {
        /*if (!isTableNameProperlyQuoted(table))
            throw new IllegalArgumentException(QUOTED_TABLE_NAME_EXCEPTION_MESSAGE);*/

        final List<IdNamePair> pairs = new ArrayList<IdNamePair>();

        final StringBuilder query = new StringBuilder();
        query.append("SELECT id, ");
        for (String field: dataFields) {
            query.append(field);
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" FROM ");
        query.append(table);
        if (whereClause != null) {
            query.append(" WHERE ");
            query.append(whereClause);
        }
        query.append(" ORDER BY ");
        for (String field: orderingFields) {
            query.append(field);
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());

        new DBAccess(db).processQuery(query.toString(), new DBQueryProcess() {
            @Override
            public void processResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    final StringBuilder name = new StringBuilder();
                    for (int i = 0; i < dataFields.size(); i++) {
                        name.append(rs.getString(2 + i));
                        if (i < dataFields.size() - 1)
                            name.append(" ");
                    }
                    pairs.add(new IdNamePair(rs.getLong(1), name.toString()));
                }
            }
        });

        return pairs;
    }


    /*public static int getMaxItemOrderInt(final DB db, final String table, final String extraCondition) {
        return new DBAccess(db).processQuery(getMaxItemOrderQuery(table, extraCondition), new DBQueryRetrieveData<Integer>() {
            @Override
            public Integer processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    public static long getMaxItemOrderLong(final DB db, final String table, final String extraCondition) {
        return new DBAccess(db).processQuery(getMaxItemOrderQuery(table, extraCondition), new DBQueryRetrieveData<Long>() {
            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }*/

    public static long getMaxItemOrder(final DB db, final String query) {
        return new DBAccess(db).processQuery(query, new DBQueryRetrieveData<Long>() {
            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    public static long getMaxItemOrder(final DB db, final String query, final long... parameters) {
        return new DBAccess(db).processQuery(query, new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                setupParameters(stat, 1, toList(parameters));
            }

            @Override
            public Long processResultSet(final ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    public static long getMaxItemOrder(final DBTransaction transaction, final String query) {
        return transaction.addQuery(query, new DBQueryRetrieveData<Long>() {
            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    public static long getMaxItemOrder(final DBTransaction transaction, final String query, final long... parameters) {
        return transaction.addQuery(query, new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                setupParameters(stat, 1, toList(parameters));
            }

            @Override
            public Long processResultSet(final ResultSet rs) throws SQLException {
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    private static void setupParameters(final PreparedStatement stat, final int startIndex, final List<Long> parameters) throws SQLException {
        int index = startIndex - 1;
        for (long parameter: parameters)
            stat.setLong(++index, parameter);
    }



    /*private static String getMaxItemOrderQuery(final String table, final String extraCondition) {
        *//*if (!isTableNameProperlyQuoted(table))
            throw new IllegalArgumentException(QUOTED_TABLE_NAME_EXCEPTION_MESSAGE);*//*

        if (extraCondition == null)
            return "SELECT MAX(item_order) FROM " + table;

        return "SELECT MAX(item_order) FROM " + table + " WHERE " + extraCondition;
    }*/


    /*public static void itemOrderMoveUp(final DB db, final String table, final long id, final long itemOrder) {
        itemOrderMoveUp(db, table, id, itemOrder, null);
    }

    public static void itemOrderMoveUp(final DB db, final String table, final long id, final long itemOrder, final String extraCondition) {
        itemOrderMove(db, table, id, itemOrder, extraCondition, true);
    }

    public static void itemOrderMoveDown(final DB db, final String table, final long id, final long itemOrder) {
        itemOrderMoveUp(db, table, id, itemOrder, null);
    }

    public static void itemOrderMoveDown(final DB db, final String table, final long id, final long itemOrder, final String extraCondition) {
        itemOrderMove(db, table, id, itemOrder, extraCondition, false);
    }*/



    /*private static void itemOrderMove(final DB db, final String table, final long id, final long itemOrder, final String extraCondition, final boolean moveUp) {
        final DBTransaction transaction = new DBTransaction(db);

        final long swapPositionWithBeanId = transaction.addQuery(getMoveItemOrderQuery(table, extraCondition), new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, getItemOrderSwapValue(itemOrder, moveUp));
            }

            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                if (rs.next())
                    return rs.getLong(1);

                throw new IllegalArgumentException("No such item order #" + id + ". Cannot effect change.  Please check database integrity.");
            }
        });

        if (moveUp) {
            incItemOrder(transaction, swapPositionWithBeanId, table);
            decItemOrder(transaction, id, table);
        } else {
            decItemOrder(transaction, swapPositionWithBeanId, table);
            incItemOrder(transaction, id, table);
        }

        transaction.commit();
    }*/

    private static long getItemOrderSwapValue(final long itemOrder, final boolean moveUp) {
        if (moveUp)
            return itemOrder - 1;

        return itemOrder + 1;
    }

    /*private static String getMoveItemOrderQuery(final String table, final String extraCondition) {
        if (extraCondition == null)
            return "SELECT id FROM " + table + " WHERE item_order=?";

        return "SELECT id FROM " + table + " WHERE item_order=? AND " + extraCondition;
    }*/


    /*private static void incItemOrder(final DBTransaction transaction, final long id, final String table) {
        setItemOrder(transaction, id, table, getItemOrder(transaction, id, table) + 1);
    }

    private static void decItemOrder(final DBTransaction transaction, final long id, final String table) {
        setItemOrder(transaction, id, table, getItemOrder(transaction, id, table) - 1);
    }

    private static long getItemOrder(final DBTransaction transaction, final long id, final String table) {
        return transaction.addQuery("SELECT item_order FROM " + table + " WHERE id=?", new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, id);
            }

            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                if (rs.next())
                    return rs.getLong(1);

                throw new IllegalArgumentException("No such ID #" + id);
            }
        });
    }

    private static void setItemOrder(final DBTransaction transaction, final long id, final String table, final long itemOrder) {
        transaction.addUpdate("UPDATE " + table + " SET item_order=? WHERE id=?", new DBQuerySetup() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, itemOrder);
                stat.setLong(2, id);
            }
        });
    }*/

    // New itemOrder implementation

    public static void itemOrderMoveUp(final DB db, final String idFromItemOrderQuery, final String table, final long id, final long itemOrder) {
        itemOrderMove(db, idFromItemOrderQuery, table, id, itemOrder, null, true);
    }

    public static void itemOrderMoveUp(final DB db, final String idFromItemOrderQuery, final String table, final long id, final long itemOrder, final long... parameters) {
        itemOrderMove(db, idFromItemOrderQuery, table, id, itemOrder, toList(parameters), true);
    }

    public static void itemOrderMoveDown(final DB db, final String idFromItemOrderQuery, final String table, final long id, final long itemOrder) {
        itemOrderMove(db, idFromItemOrderQuery, table, id, itemOrder, null, false);
    }

    public static void itemOrderMoveDown(final DB db, final String idFromItemOrderQuery, final String table, final long id, final long itemOrder, final long... parameters) {
        itemOrderMove(db, idFromItemOrderQuery, table, id, itemOrder, toList(parameters), false);
    }

    private static List<Long> toList(final long... parameters) {
        final List<Long> list = new ArrayList<Long>();
        for (long parameter: parameters)
            list.add(parameter);
        return list;
    }

    private static void itemOrderMove(final DB db, final String idFromItemOrderQuery, final String table, final long id, final long itemOrder, final List<Long> parameters, final boolean moveUp) {
        final DBTransaction transaction = new DBTransaction(db);

        final long swapPositionWithBeanId = transaction.addQuery(idFromItemOrderQuery, new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, getItemOrderSwapValue(itemOrder, moveUp));
                if (parameters != null)
                    setupParameters(stat, 2, parameters);
            }

            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                if (rs.next())
                    return rs.getLong(1);

                throw new IllegalArgumentException("No such item order # " + composeIdForException(id, parameters) + ". Cannot effect change.  Please check database integrity.");
            }
        });

        if (moveUp) {
            incItemOrder(transaction, swapPositionWithBeanId, table);
            decItemOrder(transaction, id, table);
        } else {
            decItemOrder(transaction, swapPositionWithBeanId, table);
            incItemOrder(transaction, id, table);
        }

        transaction.commit();
    }

    private static String composeIdForException(final long id, final List<Long> parameters) {
        if (parameters == null)
            return "[" + id + "]";

        final StringBuilder buf = new StringBuilder();
        buf.append("[").append(id).append(", ");
        for (long parameter: parameters)
            buf.append(parameter).append(", ");
        buf.delete(buf.length() - 2, buf.length());
        buf.append("]");

        return buf.toString();
    }

    private static void incItemOrder(final DBTransaction transaction, final long id, final String table) {
        setItemOrder(transaction, id, table, getItemOrder(transaction, id, table) + 1);
    }

    private static void decItemOrder(final DBTransaction transaction, final long id, final String table) {
        setItemOrder(transaction, id, table, getItemOrder(transaction, id, table) - 1);
    }

    private static long getItemOrder(final DBTransaction transaction, final long id, final String table) {
        return transaction.addQuery("SELECT item_order FROM " + table + " WHERE id=?", new DBQuerySetupRetrieveData<Long>() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, id);
            }

            @Override
            public Long processResultSet(ResultSet rs) throws SQLException {
                if (rs.next())
                    return rs.getLong(1);

                throw new IllegalArgumentException("No such ID #" + id);
            }
        });
    }

    private static void setItemOrder(final DBTransaction transaction, final long id, final String table, final long itemOrder) {
        transaction.addUpdate("UPDATE " + table + " SET item_order=? WHERE id=?", new DBQuerySetup() {
            @Override
            public void setupPreparedStatement(PreparedStatement stat) throws SQLException {
                stat.setLong(1, itemOrder);
                stat.setLong(2, id);
            }
        });
    }
}
