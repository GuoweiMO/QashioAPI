/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.scraping;

import api.db.DBHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author guoweim
 */
@WebServlet(name = "WebData", urlPatterns = {"/getWebData"})
public class WebData extends HttpServlet{
    
    static final String confer_Name = "name"; 
    static final String confer_Venue = "venue"; 
    static final String confer_City = "city"; 
    static final String confer_StartTime = "startTime"; 
    static final String confer_EndTime = "endTime"; 
    static final String confer_Description = "description"; 
    static DBHandler db;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); //To change body of generated methods, choose Tools | Templates.
        
        System.out.println("WebData grabing is running.");
        db = new DBHandler();
        db.getConnection();
        
        for(int i = 1; i< 2 ; i++){
            this.parseWebData("http://www.allconferences.com/search/index/Category__parent_id:1/showLastConference:0/page:"+ i);
        }
    }
    
    
    public Document parseWebData(String url){
        Document doc = new Document(url);
        List<Map<String, Object>> conferenceList = new ArrayList<>();
        try {
            doc = Jsoup.connect(url).get();
            
            Elements conferenceNames = doc.select(".listing_content h2 a");
            Elements conferenceVenues = doc.select(".listing_content .venue_info");           
            Elements conferenceDates = doc.select(".listing_content .conferenceDate");
            Elements conferenceDesc = doc.select(".listing_content .conferenceDescription a");

            
            for(int i = 0; i < conferenceNames.size(); i++){
                Map<String,Object> conference = new HashMap<>();
               
                String conferenceName = conferenceNames.get(i).text();
                conference.put(confer_Name, conferenceName);
               
                Element venueInfo = conferenceVenues.get(i);
//               System.out.println(venueInfo.children().toString());
                int index = 0;
                String hostCity = "";
                for (Element venue : venueInfo.children()) {
                    if(venue.tagName().equals("a")){
                        index ++;
                        if(index == 1){
                            String venueName = venue.text();
                            conference.put(confer_Venue, venueName);
                        } else{
                            hostCity += venue.text() + " ";
                        }
                    }
                    conference.put(confer_City, hostCity);
                }
                Element conferenceDate = conferenceDates.get(i);
                
                String startTime = (conferenceDate.getElementsByClass("begin_txt").get(0))
                        .getElementsByTag("a").get(0).text();
                String endTime = conferenceDate.getElementsByClass("end_txt").text();//.get(0).text();
//                System.out.println(startTime + " to " + conferenceDate.html());
                if(endTime.contains("Ends")){
                   endTime = endTime.replaceAll("Ends", "").trim();
                }
                System.out.println(startTime + " to " + endTime);
                //TODO: formatting data
                Timestamp startTimeStmp = this.fortmatTimestamp(startTime);
                Timestamp endTimeStmp = this.fortmatTimestamp(endTime);
                
                conference.put(confer_StartTime, startTimeStmp);
                conference.put(confer_EndTime, endTimeStmp); 
                
                System.out.println(startTimeStmp + " to " + endTimeStmp);
                
                String description = conferenceDesc.get(i).text();
                conference.put(confer_Description, description);                 
                
                conferenceList.add(conference);
                
//                this.updateDataToDB(conference);
            }
            for(Map conf: conferenceList){
                System.out.println(conf.toString());
            }
            
        } catch (IOException ex) {
            Logger.getLogger(WebData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }
    
//    public static void main(String[] args){
//        WebData data = new WebData();
//        Document doc = data.getWebData("http://www.allconferences.com/search");
//        
//        Elements confNames = doc.select(".listing_content h2 a");
//        
//        for(Element conf:confNames){
//           System.out.println(conf.text());
//        }
//    }
    public boolean updateDataToDB(Map<String, Object> conference){
        boolean flag = false;
        String sqlStr = 
//                "SELECT EXISTS (SELECT * FROM Rakett_ProConferences WHERE name=?)  \n" +
//                "   UPDATE Rakett_ProConferences SET venue=?, city =?, " +
//                            "startTime=?, endTime=?, description=? WHERE name=? \n"  +
//                "ELSE \n" +
//                    "INSERT INTO Rakett_ProConferences(name, venue, city, startTime, endTime, description) " +
//                            "VALUES(?,?,?,?,?,?) )\n";
        
        "INSERT INTO Qashio_Conferences(name, venue, city, startTime, endTime, description) " +
                            "VALUES(?,?,?,?,?,?) " +
        "ON DUPLICATE KEY  UPDATE venue=?, city=?, startTime=?, endTime=?, description=?";     

        
        List<Object> paras = new ArrayList();

        paras.add(conference.get(confer_Name));
        paras.add(conference.get(confer_Venue));
        paras.add(conference.get(confer_City));
        paras.add(conference.get(confer_StartTime));
        paras.add(conference.get(confer_EndTime));
        paras.add(conference.get(confer_Description));
        
        paras.add(conference.get(confer_Venue));
        paras.add(conference.get(confer_City));
        paras.add(conference.get(confer_StartTime));
        paras.add(conference.get(confer_EndTime));
        paras.add(conference.get(confer_Description));
        
//        paras.add(conference.get(confer_Name));
        
        
        
        try {
            flag = db.updateByPrepStmt(sqlStr, paras);
            System.out.println(flag);
            db.connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(WebData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return flag;
    }
    
    public Timestamp fortmatTimestamp(String timeStr){
        Timestamp timeStp = new Timestamp(0);
        System.out.println(timeStr);
        
        if(!"".equals(timeStr) && timeStr != null){
            String monAndDate = "0",  yearAndTime = "0";
            
            monAndDate = timeStr.split(",")[0];
            yearAndTime = timeStr.split(",")[1];
            System.out.println(monAndDate + "  -----   " + yearAndTime);
            
            String year = "0", month = "0", date = "0", time = "0";
            int monthNum = 0;

            if(monAndDate != null){
                monAndDate = monAndDate.trim();
                
                month = monAndDate.split(" ")[0];
                monthNum = this.mapMonStrToNum(month);
                date = monAndDate.split(" ")[1];
                
                System.out.println(month + " ------  " + date);
            }
            if(yearAndTime != null && yearAndTime.trim() != null){
                yearAndTime = yearAndTime.trim();
                if(yearAndTime.split(" ").length > 1){
                    year = yearAndTime.split(" ")[0].trim();
                    //time = yearAndTime.split(" ")[1];
                } else{
                    year = yearAndTime.trim();
                }
                    
            }
            if( "".equals(year)){
                year = "2014";
            }
            if( "".equals(month)){
                month = "0";
            }
            if( "".equals(date)){
                date = "0";
            }
            
            System.out.println(year + "-"+ (monthNum + 1) +"-" + date);
            timeStp = new Timestamp( Integer.parseInt(year) - 1900, 
                                    monthNum,
                                    Integer.parseInt(date), 0, 0, 0, 0);
        }
        return timeStp;
    }
    
    public int mapMonStrToNum(String month){
        int monthNum = 0;
        switch (month){
            case "Jan":
                monthNum = 0;
                break;
            case "Feb":
                monthNum = 1;
                break;
            case "Mar":
                monthNum = 2;
                break;
            case "Apr":
                monthNum = 3;
                break;
            case "May":
                monthNum = 5;
                break;
            case "Jun":
                monthNum = 0;
                break;
            case "Jul":
                monthNum = 6;
                break;
            case "Aug":
                monthNum = 7;
                break;
            case "Sep":
                monthNum = 8;
                break;
            case "Oct":
                monthNum = 9;
                break;
            case "Nov":
                monthNum = 10;
                break;
            case "Dec":
                monthNum = 11;
                break;
            default: 
                break;
        }
        return monthNum;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        Thread.yield();
    }
    
    
}
