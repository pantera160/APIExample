package eu.xenit.utils;

import org.springframework.stereotype.Component;

/**
 * Created by Thomas S on 05/10/2017.
 */
@Component
public class Utils {

    public static String[] splitNodeRef(String noderef){
        String[] split1 = noderef.split("://");
        String space = split1[0];
        String[] split2 = split1[1].split("/");
        String store = split2[0];
        String guid = split2[1];
        return new String[]{space, store, guid};
    }

    public static String reformat(String catids){
        catids = catids.substring(0,catids.lastIndexOf(","));
        String res = "";
        for (String cat : catids.split(",")) {
            res += "\""+cat.trim()+"\",";
        }
        System.out.println(res.substring(0,res.lastIndexOf(",")));
        return res.substring(0,res.lastIndexOf(","));
    }

}
