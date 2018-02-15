package eu.xenit.rest;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.xenit.utils.Utils;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

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

        return controller.createNewDoc("/app:company_home/cm:VDL/vdletranger:testOD", "testUploadOD.txt", "{http://www.alfresco.org/model/content/1.0}content");
    }


    @RequestMapping("/1/{name}/{path}")
    public String createNewDocumentExample(@PathVariable(value = "name", required = false) @NotNull final String name, @PathVariable(value = "path", required = false) @NotNull final String pathname ) throws
            ParseException,
            UnirestException, IOException {
        return controller.createNewDoc(pathname, name,"{http://www.alfresco.org/model/content/1.0}content");

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
        String catids = Utils.reformat(controller.getCathRefs("Bon de commande"));
        System.out.println(catids);
        return controller.createDocWithProp("/app:company_home/cm:VDL", "TestDocCat", "{http://vdl.liege.be/model/content/1.0/fin}documentrole", "{\"vdl:vdlnatureprop\":["+catids+"]}");

    }

    @RequestMapping("/6")
    public String setNewMetadata() throws ParseException, UnirestException, IOException {
        return controller.setMetadata("workspace://SpacesStore/c8d668f6-ae63-47e0-bb95-435da673b8a8");
    }


    // @RequestMapping("/7/{path}")
    @RequestMapping(method = RequestMethod.GET, path = "/7/**")
    @NotNull
    public @ResponseBody String setContent(HttpServletRequest request) throws ParseException, UnirestException, IOException {
        String pathname = extractFilePath(request);
        String nodeRef = this.createNewDocumentExample("testDOC.txt", pathname);
        return controller.setContentWithPut(nodeRef,"D://out.xml");
    }

    @RequestMapping("/8")
    public String setContent2() throws ParseException, UnirestException, IOException {
        //String nodeRef = this.createNewDocumentExample();
        return controller.setContentPost("workspace://SpacesStore/0bf9b981-d054-4bb8-a9a1-691c188d6213","D://test.txt");
    }

    private static String extractFilePath(HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        return apm.extractPathWithinPattern(bestMatchPattern, path);
    }


}
