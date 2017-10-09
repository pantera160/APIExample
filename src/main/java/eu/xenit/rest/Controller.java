package eu.xenit.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.xenit.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Thomas S on 03/10/2017.
 */

@Component
public class Controller {
    private final String hosturl = "http://128.1.0.212:8080/alfresco/s/";

    public Controller(){}

    private String execute(JSONObject body, String url, String method) throws IOException, UnirestException {
        HttpResponse<JsonNode> jsonresponse = Unirest.post(hosturl+url)
                .basicAuth("admin", "admin")
                .header("accept", "application/json")
                .body(body.toJSONString())
                .asJson();

        return jsonresponse.getBody().toString();
    }

    public String createNewDoc(String path, String name, String type) throws IOException, ParseException, UnirestException {
        return createDocWithProp(path, name, type, null);
    }

    public String getCathRefs(String missions) throws IOException, UnirestException, ParseException {
        JSONObject body;
        JSONObject query;

        ArrayList<String> cathRefs = new ArrayList<>();
        for(String mission : missions.split(",")) {
            body = new JSONObject();
            query = new JSONObject();
            JSONObject pathjson = new JSONObject();
            pathjson.put("path", "/cm:categoryRoot/vdl:vdlmission/*");
            JSONArray and = new JSONArray();
            and.add("{\"property\":{\"name\":\"cm:name\",\"value\":\"" + mission + "\",\"exact\":true}}");
            and.add(pathjson);
            query.put("and", and);
            body.put("query", query);
            String response = execute(body, "apix/v1/search", "POST");
            JSONParser parser = new JSONParser();
            JSONObject responseJSON = (JSONObject) parser.parse(response);
            System.out.println(responseJSON);
            cathRefs.add(((JSONArray) responseJSON.get("noderefs")).get(0).toString());
        }

        String reponseString = "";
        for(String ref:cathRefs){
            reponseString += ref+", ";
        }
        return reponseString;
    }

    public String search(String query) throws ParseException, IOException, UnirestException {
        JSONParser parser = new JSONParser();
        return execute((JSONObject) parser.parse(query), "apix/v1/search", "POST");
    }

    public String getMetaData(String nodeRef) throws IOException, UnirestException {
        String[] splitRef = Utils.splitNodeRef(nodeRef);
        String url = "apix/v1/nodes/"+splitRef[0]+"/"+splitRef[1]+"/"+splitRef[2]+"/metadata";
        return execute(new JSONObject(), url, "GET");
    }

    public String setMetadata(String nodeRef) throws IOException, UnirestException, ParseException {
        String[] splitRef = Utils.splitNodeRef(nodeRef);
        JSONParser parser = new JSONParser();
        String url = "apix/v1/nodes/"+splitRef[0]+"/"+splitRef[1]+"/"+splitRef[2]+"/metadata";
        String JSONString = "{\n" +
                "  \"aspectsToAdd\": [\"{http://www.alfresco.org/model/system/1.0}temporary\"],\n" +
                "  \"propertiesToSet\": {\"{http://www.alfresco.org/model/content/1.0}title\":[\"My new title\"]}\n" +
                "}";
        JSONObject body = (JSONObject) parser.parse(JSONString);
        return execute(body, url, "POST");
    }

    public String createDocWithProp(String path, String name, String type, String properties) throws IOException, UnirestException, ParseException {
        JSONObject body = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("path", path);
        body.put("query", query);
        String response = execute(body, "apix/v1/search", "POST");
        JSONParser parser = new JSONParser();
        JSONObject responseJSON = (JSONObject) parser.parse(response);
        String parentRef = ((JSONArray) responseJSON.get("noderefs")).get(0).toString();




        body = new JSONObject();
        body.put("parent", parentRef);
        body.put("name", name);
        body.put("type", type);
        if(properties != null){
            body.put("properties", properties);
        }
        response = execute(body, "apix/v1/nodes", "POST");
        responseJSON =(JSONObject) parser.parse(response);
        String nodeRef = ((JSONObject) responseJSON.get("metadata")).get("id").toString();


        return nodeRef;
    }
}
