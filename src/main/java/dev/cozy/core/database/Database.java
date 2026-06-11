package dev.cozy.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Database {

    void connect() throws Exception;

    void disconnect();

    void execute(String sql, Object... params) throws SQLException;

    <T> List<T> query(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException;

    <T> Optional<T> queryOne(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException;

    boolean isConnected();

    @FunctionalInterface
    interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
