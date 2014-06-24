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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
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
		
		String jsonString = json.get("etapes").toString().substring(1, json.get("etapes").toString().length() - 1);
		
		while(jsonString.length() > 0) {
			JSONObject json2 = new JSONObject(jsonString);
			JSONObject json3 = new JSONObject(json2.get("arretStop").toString());
			
			map2.put("heureDepart", json2.get("heureDepart").toString());
			map2.put("libelle", json3.get("libelle").toString());
			map2.put("heureArrivee", json2.get("heureArrivee").toString());
			if(json2.get("marche").toString() == "true") {
				map2.put("route", "Marche pendant " + json2.get("duree").toString());
			}
			else {
				JSONObject json4 = new JSONObject(json2.get("ligne").toString());
				map2.put("ligne", json4.get("numLigne").toString());
				map2.put("route", "Ligne " + json4.get("numLigne").toString() + " pendant " + json2.get("duree").toString() + " en direction de " + json4.get("terminus").toString() + " et descendre à " + json3.get("libelle").toString());
			}
			

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

		Gson gson = new Gson();
		response = gson.toJson(map, TreeMap.class);
		
		//return response;
		return response;
	}
	
	private Map<String, Object> getLatLngItineraire(Map<String, Object> parsed) {

		Map<String, Object> map = new TreeMap<String, Object>();
		Map<Integer, Object> returnMap = new TreeMap<Integer, Object>();
		Map<String, String> hashmap = new HashMap<String, String>();
		
		File stop = new File(this.getServletContext().getRealPath("data/stops.txt"));
		File shape = new File(this.getServletContext().getRealPath("data/shapes.txt"));
		
		try {
			InputStream ips=new FileInputStream(stop); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				String str[] = ligne.split(",");
				String current = str[1].replace("\"", "");
				
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
			
			// Algo pour avoir tous les coordonnées des trajets... Mais ne marche pas car les fichiers de la tan sont pourris...
			
			/*List<String> l = new ArrayList<String>(map.keySet());
			
			ListIterator<String> it = l.listIterator();
			int i = 0;
			//String id_shape = "a", id_shape2 = "a";
			while(it.hasNext()) {
				String current = it.next();
				String nextCurrent = it.next();
				Map<String, String> m = (Map<String, String>) map.get(current);
				Map<String, String> m2 = (Map<String, String>) parsed.get(current);
				Map<String, String> mBis = (Map<String, String>) map.get(nextCurrent);
				Map<String, String> m2Bis = (Map<String, String>) parsed.get(nextCurrent);
				

				System.out.println(m2.toString());
				System.out.println("Coord : " + m.toString());
				System.out.println("bis : " + m2Bis.toString());
				System.out.println("Coord bis : " + mBis.toString());
				ips = new FileInputStream(shape); 
				ipsr = new InputStreamReader(ips);
				br = new BufferedReader(ipsr);
				while((ligne = br.readLine()) != null) {
					String str[] = ligne.split(",");
					
					String id_shape = null;
					String id_shape2 = null;
					String ligne1 = null;
					String ligne2 = null;
					if(m2Bis.containsKey("ligne") && m2Bis.get("ligne").toString().equals(str[0].substring(0, str[0].length() - 4))) {
						if(m.get("lat").toString().equals(str[1]) && m.get("lng").toString().equals(str[2])) {
							hashmap.put("lat", str[1]);
							hashmap.put("lng", str[2]);
							returnMap.put(i, new HashMap<String, String>(hashmap));
							
							id_shape = str[0];
							ligne1 = ligne;
							
							ligne = null;
							ips = new FileInputStream(shape); 
							ipsr = new InputStreamReader(ips);
							br = new BufferedReader(ipsr);
							i++;
							//System.out.println("bis : " + m2Bis.get("route").toString());
							while((ligne = br.readLine()) != null) {
								if(mBis.get("lat").toString().equals(str[1]) && mBis.get("lng").toString().equals(str[2])) {
									hashmap.put("lat", str[1]);
									hashmap.put("lng", str[2]);
									returnMap.put(i, new HashMap<String, String>(hashmap));
									
									id_shape2 = str[0];
									ligne2 = ligne;
								}
							}
							
							if(id_shape.compareTo(id_shape2) > 0 ) {
								
							}
							else if(id_shape.compareTo(id_shape2) == 0) {
								
							}
							else {
								
							}
							
						}
					}
				}
				it.previous();
				
			}
			
			//System.out.println(returnMap.toString());
			
			br.close();*/
		}
		catch (Exception e){
			System.out.println(e.toString());
		}

		return map;
	}
}
