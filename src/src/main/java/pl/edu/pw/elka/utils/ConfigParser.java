package pl.edu.pw.elka.utils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ConfigParser {

    private JSONArray jsonArray;

    public ConfigParser(String filename) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        jsonArray = (JSONArray)parser.parse(new FileReader(filename));
    }

    public List getAttributesList() {
        return jsonArray;
    }
}
