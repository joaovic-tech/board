package tech;

import tech.joaovic.persistence.migration.MigrationStrategy;
import tech.joaovic.ui.Menu;

import java.sql.Connection;
import java.sql.SQLException;

import static tech.joaovic.persistence.config.ConnectionConfig.getConnection;

public class Main {
    public static void main(String[] args) throws SQLException {
        try(Connection connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }
        new Menu().execute();
    }
}
