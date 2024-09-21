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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class XmlJsonTransformerService {

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

    private void removeMissingFields(NodeList nodeList, Object jsonElement) {
        if (jsonElement instanceof Map) {
            Map<String, Object> jsonFields = (Map<String, Object>) jsonElement;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                // Skip text nodes (like #text)
                if (node.getNodeType() == Node.TEXT_NODE) {
                    continue;
                }

                String tagName = node.getNodeName();
                String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;

                // If the node has child elements, recurse into the child nodes
                if (node.hasChildNodes()) {
                    Object jsonChildElement = jsonFields.get(fieldWithoutPrefix);

                    if (jsonChildElement != null) {
                        // Recursively handle nested structures
                        removeMissingFields(node.getChildNodes(), jsonChildElement);
                    } else {
                        // Field does not exist in the JSON map, remove the node
                        node.getParentNode().removeChild(node);
                    }
                } else {
                    // Remove the node if it doesn't exist in the JSON map
                    if (!jsonFields.containsKey(fieldWithoutPrefix)) {
                        node.getParentNode().removeChild(node);
                    }
                }
            }
        } else if (jsonElement instanceof List) {
            List<?> jsonArray = (List<?>) jsonElement;

            Set<Node> nodesToKeep = new HashSet<>();

            // Match XML nodes with JSON array elements
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);

                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    // Try to match the current child node with an element in the JSON array
                    for (Object jsonArrayElement : jsonArray) {
                        if (isMatchingNode(childNode, jsonArrayElement)) {
                            nodesToKeep.add(childNode);
                            // Recur into the matched node for deeper comparison
                            removeMissingFields(childNode.getChildNodes(), jsonArrayElement);
                            break; // Exit loop once a match is found
                        }
                    }
                }
            }

            // Remove any nodes that were not kept
            for (int j = nodeList.getLength() - 1; j >= 0; j--) {
                Node childNode = nodeList.item(j);
                if (childNode.getNodeType() != Node.TEXT_NODE && !nodesToKeep.contains(childNode)) {
                    nodeList.item(0).getParentNode().removeChild(childNode);
                }
            }
        }
    }


    // Example method to determine if a child node matches a JSON object or element
    private boolean isMatchingNode(Node childNode, Object jsonArrayElement) {
        // If the element in the JSON array is a Map, treat it as an object and check for matching attributes
        if (jsonArrayElement instanceof Map) {
            Map<String, Object> jsonObject = (Map<String, Object>) jsonArrayElement;
            String idAttribute = childNode.getLocalName() != null
                    ? childNode.getLocalName()
                    : null;

            return idAttribute != null && jsonObject.containsKey(idAttribute); // Adjust as needed
        }

        // If the element in the JSON array is a primitive type (e.g., String, Number), compare the text content
        if (jsonArrayElement instanceof String || jsonArrayElement instanceof Number) {
            return jsonArrayElement.toString().equals(childNode.getTextContent().trim());
        }

        // You can add more matching logic here as needed
        return false;
    }



    // Removes XML fields that don't exist in the JSON
//    private void removeMissingFields(NodeList nodeList, Map<String, Object> jsonFields) {
//        for (int i = nodeList.getLength() - 1; i >= 0; i--) { // Iterate backward
//            if (jsonFields == null) {
//                continue; // If jsonFields is null, skip
//            }
//
//            Node node = nodeList.item(i);
//
//            // Skip text nodes (like #text)
//            if (node.getNodeType() == Node.TEXT_NODE) {
//                continue; // Skip the text nodes
//            }
//
//            String tagName = node.getNodeName();
//            // Strip the namespace prefix for comparison
//            String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;
//
//            // If the node has child elements, recurse into the child nodes
//            if (node.hasChildNodes()) {
//                Map<String, Object> jsonChild = null;
//                try {
//                    jsonChild = (Map<String, Object>) jsonFields.get(fieldWithoutPrefix);
//                } catch (ClassCastException e) {
//                    // Check if it's an array in JSON
//                    Object jsonArray = jsonFields.get(fieldWithoutPrefix);
//                    if (jsonArray instanceof List) {
//                        List<?> list = (List<?>) jsonArray;
//
//                        // Count the number of non-text child nodes
//                        int nonTextChildCount = getNonTextChildCount(node.getChildNodes());
//
//                        // Custom logic can go here using nonTextChildCount
//                        System.out.println("Non-text child count: " + nonTextChildCount);
//
//                        NodeList childNodes = node.getChildNodes();
//                        Set<Node> nodesToKeep = new HashSet<>();
//
//                        for (Object obj : list) {
//                            if (obj instanceof Map) {
//                                Map<String, Object> jsonObject = (Map<String, Object>) obj;
//                                // Match the corresponding XML node
//                                for (int j = 0; j < childNodes.getLength(); j++) {
//                                    Node childNode = childNodes.item(j);
//                                    if (childNode.getNodeType() != Node.TEXT_NODE && isMatchingNode(childNode, jsonObject)) {
//                                        nodesToKeep.add(childNode);
//                                        removeMissingFields(childNode.getChildNodes(), jsonObject); // Recurse into the matched node
//                                    }
//                                }
//                            }
//                        }
//
//                        // Remove nodes that were not kept
//                        for (int j = childNodes.getLength() - 1; j >= 0; j--) {
//                            Node childNode = childNodes.item(j);
//                            if (childNode.getNodeType() != Node.TEXT_NODE && !nodesToKeep.contains(childNode)) {
//                                node.removeChild(childNode);
//                            }
//                        }
//                    }
//                }
//
//                // Recurse into the child nodes
//                removeMissingFields(node.getChildNodes(), jsonChild);
//            }
//
//            // If the field is not in the JSON, remove it
//            if (!jsonFields.containsKey(fieldWithoutPrefix)) {
//                node.getParentNode().removeChild(node);
//            }
//        }
//    }
//
//    private boolean isMatchingNode(Node childNode, Map<String, Object> jsonObject) {
//        // Implement your matching logic here
//        String idAttribute = childNode.getLocalName() != null
//                ? childNode.getLocalName()
//                : null;
//
//        return idAttribute != null && jsonObject.containsKey(idAttribute); // Adjust as needed
//    }
    // Helper function to count non-text child nodes
    private int getNonTextChildCount(NodeList nodeList) {
        int count = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.TEXT_NODE) {
                count++;
            }
        }
        return count;
    }


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
