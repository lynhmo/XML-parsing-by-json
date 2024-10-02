package com.example.testxml.service;

import com.google.gson.internal.LinkedTreeMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JsonJsonService {
    @SuppressWarnings("unchecked")
    private void mapperObject(Map<String, Object> output, Map<String, Object> response) {
        try {
            List<Map<String, Object>> dataResponse, dataOutput;
            for (String key : response.keySet()) {
                if (!output.containsKey(key)) {
                    response.put(key, null);
                }

                if (response.get(key) instanceof LinkedTreeMap) {
                    mapperObject((Map<String, Object>) output.get(key), (Map<String, Object>) response.get(key));
                } else if (response.get(key) instanceof ArrayList) {
                    dataResponse = (List<Map<String, Object>>) response.get(key);
                    dataOutput = (List<Map<String, Object>>) output.get(key);
                    for (int i = 0; i < dataResponse.size(); i++) {
                        try {
                            mapperObject(dataOutput.get(0), dataResponse.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.getStackTrace();
        }
    }

    public List<Map<String, Object>> propFilterList(List<Map<String, Object>> fullOutput,
            Map<String, Object> filterOutpt) {
        for (Map<String, Object> stringObjectMap : fullOutput) {
            mapperObject(filterOutpt, stringObjectMap);
        }
        return fullOutput;
    }

}
