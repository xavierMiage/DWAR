package appspot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")

public class ItineraireServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		URL url;
		String rep = "";

        try {
        	String depart = (String) req.getAttribute("depart");
        	String arrive = (String) req.getAttribute("arrive");
        	
        	depart = URLEncoder.encode("Address|ADDRESS11524|JULES VERNE|Nantes||boulevard|307521|2256362", "UTF-8");
        	arrive = URLEncoder.encode("Address|ADDRESS11910|MAIL PABLO PICASSO|Nantes|||306856|2253442", "UTF-8");
            // get URL content

            String a= "https://www.tan.fr/ewp/mhv.php/itineraire/resultat.json?depart=" + depart + "&arrive=" + arrive + "&retour=0&type=0&accessible=0&temps=2014-06-15%2014:00";
            url = new URL(a);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
            	rep += inputLine;
            }
            br.close();
            rep = rep.substring(1, rep.length() - 1);

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
		response = "<div>D&eacute;part : " + json.get("adresseDepart") + " &agrave; " + json.get("heureDepart")
				+ "<br/>Arriv&eacute; : " + json.get("adresseArrivee") + " &agrave; " + json.get("heureArrivee")
				+ "<br/>Dur&eacute;e : " + json.get("duree")
				+ "<table><tr><th>id</th><th>nom</th><th>cp</th><th>ville</th></tr>";
		
		JSONObject json2 = new JSONObject(json.get("etapes").toString().substring(1, json.get("etapes").toString().length() - 1));
		
		response += "<tr>";
		response += 	"<td>" + json2.get("duree") + "</td><td>" + json2.get("arretStop") + "</td>";
		if(json2.has("marche")) {
			response +=	"<td>" + json2.get("marche") + "</td>";
		}
		response += "</tr>";
		
		/*Iterator<Object> it = json.keys();
		while(it.hasNext()) {
			String key = (String) it.next();
			String result = json.get(key).toString();
			result = result.substring(1, result.length() - 1);
			JSONObject json2 = new JSONObject(result);
			Iterator<Object> it2 = json2.keys();

			response += "<tr>";
			while(it2.hasNext()) {
				String key2 = (String) it2.next();
				String val = json2.getString(key2);
				response += "<td>" + val + "</td>";
			}
			response += "</tr>";
			
			while(result.contains(",{")) {
				response += "<tr>";
				result = result.substring(result.indexOf(",{") + 1, result.length());
				json2 = new JSONObject(result);
				it2 = json2.keys();
				while(it2.hasNext()) {
					String key2 = (String) it2.next();
					String val = json2.getString(key2);
					response += "<td>" + val + "</td>";
				}

				response += "</tr>";
			}
		}*/
		response += "</table></div>";
		
		return response;
	}
}
