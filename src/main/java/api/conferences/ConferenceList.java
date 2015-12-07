/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.conferences;

import api.db.DBHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author guoweim
 */
@WebServlet(name = "ConferenceList", urlPatterns = {"/getConferenceList"})
public class ConferenceList extends HttpServlet {
    
    DBHandler db;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); //To change body of generated methods, choose Tools | Templates.
        
        this.connectToMySQL();
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
        // request { categoryId: 1 , categoryName: "" , timeStart: "",  timeEnd: "",  location: [] }
        String categoryId = request.getParameter("categoryId");
        String categoryName = request.getParameter("categoryName");
        
        String timeStart = request.getParameter("timeStart");
        String timeEnd = request.getParameter("timeEnd");
        
        String locations = request.getParameter("locations");
        
        if(categoryId != null ){
            Integer.parseInt(categoryId);
        }
        
        if(locations != null){
           locations = locations.substring(1, locations.length() - 1 );
        }
        
        List<Map<String,Object>> allResults = this.getAllConferences();
        JSONObject conferences = new JSONObject();
        JSONArray conferenceList = new JSONArray();
        for(Map<String,Object> result : allResults){
            Iterator it = result.entrySet().iterator();
            JSONObject conference = new JSONObject();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                Object value = pair.getValue();
                System.out.println(pair.getKey() + " = " +  value);
                if(value instanceof Timestamp){
                    value = value.toString();
                }
                conference.put(pair.getKey(), value);
                
                it.remove(); // avoids a ConcurrentModificationException
            }
            conferenceList.add(conference);
        }
        
        conferences.put("conferences", conferenceList);
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
             out.write(conferences.toJSONString());
             
             out.close();
        }
        
    }
    
    public void connectToMySQL(){
        db = new DBHandler();
        db.getConnection();
    }
    
    public List<Map<String,Object>> getAllConferences(){
        //{id, name, venue, time, image, category
        String queryStr = "SELECT id, name, city, startTime, imageUrl, categoryId, categoryName, cost FROM Rakett_ProConferences";
        List<Map<String,Object>> results = new ArrayList<>();
        try {
            results = db.findMultiResult(queryStr, null);
        } catch (SQLException ex) {
            Logger.getLogger(ConferenceList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return results;
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

}
