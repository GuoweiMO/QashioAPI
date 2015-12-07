/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.accounts;

import api.db.DBHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author guoweim
 */
@WebServlet(name = "UserVerify", urlPatterns = {"/verify"})
public class UserVerify extends HttpServlet {

    DBHandler db;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); //To change body of generated methods, choose Tools | Templates
        connectToMySQL();
    }
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // request "?username=str1&password=str2&type=check/add"
        String clientUserName = request.getParameter("username");
        String clientPassword = request.getParameter("password");
        String methodType = request.getParameter("type");
        Map<String,Object> outcome = new HashMap<>();
        JSONObject output = new JSONObject();
        
        switch (methodType) {
            case "check":
                outcome = this.checkUserInfo(clientUserName,clientPassword);
                output = new JSONObject(outcome);
                break;
            case "add":
                break;
            default:
                break;
        }
        
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println(output.toString());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    public void connectToMySQL(){
        db = new DBHandler();
        db.getConnection();
    }
    
    public Map<String,Object> checkUserInfo(String userName, String password){
        //{id, name, venue, time, image, category
        String queryStr = "SELECT * FROM Qashio_UserInfos WHERE userName=?";
        Map<String,Object> results = new HashMap<>();
        Map<String,Object> checkOutcome = new HashMap<>();
        List<Object> paras = new ArrayList<>();
        paras.add(userName);
        try {
            checkOutcome.put("success", false);
            checkOutcome.put("msg", "");
            checkOutcome.put("sessionId", System.currentTimeMillis());
            results = db.findSingleResult(queryStr, paras);
            if(results.get("userName") != null){
                String dbPassword= results.get("password").toString();
                if(dbPassword != null && (dbPassword.trim() == null ? password.trim() == null : dbPassword.trim().equals(password.trim()))){
                    checkOutcome.replace("success", true);
                } else{
                    checkOutcome.replace("success", false);
                    checkOutcome.replace("msg", "password wrong");
                }
            }else{
                checkOutcome.replace("success", false);
                checkOutcome.replace("msg", "username not exist");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserVerify.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return checkOutcome;
    }
}
