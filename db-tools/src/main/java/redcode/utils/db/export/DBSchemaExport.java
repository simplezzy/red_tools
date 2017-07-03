package redcode.utils.db.export;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;

/**
 * Created by zhiyu.zhou on 2017/7/3.
 */
public class DBSchemaExport {

    public static void main(String args[]) throws SQLException {

        String dbName = "db_perp";   // db_perp
        String url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?serverTimezone=UTC";
        String user = "root";
        String password = "123456";

        Connection connection = DriverManager.getConnection(url, user, password);
        Statement stmt = connection.createStatement();
        ResultSet rsTables = stmt.executeQuery("SELECT * from information_schema.`TABLES` where TABLE_SCHEMA = '" + dbName + "'");

        PreparedStatement psmt = connection.prepareStatement("SELECT * from information_schema.`COLUMNS`  WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ");
        ResultSet rsColumns = null;

        JsonArray tableArray = new JsonArray();

        if ( rsTables != null ){
            while ( rsTables.next() ) {
                String tableName = rsTables.getString("TABLE_NAME");

                JsonObject table = new JsonObject();
                table.addProperty("key", tableName);

                psmt.setString(1, dbName);
                psmt.setString(2, tableName);
                rsColumns = psmt.executeQuery();
                if ( rsColumns != null ){
                    JsonArray columnArray = new JsonArray();

                    while ( rsColumns.next() ){
                        JsonObject column = new JsonObject();
                        column.addProperty("name", rsColumns.getString("COLUMN_NAME"));
                        column.addProperty("iskey", false);
                        column.addProperty("figure", "Cube1");   // Cube1  Decision
                        column.addProperty("color", "purple");

                        columnArray.add(column);
                    }
                    table.add("items", columnArray);
                }

                tableArray.add(table);
            }
        }

        ResultSet rsConstraints = stmt.executeQuery("SELECT * from information_schema.REFERENTIAL_CONSTRAINTS where CONSTRAINT_SCHEMA = '"+dbName+"'");
        JsonArray constraintArray = new JsonArray();
        if (rsConstraints != null){

            while(rsConstraints.next()){
                JsonObject constraint = new JsonObject();
                constraint.addProperty("from", rsConstraints.getString("TABLE_NAME"));
                constraint.addProperty("to", rsConstraints.getString("REFERENCED_TABLE_NAME"));
                constraint.addProperty("text", "0..N");
                constraint.addProperty("toText", "1");

                constraintArray.add(constraint);
            }
        }

        rsTables.close();
        stmt.close();
        if ( rsColumns != null ){
            rsColumns.close();
        }
        psmt.close();
        connection.close();

        Gson gson = new Gson();
        String tablesJson = gson.toJson(tableArray);
        String constraintsJson = gson.toJson(constraintArray);
        System.out.println(tablesJson);
        System.out.println(constraintsJson);
    }

}
