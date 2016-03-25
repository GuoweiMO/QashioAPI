/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.swipe;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import api.db.DBHandler;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import org.json.simple.JSONObject;

/**
 *
 * @author guoweim
 */
public class UserData extends HttpServlet {

    static DBHandler db;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); //To change body of generated methods, choose Tools | Templates.        
        db = new DBHandler();
        db.getConnection();        
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
        response.setContentType("text/html;charset=UTF-8");
        String method = request.getParameter("method");
        JSONObject resp = new JSONObject();
        Map result = null;
        switch(method){
            case "newCard":
                result = this.createNewCardWith(request);
                break;
            case "updateCard":
                result = this.updateCardWith(request);
                break;
            case "removeCard":
                result = this.removeCardWith(request);
                break;
            case "updateStatus": // on, off, sending, receiving.
                result = this.updateUserCardStatusWith(request); // status = "on/off"
                break;
            case "sendCard":
                result = this.updateUserCardStatusWith(request); //status = "sending"
                break;
            case "receiveCard":
                result = this.receiveUserCardWith(request); //status = "receiving"
                break;
            default:
                result = new HashMap();
                break;
        }
        try(PrintWriter out = response.getWriter()){
            resp.put("response", result);
            out.write(resp.toJSONString());
        }
    }

    public Map createNewCardWith(HttpServletRequest request){
        String name = request.getParameter("name");
        String jobTitle = request.getParameter("title");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String imageUrl = request.getParameter("imageUrl");
        String status = request.getParameter("status");
        String imageStrData = request.getParameter("imageData");
        byte[] imageBytes = imageStrData.getBytes();
        String updateStr = "INSERT INTO SwipeCard (name, title, email, phone, imageUrl, imageData, status) VALUES (?,?,?,?,?,?,?)";

        List<Object> paras = new ArrayList<>();
        paras.add(name);
        paras.add(jobTitle);
        paras.add(email);
        paras.add(phone);
        paras.add(imageUrl);
        paras.add(status);
        paras.add(imageBytes);

        try {  
            Boolean success = db.updateByPrepStmt(updateStr, paras);
            System.out.println("add new card :" + success );
            if(success){
                String queryId  = "SELECT id SwipeCard WHERE email = " + email;
                Map<String,Object> result = db.findSingleResult(queryId, null);
                return result;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public Map updateCardWith(HttpServletRequest request){
        Map<String, Boolean> result = new HashMap<>();
        Boolean success = false;
        
        String updateStr = "UPDATE SwipeCard SET ";
        String name = request.getParameter("name");
        if(name != null){
            updateStr += (" name=" + name);
        }
        String jobTitle = request.getParameter("title");
        if(jobTitle != null){
            updateStr += (" title=" + jobTitle);
        }
        String email = request.getParameter("email");
        if(email != null){
            updateStr += (" email=" + email);
        }
        String phone = request.getParameter("phone");
        if(phone != null){
            updateStr += (" phone=" + phone);
        }
        String imageUrl = request.getParameter("imageUrl");
        if(imageUrl != null){
            updateStr += (" imageUrl=" + imageUrl);
        }
        String imageStrData = request.getParameter("imageData");
        if(imageStrData != null){
            byte[] imageBytes = imageStrData.getBytes();
            updateStr += (" imageData=" + imageBytes);
        }
        String cardId = request.getParameter("id");
        updateStr += " WHERE id=" + cardId;
        
        try {
            success = db.updateByPrepStmt(updateStr, null);
            result.put("result", success);
        } catch (SQLException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public Map removeCardWith(HttpServletRequest request){
        Map<String, Boolean> result = new HashMap<>();
        Boolean success = false;
        String cardId = request.getParameter("id");
        String removeStr = "DELETE FROM SwipeCard WHERE id="+cardId;
        try {
            success = db.updateByPrepStmt(removeStr, null);
            result.put("result", success);
        } catch (SQLException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public Map updateUserCardStatusWith(HttpServletRequest request){
        Map<String, Boolean> result = new HashMap<>();
        Boolean success = false;
        String cardId = request.getParameter("id");
        String status = request.getParameter("status");
        String updateStatusStr = "UPDATE SwipeCard SET status=? WHERE id=?";
        List<Object> paras = new ArrayList<>();
        paras.add(status);
        paras.add(cardId);
        try {
            success = db.updateByPrepStmt(updateStatusStr, paras);
            result.put("result", success);
        } catch (SQLException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public Map receiveUserCardWith(HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();
        String querySentCard = "SELECT * FROM SwipeCard WHERE status='sending'";
        
        try {
            result = db.findSingleResult(querySentCard, null);
        } catch (SQLException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
