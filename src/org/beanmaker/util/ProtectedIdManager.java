package org.beanmaker.util;

import net.pythoud.passwords.CharacterSets;
import net.pythoud.passwords.PasswordMaker;

import org.dbbeans.sql.DBAccess;
import org.dbbeans.sql.DBQueryRetrieveData;
import org.dbbeans.sql.DBQuerySetup;

import org.dbbeans.util.Dates;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProtectedIdManager {

    private static final PasswordMaker CODE_GENERATOR =
            PasswordMaker.getFactory()
                    .setCharCount(32)
                    .setMainChars(CharacterSets.UPPER_CASES + CharacterSets.LOWER_CASES + CharacterSets.DIGITS)
                    .create();

    private final DBAccess dbAccess;
    private final String table;

    public ProtectedIdManager(final DBAccess dbAccess, final String table) {
        this.dbAccess = dbAccess;
        this.table = table;
    }

    public String getCode(final long id) {
        String code = getCodeFromDB(id);

        if (code == null)
            code = createCode(id);

        return code;
    }

    public long getId(final String code) {
        return getIdFromDB(code);
    }

    public boolean codeMatchesId(final String code, final long id) {
        return getId(code) == id;
    }

    private String getCodeFromDB(final long id) {
        return dbAccess.processQuery(
                "SELECT code FROM " + table + " WHERE protected_id=?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setLong(1, id);
                    }
                },
                new DBQueryRetrieveData<String>() {
                    @Override
                    public String processResultSet(final ResultSet rs) throws SQLException {
                        if (rs.next())
                            return rs.getString(1);

                        return null;
                    }
                }
        );
    }

    private String createCode(final long id) {
        final String code = CODE_GENERATOR.getPassword();

        if (!exists(code))
            dbAccess.processUpdate(
                    "INSERT INTO " + table + " (protected_id, code, creation_date) VALUES (?, ?, ?)",
                    new DBQuerySetup() {
                        @Override
                        public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                            stat.setLong(1, id);
                            stat.setString(2, code);
                            stat.setTimestamp(3, Dates.getCurrentTimestamp());
                        }
                    }
            );

        return code;
    }

    private boolean exists(final String code) {
        return dbAccess.processQuery(
                "SELECT protected_id FROM " + table + " WHERE code=?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setString(1, code);
                    }
                },
                new DBQueryRetrieveData<Boolean>() {
                    @Override
                    public Boolean processResultSet(ResultSet rs) throws SQLException {
                        return rs.next();
                    }
                }
        );
    }

    private long getIdFromDB(final String code) {
        return dbAccess.processQuery(
                "SELECT protected_id FROM " + table + " WHERE code=?",
                new DBQuerySetup() {
                    @Override
                    public void setupPreparedStatement(final PreparedStatement stat) throws SQLException {
                        stat.setString(1, code);
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
}
