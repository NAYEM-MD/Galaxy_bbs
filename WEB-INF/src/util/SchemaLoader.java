package util;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SchemaLoader {

    private static final Logger LOG = LogUtil.getLogger(SchemaLoader.class);

    public static void main(String[] args) {
        System.out.println("Initializing Database Schema...");
        String schemaPath = "SQL/schema.sql";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = Files.newBufferedReader(Paths.get(schemaPath), StandardCharsets.UTF_8)) {

            System.out.println("Connected to: " + conn.getMetaData().getURL());

            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                // Trim line
                String trimmedLine = line.trim();
                
                // Skip comments and empty lines
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }

                // Append line to buffer (preserve newlines for readability/formatting)
                sql.append(line).append("\n");

                // Check for execution triggers
                boolean execute = false;
                
                // Trigger 1: Line is exactly "/" (PL/SQL block terminator)
                if (trimmedLine.equals("/")) {
                    // Remove the slash from the SQL to be executed
                    sql.setLength(sql.length() - line.length() - 1); // Remove last line + newline
                    execute = true;
                }
                // Trigger 2: Line ends with ";" AND we are not in a BEGIN block
                else if (trimmedLine.endsWith(";")) {
                    String start = sql.toString().trim().toUpperCase();
                    boolean isPlSql =
                            start.startsWith("BEGIN") ||
                            start.startsWith("DECLARE") ||
                            start.startsWith("CREATE OR REPLACE");

                    if (isPlSql) {
                        continue;
                    }
                    // Remove the semicolon for JDBC execution
                    sql.setLength(sql.length() - 1); // Remove trailing newline (added above)
                    // We need to remove the semicolon from the buffer.
                    // The buffer currently has "stmt;\n". 
                    // We want "stmt".
                    String currentSql = sql.toString().trim();
                    if (currentSql.endsWith(";")) {
                        sql = new StringBuilder(currentSql.substring(0, currentSql.length() - 1));
                    }
                    execute = true;
                }

                if (execute) {
                    String query = sql.toString().trim();
                    if (!query.isEmpty()) {
                        System.out.println("Executing SQL...");
                        try {
                            stmt.execute(query);
                            System.out.println("✅ Success");
                        } catch (SQLException ex) {
                            System.err.println("❌ Error: " + ex.getMessage());
                            // Don't stop on drop errors (which we handle via PL/SQL anyway, but just in case)
                            // For CREATE statements, we might want to stop or log.
                        }
                    }
                    sql.setLength(0); // Clear buffer
                }
            }
            
            System.out.println("Schema initialization complete.");

        } catch (Exception e) {
            LogUtil.logError(LOG, "Failed to setup database", e);
        }
    }
}
