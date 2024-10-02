package com.example.testxml.service;

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
public class XmlJsonService {

    // Parses the XML string
    private Document parseXml(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // To retain prefixes
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlString)));
    }

    // Parses the JSON string
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Map.class);
    }

    // Updates XML based on JSON fields
    private void updateXml(Document xmlDocument, Map<String, Object> jsonFields) {
        org.w3c.dom.NodeList nodeList = xmlDocument.getDocumentElement().getChildNodes();
        removeMissingFields(nodeList, jsonFields);
    }

    @SuppressWarnings("unchecked")
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
                            //
                            String tagName = childNode.getNodeName();
                            String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;
                            Map<String, Object> jsonFields = (Map<String, Object>) jsonArrayElement;
                            Object jsonChildElement = jsonFields.get(fieldWithoutPrefix);
                            removeMissingFields(childNode.getChildNodes(), jsonChildElement);
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
    @SuppressWarnings("unchecked")
    private boolean isMatchingNode(Node childNode, Object jsonArrayElement) {
        // If the element in the JSON array is a Map, treat it as an object and check
        // for matching attributes
        if (jsonArrayElement instanceof Map) {
            Map<String, Object> jsonObject = (Map<String, Object>) jsonArrayElement;
            String idAttribute = childNode.getLocalName() != null
                    ? childNode.getLocalName()
                    : null;

            return idAttribute != null && jsonObject.containsKey(idAttribute); // Adjust as needed
        }

        // If the element in the JSON array is a primitive type (e.g., String, Number),
        // compare the text content
        if (jsonArrayElement instanceof String || jsonArrayElement instanceof Number) {
            return jsonArrayElement.toString().equals(childNode.getTextContent().trim());
        }

        // You can add more matching logic here as needed
        return false;
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
