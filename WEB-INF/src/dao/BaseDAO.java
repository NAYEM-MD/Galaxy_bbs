package dao;

import util.DBUtil;
import util.LogUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Base DAO that provides shared connection access and consistent error logging.
 * Subclasses use getConnection() and logError() instead of duplicating DBUtil and printStackTrace.
 */
public abstract class BaseDAO {

    private final Logger log = LogUtil.getLogger(getClass());

    protected Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    protected void logError(String message, SQLException e) {
        LogUtil.logError(log, message, e);
    }
}
