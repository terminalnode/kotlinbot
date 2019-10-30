package xyz.kazuthecat.coffeebot;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles all of the bots database functions and requires a working MySQL server.
 */
public class DBHandler {
  private final String dbAddr;

  /**
   * Default constructor.
   * @param dbAddr The address to the database, can be an IP or host name.
   * @param dbUser The username with which we'll login to the database.
   * @param dbPass The password with which we'll login to the database.
   */
  DBHandler(String dbAddr, String dbUser, String dbPass) {
    this.dbAddr = "jdbc:mysql://" + dbAddr + "/coffeedb?user=" + dbUser + "&password=" + dbPass;
  }

  /**
   * Get a Connection object for the database.
   * @return Connection object.
   * @throws SQLException If a connection can not be established.
   * @throws ClassNotFoundException If the MySQL JDBC Driver can not be found.
   */
  private Connection getDBConnection() throws SQLException, ClassNotFoundException {
    Class.forName("com.mysql.jdbc.Driver");
    return DriverManager.getConnection(dbAddr);
  }

  /**
   * Execute an arbitrary number of SQL statements.
   * @param sqls The statements to be executed.
   * @return A DBEnum indicating if the operation was successful.
   */
  public boolean execute(String[] sqls) {
    Connection conn = null;
    Statement stmt = null;
    boolean returnValue;
    try {
      conn = getDBConnection();
      stmt = conn.createStatement();
      for (String sql : sqls) {
        stmt.addBatch(sql);
      }
      stmt.executeBatch();
      returnValue = true;
    } catch (SQLException | ClassNotFoundException e) {
      returnValue = false;
      executeException(e);
    } finally {
      try { if (stmt != null) stmt.close(); } catch (Exception e) { executeException(e); }
      try { if (conn != null) conn.close(); } catch (Exception e) { executeException(e); }
    }
    return returnValue;
  }

  /**
   * Execute a query with a set of keys.
   * Execute an a single SQL statement an arbitrary number of times with different keys.
   * For different statements the function needs to be called multiple times.
   * @param sql The statements to be executed.
   * @param keys The keys to be filled in for each statement.
   * @return A DBEnum indicating if the operation was successful.
   */
  public boolean execute(String sql, String[][] keys) {
    Connection conn = null;
    PreparedStatement stmt = null;
    boolean returnValue;
    try {
      conn = getDBConnection();
      stmt = conn.prepareStatement(sql);
      for (String[] keyset : keys) {
        for (int i = 0; i < keyset.length; i++) {
          stmt.setString(i + 1, keyset[i]);
        }
        stmt.execute();
        stmt.clearParameters();
      }
      returnValue = true;
    } catch (SQLException | ClassNotFoundException e) {
      returnValue = false;
      executeException(e);
    } finally {
      try { if (stmt != null) stmt.close(); } catch (Exception e) { executeException(e); }
      try { if (conn != null) conn.close(); } catch (Exception e) { executeException(e); }
    }
    return returnValue;
  }

  /**
   * Execute a query.
   * @param sql The query to be executed.
   * @return All rows in the form of a list of hashmaps with column names as keys.
   */
  public List<Map<String, String>> query(String sql) {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;
    List<Map<String, String>> result = null;
    try {
      conn = getDBConnection();
      stmt = conn.createStatement();
      rset = stmt.executeQuery(sql);
      result = parseResultSet(rset);

    } catch (SQLException | ClassNotFoundException e) {
      executeException(e);
    } finally {
      try { if (stmt != null) stmt.close(); } catch (Exception e) { executeException(e); }
      try { if (conn != null) conn.close(); } catch (Exception e) { executeException(e); }
      try { if (rset != null) rset.close(); } catch (Exception e) { executeException(e); }
    }
    return result;
  }

  /**
   * Execute a query with a set of keys.
   * @param sql The query to be executed.
   * @param keys The set of keys to apply to the query.
   * @return All rows in the form of a list of hashmaps with column names as keys.
   */
  public List<Map<String, String>> query(String sql, String[] keys) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rset = null;
    List<Map<String, String>> result = null;
    try {
      conn = getDBConnection();
      stmt = conn.prepareStatement(sql);
      for (int i = 0; i < keys.length; i++) {
        // SQL-datatype doesn't seem to matter, works for int as well
        stmt.setString(i + 1, keys[i]);
      }
      rset = stmt.executeQuery();
      result = parseResultSet(rset);

    } catch (SQLException | ClassNotFoundException e) {
      executeException(e);
    } finally {
      try { if (stmt != null) stmt.close(); } catch (Exception e) { executeException(e); }
      try { if (conn != null) conn.close(); } catch (Exception e) { executeException(e); }
      try { if (rset != null) rset.close(); } catch (Exception e) { executeException(e); }
    }
    return result;
  }

  /**
   * Helper function for the query methods. Parses a ResultSet into a list of Maps.
   * @param rset The ResultSet that's being parsed.
   * @return A List of Map. The keys are the columns in the result and the values the values.
   * @throws SQLException The function doesn't handle any exceptions by itself.
   */
  private List<Map<String, String>> parseResultSet(ResultSet rset) throws SQLException {
    List<Map<String, String>> result = new ArrayList<>();
    ResultSetMetaData rsetMeta = rset.getMetaData();
    String[] columns = new String[rsetMeta.getColumnCount()];
    for (int i = 0; i < columns.length; i++) { columns[i] = rsetMeta.getColumnLabel(i + 1); }

    while (rset.next()) {
      Map<String, String> nextResult = new HashMap<>();
      for (String column : columns) {
        nextResult.put(column, rset.getString(column));
      }
      result.add(nextResult);
    }
    return result;
  }

  /**
   * Exception handling to avoid duplicate code in other functions.
   * @param e The Exception we are handling.
   */
  private void executeException(Exception e) {
    if (e instanceof SQLException) {
      System.out.println("ERROR: Encountered an SQLException when trying to execute batch statement.");
    } else if (e instanceof ClassNotFoundException) {
      System.out.println("ERROR: Could not find MySQL Database Driver.");
    }
    e.printStackTrace();
  }

}
