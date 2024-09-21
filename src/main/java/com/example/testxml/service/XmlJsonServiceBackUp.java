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
public class XmlJsonServiceBackUp {

    // Phân tích chuỗi XML
    private Document parseXml(String xmlString) throws Exception {
        // Tạo DocumentBuilderFactory để xử lý XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Để giữ lại các namespace và prefix
        // Trả về Document sau khi phân tích XML
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlString)));
    }

    // Phân tích chuỗi JSON
    private Map<String, Object> parseJson(String jsonString) throws Exception {
        // Tạo ObjectMapper để đọc JSON
        ObjectMapper objectMapper = new ObjectMapper();
        // Đọc và chuyển đổi chuỗi JSON thành Map
        return objectMapper.readValue(jsonString, Map.class);
    }

    // Cập nhật XML dựa trên các trường JSON
    private void updateXml(Document xmlDocument, Map<String, Object> jsonFields) {
        // Lấy tất cả các node con của root trong tài liệu XML
        org.w3c.dom.NodeList nodeList = xmlDocument.getDocumentElement().getChildNodes();
        // Loại bỏ các trường không có trong JSON từ XML
        removeMissingFields(nodeList, jsonFields);
    }

    // Loại bỏ các trường không có trong JSON từ danh sách node XML
    private void removeMissingFields(NodeList nodeList, Object jsonElement) {
        // Nếu JSON là một Map, kiểm tra các key và node XML tương ứng
        if (jsonElement instanceof Map) {
            Map<String, Object> jsonFields = (Map<String, Object>) jsonElement;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                // Bỏ qua các node kiểu văn bản (như #text)
                if (node.getNodeType() == Node.TEXT_NODE) {
                    continue;
                }

                String tagName = node.getNodeName();
                // Xóa prefix nếu có trong tagName
                String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;

                // Nếu node có node con, tiếp tục kiểm tra đệ quy
                if (node.hasChildNodes()) {
                    Object jsonChildElement = jsonFields.get(fieldWithoutPrefix);

                    if (jsonChildElement != null) {
                        // Gọi lại đệ quy để xử lý các cấu trúc lồng nhau
                        removeMissingFields(node.getChildNodes(), jsonChildElement);
                    } else {
                        // Nếu trường không tồn tại trong JSON, xóa node
                        node.getParentNode().removeChild(node);
                    }
                } else {
                    // Xóa node nếu không tồn tại trong JSON map
                    if (!jsonFields.containsKey(fieldWithoutPrefix)) {
                        node.getParentNode().removeChild(node);
                    }
                }
            }
        } else if (jsonElement instanceof List) {
            // Nếu JSON là một mảng
            List<?> jsonArray = (List<?>) jsonElement;

            Set<Node> nodesToKeep = new HashSet<>();

            // Duyệt các node trong XML và tìm kiếm sự khớp với mảng JSON
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);

                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    // Thử khớp node con với phần tử trong mảng JSON
                    for (Object jsonArrayElement : jsonArray) {
                        if (isMatchingNode(childNode, jsonArrayElement)) {
                            nodesToKeep.add(childNode);
                            String tagName = childNode.getNodeName();
                            String fieldWithoutPrefix = tagName.contains(":") ? tagName.split(":")[1] : tagName;
                            Map<String, Object> jsonFields = (Map<String, Object>) jsonArrayElement;
                            Object jsonChildElement = jsonFields.get(fieldWithoutPrefix);
                            // Đệ quy để xử lý các cấu trúc lồng nhau
                            removeMissingFields(childNode.getChildNodes(), jsonChildElement);
                            break; // Thoát vòng lặp khi tìm được khớp
                        }
                    }
                }
            }

            // Xóa các node không được giữ lại
            for (int j = nodeList.getLength() - 1; j >= 0; j--) {
                Node childNode = nodeList.item(j);
                if (childNode.getNodeType() != Node.TEXT_NODE && !nodesToKeep.contains(childNode)) {
                    nodeList.item(0).getParentNode().removeChild(childNode);
                }
            }
        }
    }

    // Hàm kiểm tra xem node con có khớp với phần tử JSON hay không
    private boolean isMatchingNode(Node childNode, Object jsonArrayElement) {
        // Nếu phần tử trong mảng JSON là một Map, kiểm tra các thuộc tính khớp
        if (jsonArrayElement instanceof Map) {
            Map<String, Object> jsonObject = (Map<String, Object>) jsonArrayElement;
            String idAttribute = childNode.getLocalName() != null
                    ? childNode.getLocalName()
                    : null;

            return idAttribute != null && jsonObject.containsKey(idAttribute); // Điều chỉnh nếu cần
        }

        // Nếu phần tử JSON là kiểu cơ bản (String, Number), so sánh nội dung văn bản
        if (jsonArrayElement instanceof String || jsonArrayElement instanceof Number) {
            return jsonArrayElement.toString().equals(childNode.getTextContent().trim());
        }

        // Có thể thêm các logic so khớp khác nếu cần
        return false;
    }

    // Chuyển đổi Document thành chuỗi XML
    private String convertDocumentToString(Document doc) throws Exception {
        StringWriter writer = new StringWriter();
        // Biến đổi Document thành chuỗi XML
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    // Phương thức công khai để xử lý chuyển đổi XML dựa trên JSON
    public String transformXmlBasedOnJson(String xmlString, String jsonString) throws Exception {
        // Phân tích chuỗi XML
        Document xmlDocument = parseXml(xmlString);
        // Phân tích chuỗi JSON
        Map<String, Object> jsonFields = parseJson(jsonString);
        // Cập nhật XML dựa trên JSON
        updateXml(xmlDocument, jsonFields);
        // Chuyển đổi lại Document thành chuỗi XML
        return convertDocumentToString(xmlDocument);
    }

}
