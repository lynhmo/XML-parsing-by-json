package com.example.testxml.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestCtrl {

    @PostMapping(consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEmployee(@RequestBody String dto) throws IOException {

        String originalXML = new String(Base64.getDecoder().decode(dto.getBytes()));

        // Create an XmlMapper object
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode node = xmlMapper.readTree(originalXML.getBytes());

        // Create an ObjectMapper to convert it to JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(node);
        System.out.println(json);
        return ResponseEntity.ok(json);
    }

    @PostMapping(value = "/base64", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<?> base64(@RequestBody String dto) {
        return ResponseEntity.ok(new String(Base64.getEncoder().encode(dto.getBytes())));
    }


    @PostMapping(value = "/gson")
    public ResponseEntity<?> gsonTest() throws NullPointerException {
        String dto = "{}";
        Gson gson = new Gson();
        Map<String, Object> testMap = null;
        try {
            testMap = gson.fromJson(dto, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
        if (testMap.isEmpty()) {
            throw new NullPointerException("Empty map");
        }
        return ResponseEntity.ok(testMap);
    }


    private JSONObject xmlap(JSONObject jsonObject) {
        String rs = null;
        JSONObject test = null;
        System.out.println(jsonObject.toString(4));
        for (String key : jsonObject.keySet()) {
            if (key.endsWith(":CongDan")) {
                return jsonObject.getJSONObject(key);
            }
            test = xmlap(jsonObject.getJSONObject(key));
            break;
        }
        return test;
    }

    @PostMapping(value = "/testParse", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> asdjasjdh(@RequestBody String originalXML) throws IOException {
        try {
            // Convert XML to JSON
            JSONObject jsonObject = XML.toJSONObject(originalXML);
//            System.out.println("Converted to JSON:");
//            System.out.println(jsonObject.toString(4));  // Pretty-print with 4 spaces
//
//            JSONObject bodyKey = this.xmlap(jsonObject);
//
//            // Now you can safely get the Body object
//            if (bodyKey != null) {
//                // Use 'body' as needed
//                System.out.println(bodyKey.toString(4));
//            }
//            System.out.println("Modified JSON:");
//            System.out.println(jsonObject.toString(4));  // Print modified JSON
//
//
//            // Convert JSON back to XML
//            String modifiedXml = XML.toString(jsonObject);
//            System.out.println("Converted back to XML:");
//            System.out.println(modifiedXml);
            return ResponseEntity.ok(jsonObject.toString(4));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");

    }


}
