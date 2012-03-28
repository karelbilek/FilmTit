package filmtit0.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import java.util.List;

/**
 * An attempt to create a minimal handler of a JSON string,
 * for creating only a SubtitleList (older version!) from it.
 * 
 * @author Ruda Rosa
 * @deprecated
 */

public class JSONHandler {
	
	public String jsonText;
	
	public JSONHandler(String jsonText) {
		this.jsonText = jsonText;
	}
	
	public SubtitleList generateSubtitleList () {
		SubtitleList subtitleList = new SubtitleList();
		
		try {
			JSONArray jarray = JSONParser.parseStrict(jsonText).isArray();
			if (jarray != null) {
				subtitleList = parseJSONarray(jarray);
			}
			else {
				Window.alert("Incorrect JSON format! (Not an array.)");
			}
		}
		catch (Exception e) {
			Window.alert("Caught exception: " + e.getLocalizedMessage());
		}
				
		return subtitleList;
	}
	
	private SubtitleList parseJSONarray (JSONArray jarray) {
		
		SubtitleList subtitleList = new SubtitleList();
		for (int i = 0; i < jarray.size(); i++) {
			JSONObject jobject  = jarray.get(i).isObject();
			if (jobject != null) {
				SubtitleChunk chunk = parseJSONobject(jobject);
				subtitleList.addSubtitleChunk(chunk);				
			}
			else {
				Window.alert("Incorrect JSON format! (Not an object.)");
			}
		}
		
		return subtitleList;
	}
	
	private SubtitleChunk parseJSONobject (JSONObject jobject) {
		
		String source = getJSONstring(jobject, "source");
		String match = getJSONstring(jobject, "match");
		String translation = getJSONstring(jobject, "translation");
		
		return new SubtitleChunk(source, match, translation);
	}
	
	private String getJSONstring (JSONObject jobject, String key) {
		
		String string = "";
		if (jobject.containsKey(key)) {
			JSONString jstring = jobject.get(key).isString();
			if (jstring != null) {
				string = jstring.stringValue();
			}
			else {
				Window.alert("Incorrect JSON format! (" + key + " is not a string.)");
			}
		}
		else {
			Window.alert("Incorrect JSON format! (No " + key + ".)");
		}
		
		return string;
	}
	
}
