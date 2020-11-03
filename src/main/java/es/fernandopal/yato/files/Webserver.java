package es.fernandopal.yato.files;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.ArrayList;

public class Webserver implements IConfigFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(Webserver.class);
    private JSONObject jsonObject = null;

    public Webserver() {
        final JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader("webserver.json"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public ArrayList<Object> getList(String key) {
        ArrayList<Object> list = null;

        if(jsonObject.containsKey(key)) {
            final JSONArray jsonArray = (JSONArray) jsonObject.get(key);
            list = new ArrayList<Object>(jsonArray);
        } else {
            missing(key);
        }

        return list;
    }

    public String getString(String key) {
        String string = null;

        if(jsonObject.containsKey(key)) {
            string = jsonObject.get(key).toString();
        } else {
            missing(key);
        }

        return string;
    }

    public Integer getInt(String key) {
        Integer integer = null;

        if(jsonObject.containsKey(key)) {
            integer = Integer.valueOf(jsonObject.get(key).toString());
        } else {
            missing(key);
        }

        return integer;
    }

    private void missing(String key) {
        LOGGER.error("The key '" + key + "' is missing on the webserver.json file");
    }
}
