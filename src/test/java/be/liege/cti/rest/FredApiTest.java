package be.liege.cti.rest;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.xenit.rest.Controller;
import eu.xenit.utils.Utils;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class FredApiTest {

    @Test
    public void testCreateDocWithUpload() throws ParseException, UnirestException, IOException {
        Controller c = new Controller();

        String mission = c.getCathRefs("Population");
        List<String> nature = c.getMissionsOrNature("Emprunt", false);

        // Setting the mission category directly at the creation of the directory does not work !
        String nodeRef = createDirectory(c,"OD-275100-01",mission);
        // We need to set it explicitly
        // c.setMetadata(nodeRef);

        // We would like to set the metadata in one call, passing the properties as arguments like with the metadata call
        String nodeRef3 = c.setContentPost(nodeRef,"D:/8871S_LROLE-20161222-1027.pdf");
        // We need to set the metadata afterwoods
        c.setMetadata(nodeRef3);
    }

    @Test
    public void testCreateDocWithPost() throws ParseException, UnirestException, IOException {
        Controller c = new Controller();
        String mission = c.getCathRefs("Population");
        List<String> nature = c.getMissionsOrNature("Emprunt", false);
        String nodeRef = createDirectory(c,"OD-275100-01", mission);
        String catids = c.getCathRefs("Contrôle interne");

        String nodeRef2 = c.createDocWithProp(nodeRef, "ODCatTestDoc5.pdf",
                "{http://www.alfresco.org/model/content/1.0}content",
                "{\"{http://vdl.liege.be/model/content/1.0}vdlnatureprop\":[" + Utils.reformat(nature) + "]}");
        c.setMetadata(nodeRef2);
        c.setContentWithPut(nodeRef2,"D:/8871S_LROLE-20161222-1027.pdf");

        // c.setProperties(nodeRef2,"{http://vdl.liege.be/model/content/1.0}vdlmissionprop", "[\"" + mission.substring(0,mission.lastIndexOf(",")) + "\"]");


    }

    private String createDirectory (Controller c, String dir, String mission) throws ParseException, UnirestException, IOException {
        return c.createDocWithProp("/app:company_home/cm:VDL", dir,
                "{http://vdl.liege.be/model/content/1.0}dossier",
                "{\"{http://vdl.liege.be/model/content/1.0}vdlmissionprop\":[\"" + mission.substring(0,mission.lastIndexOf(",")) + "\"]}");

    }
}