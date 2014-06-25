package appspot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.gson.Gson;


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
            //rep = "{\"typeLieu\":\""+ rep.substring(1, rep.length() - 1);
            rep = rep.substring(1, rep.length() - 1);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
			
			rep = this.parseJson(rep);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        resp.getWriter().println(rep);
	}
	
	private String parseJson(String rep) throws JSONException {
		String response;
		JSONObject json = new JSONObject(rep);
		Map<Integer, Map> resultMap = new TreeMap<Integer, Map>();
		
		
		int i = 0;
		Boolean last = false;
		Boolean finish = false;
		while(rep.contains("]},") || !finish) {

			Iterator<Object> it = json.keys();
			
			while(it.hasNext()) {
				String key = (String) it.next();
				if(key.equals("lieux")) {
					Map<String, String> map = new HashMap<String, String>();
					String result = json.get(key).toString();
					result = result.substring(1, result.length() - 1);
					JSONObject json2 = new JSONObject(result);
					Iterator<Object> it2 = json2.keys();

					response = "<tr>";
					while(it2.hasNext()) {
						String key2 = (String) it2.next();
						String val = json2.getString(key2);
						map.put(key2, val);
						response += "<td>" + val + "</td>";
					}
					response += "</tr>";
					resultMap.put(i, new HashMap<String, String>(map));
					i++;
					
					while(result.contains(",{")) {
						response += "<tr>";
						result = result.substring(result.indexOf(",{") + 1, result.length());
						json2 = new JSONObject(result);
						it2 = json2.keys();
						while(it2.hasNext()) {
							String key2 = (String) it2.next();
							String val = json2.getString(key2);
							map.put(key2, val);
							response += "<td>" + val + "</td>";
						}

						response += "</tr>";
						resultMap.put(i, new HashMap<String, String>(map));
						i++;
					}
				}
			}
			
			if(!last) {
				rep = rep.substring(rep.indexOf("]},") + 2);
				json = new JSONObject(rep.substring(1));
			} else {
				finish = true;
			}
			
			if(!rep.contains("]},") && rep.length() > 3) {
				last = true;
				rep = "";
			}
		}
		

        Gson gson = new Gson();
		response = gson.toJson(resultMap, TreeMap.class);
		
		return response;
	}
}
