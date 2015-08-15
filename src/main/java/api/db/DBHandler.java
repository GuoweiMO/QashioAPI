/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.db;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author guoweim
 */
public class DBHandler {
    private final String USERNAME = "root";
    private final String PASSWORD = "b7DmW8Gc81HH";
    private final String DRIVER = "com.mysql.jdbc.Driver";
    private final String URL = "jdbc:mysql://198.100.146.69:3306/mysql";

    private Connection connection;

    //SQL execution statement
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    public DBHandler(){
        try {
            Class.forName(DRIVER);
            System.out.println("Driver Loaded!");
        }catch (Exception e){

        }
    }

    public Connection getConnection(){
        try{
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);            
            System.out.println("Connected to MySQL!!");
        }catch (Exception e){
            e.printStackTrace();
        }
        return connection;
    }

    public boolean updateByPrepStmt(String sql,List<Object> paras) throws SQLException{
        boolean flag = false;
        int result = -1; // the number of lines influenced
        pstmt = connection.prepareStatement(sql);
        int index = 1;
        if(paras != null && !paras.isEmpty()){
            for (Object para : paras) {
                pstmt.setObject(index++, para);
            }
        }
        result = pstmt.executeUpdate();
        flag = result > 0;
        return flag;
    }

    //return single record
    public Map<String,Object> findSingleResult(String sql,List<Object> paras) throws SQLException{
        Map<String,Object> map = new HashMap<>();

        pstmt = connection.prepareStatement(sql);

        int index = 1;
        if(paras!=null && paras.isEmpty()){
            for (Object para : paras) {
                pstmt.setObject(index++, para);
            }
        }
        resultSet = pstmt.executeQuery(); // return the query result
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount(); // get the number of column
        while (resultSet.next()){
            for(int i=0;i<col_len;i++){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_val = resultSet.getObject(cols_name);
                if(cols_val==null ) {
                    cols_val = "";
                }
                map.put(cols_name,cols_val);
            }

        }
        return map;
    }

    //return multiple records
    public List<Map<String,Object>> findMultiResult(String sql,List<Object> paras) throws SQLException{
        List<Map<String,Object>> list = new ArrayList<>();
        pstmt = connection.prepareStatement(sql);
        int index = 1;
        if(paras!=null && paras.isEmpty()){
            for (Object para : paras) {
                pstmt.setObject(index++, para);
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount();
        while (resultSet.next()){
            Map<String,Object> map = new HashMap<>();
            for(int i=0; i<col_len;i++){
                String cols_name = metaData.getColumnName(i+1); //start from 1
                Object cols_val = resultSet.getObject(cols_name);
                if(cols_val == null){
                    cols_val = "";
                }
                map.put(cols_name,cols_val);
            }
            list.add(map);
        }

        return list;

    }

//    public static void main(String[] args){
//        DBHandler db = new DBHandler();
//        db.getConnection();
//        String sql = "SELECT * FROM Rakett_UserAccounts";
//        List<Object> paras = new ArrayList<>();
//        List<Map<String,Object>> results;
//        
//        try{
//            results = db.findMultiResult(sql, paras);
//            System.out.println(results.toString());
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }

}
