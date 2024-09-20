package com.example.testxml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
public class XmlJsonTransformerService {
//    // Parses the XML string
//    public Document parseXml(String xmlString) throws Exception {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true); // Retain prefixes
//        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlString)));
//    }
//
//    // Parses the JSON string
//    public Map<String, Object> parseJson(String jsonString) throws Exception {
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.readValue(jsonString, Map.class);
//    }
//
//    // Recursively process nodes and remove missing fields
//    private void processNode(Node node, Map<String, Object> jsonFields) {
//        if (node.getNodeType() != Node.ELEMENT_NODE) {
//            return; // Skip non-element nodes
//        }
//
//        // Get the tag name without the namespace prefix
//        String tagName = node.getNodeName();
//        String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;
//
//        // If the field is missing in JSON, remove the node
//        if (!jsonFields.containsKey(fieldWithoutPrefix)) {
//            node.getParentNode().removeChild(node);
//            return;
//        }
//
//        // If the field exists and is a nested structure, recursively process children
//        Object jsonField = jsonFields.get(fieldWithoutPrefix);
//        if (jsonField instanceof Map) {
//            NodeList childNodes = node.getChildNodes();
//            for (int i = 0; i < childNodes.getLength(); i++) {
//                processNode(childNodes.item(i), (Map<String, Object>) jsonField);
//            }
//        }
//    }
//
//    // Updates the XML based on JSON structure
//    public void updateXml(Document xmlDocument, Map<String, Object> jsonFields) {
//        Element rootElement = xmlDocument.getDocumentElement();
//        processNode(rootElement, jsonFields);
//    }
//
//    // Converts Document back to XML string
//    public String convertDocumentToString(Document doc) throws Exception {
//        StringWriter writer = new StringWriter();
//        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));
//        return writer.toString();
//    }
//
//    // Main method to transform XML based on JSON
//    public String transformXmlBasedOnJson(String xmlString, String jsonString) throws Exception {
//        Document xmlDocument = parseXml(xmlString);
//        Map<String, Object> jsonFields = parseJson(jsonString);
//        updateXml(xmlDocument, jsonFields);
//        return convertDocumentToString(xmlDocument);
//    }


    // Parses the XML string
    private Document parseXml(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // To retain prefixes
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlString)));
    }

    // Parses the JSON string
    private Map<String, Object> parseJson(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Map.class);
    }

    // Updates XML based on JSON fields
    private void updateXml(Document xmlDocument, Map<String, Object> jsonFields) {
        org.w3c.dom.NodeList nodeList = xmlDocument.getDocumentElement().getChildNodes();
        removeMissingFields(nodeList, jsonFields);
    }

    // Removes XML fields that don't exist in the JSON

    private void removeMissingFields(NodeList nodeList, Map<String, Object> jsonFields) {
        for (int i = nodeList.getLength() - 1; i >= 0; i--) { // Iterate backward
            if (jsonFields == null) {
                continue; // If jsonFields is null, skip
            }

            Node node = nodeList.item(i);

            // Skip text nodes (like #text)
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue; // Skip the text nodes
            }

            String tagName = node.getNodeName();
            // Strip the namespace prefix for comparison
            String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;

            // If the node has child elements, recurse into the child nodes
            if (node.hasChildNodes()) {
                Map<String, Object> jsonChild = null;
                try {
                    jsonChild = (Map<String, Object>) jsonFields.get(fieldWithoutPrefix);
                } catch (ClassCastException e) {
                    // Check if it's an array in JSON
                    Object jsonArray = jsonFields.get(fieldWithoutPrefix);
                    if (jsonArray instanceof List) {
                        // Handle array logicp
                        List<?> list = (List<?>) jsonArray;
                        int childCount = node.getChildNodes().getLength();
                        for (int j = 0; j < childCount; j++) {
                            Node childNode = node.getChildNodes().item(j);
                            // Check if childNode matches any item in the list
                            // Assuming childNode has an identifiable attribute or structure
                            // Here we can add more sophisticated matching logic based on requirements
                        }
                    }
                }

                // Recurse into the child nodes
                removeMissingFields(node.getChildNodes(), jsonChild);
            }

            // If the field is not in the JSON, remove it
            if (!jsonFields.containsKey(fieldWithoutPrefix)) {
                node.getParentNode().removeChild(node);
            }
        }
    }

//    private void removeMissingFields(NodeList nodeList, Map<String, Object> jsonFields) {
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            if (jsonFields==null){
//                continue;
//            }
//            Node node = nodeList.item(i);
//
//            // Skip text nodes (like #text)
//            if (node.getNodeType() == Node.TEXT_NODE) {
//                continue; // Skip the text nodes
//            }
//
//            String tagName = node.getNodeName();
//
//            // Strip the namespace prefix for comparison
//            String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;
//
//            // If the node has child elements, recurse into the child nodes
//            if (node.hasChildNodes()) {
//                Map<String,Object> jsonChild = null;
//                try {
//                    jsonChild = (Map<String, Object>) jsonFields.get(fieldWithoutPrefix);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                removeMissingFields(node.getChildNodes(), jsonChild);
//            }
//            System.out.println("Check");
//            // If the field is not in the JSON, remove it
//            if (!jsonFields.containsKey(fieldWithoutPrefix)) {
//                node.getParentNode().removeChild(node);
//            }
//
//        }
//    }

    // Converts Document back to XML String
    private String convertDocumentToString(Document doc) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    // Public method to handle XML and JSON transformation
    public String transformXmlBasedOnJson(String xmlString, String jsonString) throws Exception {
        Document xmlDocument = parseXml(xmlString);
        Map<String, Object> jsonFields = parseJson(jsonString);
        updateXml(xmlDocument, jsonFields);
        return convertDocumentToString(xmlDocument);
    }
}
