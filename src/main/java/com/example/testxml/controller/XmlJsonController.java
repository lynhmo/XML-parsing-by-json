package com.example.testxml.controller;

import com.example.testxml.service.JsonJsonService;
import com.example.testxml.service.XmlJsonService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transform")
public class XmlJsonController {

    @Autowired
    private XmlJsonService transformerService;
    @Autowired
    private JsonJsonService jsonJsonService;

    @PostMapping(value = "/xml-to-json", produces = MediaType.TEXT_XML_VALUE)
    public String transformXml() throws Exception {

        String xmlString = Files.readString(Paths.get("src/main/resources/XML/Request.xml"));
        String jsonString = Files.readString(Paths.get("src/main/resources/XML/ConfigXml.json"));

        return transformerService.transformXmlBasedOnJson(xmlString, jsonString);
    }

    @PostMapping(value = "/json-to-json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> transformJson() throws Exception {
        Gson gson = new Gson();
        // Read XML file content into a String
        String fullDataJson = Files.readString(Paths.get("src/main/resources/JSON/FullData.json"));
        String fillterDataJson = Files.readString(Paths.get("src/main/resources/JSON/fillter.json"));

        Map<String, Object> fullDataMap = gson.fromJson(fullDataJson, new TypeToken<Map<String, Object>>() {
        }.getType());
        List<Map<String, Object>> listFullDataMap = new ArrayList<>();
        // Data trả ra là dạng list nên cần tạo tạm 1 list để giả lập
        listFullDataMap.add(fullDataMap);

        Map<String, Object> fillterDataMap = gson.fromJson(fillterDataJson, new TypeToken<Map<String, Object>>() {
        }.getType());

        return jsonJsonService.propFilterList(listFullDataMap, fillterDataMap);
    }


}

