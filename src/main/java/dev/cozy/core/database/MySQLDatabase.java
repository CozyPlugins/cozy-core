package dev.cozy.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class MySQLDatabase implements Database {

    private final Logger logger;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int poolSize;
    private final boolean useSsl;

    private HikariDataSource dataSource;

    public MySQLDatabase(Logger logger,
                         String host, int port, String database,
                         String username, String password,
                         int poolSize, boolean useSsl) {
        this.logger = logger;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.poolSize = poolSize;
        this.useSsl = useSsl;
    }

    @Override
    public void connect() throws Exception {
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSsl
                + "&autoReconnect=true";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setPoolName("CozyCore-MySQL");
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cozy_meta ("
                    + "`key` VARCHAR(255) PRIMARY KEY,"
                    + "`value` TEXT"
                    + ")");
        }

        logger.info("MySQL database connected: " + database + "@" + host);
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("MySQL database disconnected");
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
