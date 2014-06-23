package appspot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.gson.Gson;

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
		Map<String, Object> map = new TreeMap<String, Object>();
		Map<String, String> map2 = new TreeMap<String, String>();
		Map<String, Object> map3 = new TreeMap<String, Object>();
		map.put("adresseDepart", json.get("adresseDepart").toString());
		map.put("heureDepart", json.get("heureDepart").toString());
		map.put("adresseArrivee", json.get("adresseArrivee"));
		map.put("heureArrivee", json.get("heureArrivee"));
		map.put("duree", json.get("duree"));
		
		response = "<div id='itineraire'>D&eacute;part : " + json.get("adresseDepart") + " &agrave; " + json.get("heureDepart")
				+ "<br/>Arriv&eacute; : " + json.get("adresseArrivee") + " &agrave; " + json.get("heureArrivee")
				+ "<br/>Dur&eacute;e : " + json.get("duree")
				+ "<table>";
		String jsonString = json.get("etapes").toString().substring(1, json.get("etapes").toString().length() - 1);
		
		while(jsonString.length() > 0) {
			JSONObject json2 = new JSONObject(jsonString);
			JSONObject json3 = new JSONObject(json2.get("arretStop").toString());
			
			map2.put("heureDepart", json2.get("heureDepart").toString());
			map2.put("libelle", json3.get("libelle").toString());
			map2.put("heureArrivee", json2.get("heureArrivee").toString());
			
			response += "<tr>";
			response += 	"<td>" + json2.get("heureDepart") + " - " + json2.get("heureArrivee") + "</td><td>" + json3.get("libelle") + "</td>";
			if(json2.get("marche").toString() == "true") {
				map2.put("route", "Marche pendant " + json2.get("duree").toString());
				response +=	"<td>Marche pendant " + json2.get("duree") + "</td>";
			}
			else {
				JSONObject json4 = new JSONObject(json2.get("ligne").toString());
				response += "<td>Ligne " + json4.get("numLigne") + " pendant " + json2.get("duree") + " en direction de " + json4.get("terminus") +"</td>";
				map2.put("route", "Ligne " + json4.get("numLigne").toString() + " pendant " + json2.get("duree").toString() + " en direction de " + json4.get("terminus").toString());
			}
			response += "</tr>";
			

			if(!jsonString.contains(",{")) {
				jsonString = "";
			}
			
			
			jsonString = jsonString.substring(jsonString.indexOf(",{") + 1, jsonString.length());
			map.put(json2.get("heureDepart").toString(), new HashMap<String, Object>(map2));
			map2.clear();
		}
		
		map3 = this.getLatLngItineraire(map);
		map.put("itineraire", new TreeMap<String, Object>(map3));
		
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

		Gson gson = new Gson();
		response = gson.toJson(map, TreeMap.class);
		
		//return response;
		return response;
	}
	
	private Map<String, Object> getLatLngItineraire(Map<String, Object> parsed) {

		Map<String, Object> map = new TreeMap<String, Object>();
		Map<String, String> hashmap = new HashMap<String, String>();
		
		File stop = new File(this.getServletContext().getRealPath("data/stops.txt"));
		File shape = new File(this.getServletContext().getRealPath("data/shapes.txt"));
		
		String depart = parsed.get("adresseDepart").toString();
		String arrive = parsed.get("adresseArrivee").toString();
		
		try {
			InputStream ips=new FileInputStream(stop); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				String str[] = ligne.split(",");
				String current = str[1].replace("\"", "");
				if(depart.toLowerCase().contains(current.toLowerCase())) {
					hashmap.put("lat", str[3]);
					hashmap.put("lng", str[4]);
					map.put(parsed.get("heureDepart").toString(), new HashMap<String, String>(hashmap));
				}
				if(arrive.toLowerCase().contains(current.toLowerCase())) {
					hashmap.put("lat", str[3]);
					hashmap.put("lng", str[4]);
					map.put(parsed.get("heureArrivee").toString(), new HashMap<String, String>(hashmap));
				}
				
				Set<String> s = parsed.keySet();
				Iterator<String> i = s.iterator();
				
				while(i.hasNext()) {
					String cur = i.next();
					if(parsed.get(cur) instanceof Map) {
						Map<String, String> m = (Map<String, String>) parsed.get(cur);
						if(m.get("libelle").toString().toLowerCase().contains(current.toLowerCase())) {
							hashmap.put("lat", str[3]);
							hashmap.put("lng", str[4]);
							map.put(m.get("heureDepart").toString(), new HashMap<String, String>(hashmap));
						}
					}
				}
			}
			br.close();
			
			/*ips = new FileInputStream(shape); 
			ipsr = new InputStreamReader(ips);
			br = new BufferedReader(ipsr);
			while((ligne=br.readLine()) != null) {
				String str[] = ligne.split(",");
				if(map.get("latDep") == str[1]) {
					map.put("latDep", str[2]);
					map.put("lngDep", str[3]);
				}
				if(arrive.toLowerCase().contains(current.toLowerCase())) {
					map.put("latArr", str[3]);
					map.put("lngArr", str[4]);
				}
			}
			
			br.close();*/
		}
		catch (Exception e){
			System.out.println(e.toString());
		}

		return map;
	}
}
