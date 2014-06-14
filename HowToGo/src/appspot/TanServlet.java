package appspot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import models.Json;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")

public class TanServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		URL url;
		String rep = "";

        try {
        	String name = (String) req.getAttribute("nom");
            // get URL content

            String a="https://www.tan.fr/ewp/mhv.php/itineraire/address.json?nom=" + name;
            url = new URL(a);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
            	rep += inputLine;
            }
            br.close();
            int i = rep.indexOf("Addresses");
            rep = "{\"typeLieu\":\""+ rep.substring(i, rep.length() - 1);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
			JSONObject json = new JSONObject(rep);
			
			rep = this.parseJson(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        resp.getWriter().println(rep);
	}
	
	private String parseJson(JSONObject json) throws JSONException {
		String response;
		response = "<div><table><tr><th>id</th><th>nom</th><th>cp</th><th>ville</th></tr>";
		Iterator<Object> it = json.keys();
		while(it.hasNext()) {
			String key = (String) it.next();
			if(key.equals("lieux")) {
				response += "<tr>";
				String result = json.get(key).toString();
				result = result.substring(1, result.length() - 1);
				JSONObject json2 = new JSONObject(result);
				Iterator<Object> it2 = json2.keys();
				
				while(it2.hasNext()) {
					String key2 = (String) it2.next();
					String val = json2.getString(key2);
					response += "<td>" + val + "</td>";
				}
				
				while(result.contains(",{")) {
					result = result.substring(result.indexOf(",{") + 1, result.length());
					json2 = new JSONObject(result);
					it2 = json2.keys();
					while(it2.hasNext()) {
						String key2 = (String) it2.next();
						String val = json2.getString(key2);
						response += "<td>" + val + "</td>";
					}
				}
				response += "</tr>";
			}
		}
		response += "</table></div>";
		
		return response;
	}
}
