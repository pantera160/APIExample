package eu.xenit.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RawBody;
import eu.xenit.utils.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas S on 03/10/2017.
 */

@Component
public class Controller {
    private final String hosturl = "http://alfresco-test:8080/alfresco/s/";

    public Controller(){}

    private String execute(JSONObject body, String url, String method) throws IOException, UnirestException {
        HttpResponse<JsonNode> jsonresponse = Unirest.post(hosturl+url)
                .basicAuth("admin", "admin")
                .header("accept", "application/json")
                .body(body.toJSONString())
                .asJson();

        return jsonresponse.getBody().toString();
    }

    /**
     * Send a file with the PUT HTTP Method
     *
     * @param filename
     * @param url
     * @return
     * @throws IOException
     * @throws UnirestException
     */
    private String executePut(String filename, String url) throws IOException, UnirestException {
        Path path = Paths.get(filename);
        //byte[] data = Files.readAllBytes(path);
        HttpResponse<JsonNode> jsonresponse = Unirest.put(hosturl+url)
                .basicAuth("admin", "admin")
                .header("accept", "application/json")
                .field("file", new File(filename))
                // .body(data) // Same problem
                .asJson();

        return jsonresponse.getBody().toString();
    }

    public String setContent(String nodeRef, String filename) throws IOException, UnirestException {
        String[] splitRef = Utils.splitNodeRef(nodeRef);
        String url = "apix/v1/nodes/"+splitRef[0]+"/"+splitRef[1]+"/"+splitRef[2]+"/content";
        return executePut(filename, url);

        // Version avec Apache HTTP Client -> resultat identique
        // return executeHttpClient(filename, url);
    }

    public String setContentPost(String nodeRef, String filename) throws IOException, UnirestException {

        return Unirest.post(hosturl + "apix/v1/nodes/upload")
                .basicAuth("admin", "admin")
                .header("accept", "application/json")
                .field("parent", "workspace://SpacesStore/65285809-73b2-4434-b258-8eddb55d1369")
                .field("type", "cm:content")
                .field("file", new File(filename))//"/home/willem/devXenit/apix-examples/build/resources/test/test.txt"
                .asJson().toString();
       // System.out.println(result.getBody());
    }

    /*
    remplace execute(String filename, String url) pour un test avec HTTPClient
     */
    public String executeHttpClient(String fileName, String url) throws IOException {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();


        Path path = Paths.get(fileName);
        byte[] data = Files.readAllBytes(path);
        System.out.println(hosturl+url);
        HttpPut put = new HttpPut(hosturl+url);

        EntityBuilder eb = EntityBuilder.create();
        eb.setBinary(data);
        HttpEntity entity = eb.build();
        put.setEntity(entity);

        org.apache.http.HttpResponse response = client.execute(put);
        return response.toString();
    }

    public String createNewDoc(String path, String name, String type) throws IOException, ParseException, UnirestException {
        return createDocWithProp(path, name, type, null);
    }

    public String getCathRefs(String missions) throws IOException, UnirestException, ParseException {
        List<String> cathRefs = getCathRefsList(missions);


        String reponseString = "";
        for(String ref:cathRefs){
            reponseString += ref+", ";
        }
        return reponseString;
    }

    public List<String> getCathRefsList(String missions) throws IOException, UnirestException, ParseException {
        JSONObject body;
        JSONObject query;

        ArrayList<String> cathRefs = new ArrayList<>();
        for (String mission : missions.split(",")) {
            JSONParser parser = new JSONParser();
            body = new JSONObject();
            query = new JSONObject();
            JSONObject pathjson = new JSONObject();


            pathjson.put("path", "/cm:categoryRoot/vdl:vdlmission/*");
            JSONArray and = new JSONArray();
            and.add(parser.parse("{\"property\":{\"name\":\"cm:name\",\"value\":\"" + mission + "\",\"exact\":true}}"));
            and.add(pathjson);
            query.put("and", and);
            body.put("query", query);
            String response = execute(body, "apix/v1/search", "POST");

            JSONObject responseJSON = (JSONObject) parser.parse(response);
            System.out.println(responseJSON);
            cathRefs.add(((JSONArray) responseJSON.get("noderefs")).get(0).toString());
        }
        return cathRefs;
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
        System.out.println(responseJSON.toJSONString());
        String parentRef = ((JSONArray) responseJSON.get("noderefs")).get(0).toString();



        body = new JSONObject();
        body.put("parent", parentRef);
        body.put("name", name);
        body.put("type", type);
        if(properties != null){
            JSONObject propjson = (JSONObject) parser.parse(properties);
            body.put("properties", propjson);
        }
        response = execute(body, "apix/v1/nodes", "POST");
        responseJSON =(JSONObject) parser.parse(response);
        System.out.println(responseJSON.toJSONString());
        String nodeRef = ((JSONObject) responseJSON.get("metadata")).get("id").toString();


        return nodeRef;
    }

    public String setProperties(String nodeRef, String property, String jsonValue) throws IOException, UnirestException, ParseException {
        String[] splitRef = Utils.splitNodeRef(nodeRef);
        JSONParser parser = new JSONParser();
        String url = "apix/v1/nodes/"+splitRef[0]+"/"+splitRef[1]+"/"+splitRef[2]+"/metadata";
        String JSONString = "{\n" +
                "  \"propertiesToSet\": {"+property+":"+jsonValue+"}\n" +
                "}";
        JSONObject body = (JSONObject) parser.parse(JSONString);
        return execute(body, url, "POST");
    }
}
