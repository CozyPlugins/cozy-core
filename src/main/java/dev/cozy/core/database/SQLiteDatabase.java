package dev.cozy.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class SQLiteDatabase implements Database {

    private final File dataFile;
    private final Logger logger;
    private HikariDataSource dataSource;

    public SQLiteDatabase(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "data.db");
        this.logger = logger;
    }

    @Override
    public void connect() throws Exception {
        File parent = dataFile.getParentFile();
        if (!parent.exists()) parent.mkdirs();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dataFile.getAbsolutePath());
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("CozyCore-SQLite");

        this.dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cozy_meta ("
                    + "key TEXT PRIMARY KEY,"
                    + "value TEXT"
                    + ")");
        }

        logger.info("SQLite database connected: " + dataFile.getName());
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("SQLite database disconnected");
        }
    }

    @Override
    public void execute(String sql, Object... params) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepare(conn, sql, params)) {
            stmt.execute();
        }
    }

    @Override
    public <T> List<T> query(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
        }
        return results;
    }

    @Override
    public <T> Optional<T> queryOne(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = query(sql, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    private PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
}
