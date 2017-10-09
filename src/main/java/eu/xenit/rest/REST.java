package eu.xenit.rest;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by Thomas S on 03/10/2017.
 */


@RestController
public class REST {

    @Autowired
    Controller controller;

    @RequestMapping("/1")
    public String createNewDocumentExample() throws ParseException, UnirestException, IOException {
        return controller.createNewDoc("/app:Company_Home/cm:VDL", "New Test Doc", "{http://www.alfresco.org/model/content/1.0}content");
    }

    @RequestMapping("/2")
    public String searchNodes() throws ParseException, UnirestException, IOException {
        String query = "{\n" +
                "  \"query\": {\"property\":{\"name\":\"cm:name\",\"value\":\"VDL\"}},\n" +
                "  \"paging\": {\n" +
                "    \"limit\": 10,\n" +
                "    \"skip\": 0\n" +
                "  },\n" +
                "  \"facets\": {\n" +
                "    \"enabled\": false\n" +
                "  }\n" +
                "}";
        return controller.search(query);
    }

    @RequestMapping("/3")
    public String searchMetaData() throws IOException, UnirestException {
        return controller.getMetaData("workspace://SpacesStore/c8d668f6-ae63-47e0-bb95-435da673b8a8");
    }

    @RequestMapping("/4")
    public String getCategorieRefs() throws ParseException, UnirestException, IOException {
        return controller.getCathRefs("Contrôle interne,Stratégie,Logement");
    }

    @RequestMapping("/5")
    public String createDocWithCats() throws ParseException, UnirestException, IOException {
        return controller.createDocWithProp("/app:company_home/cm:VDL", "This is a testdoc", "{http://vdl.liege.be/model/content/1.0/fin}documentrole", "{\"vdl:vdlmission\":[\"workspace://SpacesStore/d416c8bf-1d28-498b-8441-da836092dd46\", \"workspace://SpacesStore/ef77181f-034a-46cb-b2fb-c8fc30c43593\"]}");
    }

    @RequestMapping("/6")
    public String setNewMetadata() throws ParseException, UnirestException, IOException {
        return controller.setMetadata("workspace://SpacesStore/c8d668f6-ae63-47e0-bb95-435da673b8a8");
    }


}
