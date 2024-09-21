package com.example.testxml.controller;

import com.example.testxml.XmlJsonTransformerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transform")
public class XmlJsonController {

    @Autowired
    private XmlJsonTransformerService transformerService;

    @PostMapping(value = "/xml-to-json", produces = MediaType.TEXT_XML_VALUE)
    public String transformXml(@RequestBody TransformationRequest request) throws Exception {

        String XMLString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soapenv:Header xmlns:dan=\"http://dancuquocgia.bca\"/>\n" +
                "    <soapenv:Body xmlns:dan=\"http://dancuquocgia.bca\">\n" +
                "        <ns1:CongdanCollection xmlns:ns1=\"http://www.mic.gov.vn/dancu/1.0\">\n" +
                "            <ns1:CongDan>\n" +
                "                <ns1:SoDinhDanh>100000010836</ns1:SoDinhDanh>\n" +
                "                <ns1:SoCMND>222222222</ns1:SoCMND>\n" +
                "                <ns1:HoVaTen>\n" +
                "                    <ns1:Ten>\n" +
                "                        <ns1:Age>\n" +
                "                            <ns1:Age1>123</ns1:Age1>\n" +
                "                            <ns1:Age2>123</ns1:Age2>\n" +
                "                        </ns1:Age>\n" +
                "                        <ns1:Ho>123</ns1:Ho>\n" +
                "                        <ns1:Ho1>123</ns1:Ho1>\n" +
                "                    </ns1:Ten>\n" +
                "                    <ns1:Ten>\n" +
                "                        <ns1:Age>\n" +
                "                            <ns1:Age1>123</ns1:Age1>\n" +
                "                            <ns1:Age2>123</ns1:Age2>\n" +
                "                        </ns1:Age>\n" +
                "                        <ns1:Ho>123</ns1:Ho>\n" +
                "                        <ns1:Ho1>123</ns1:Ho1>\n" +
                "                    </ns1:Ten>\n" +
                "                    <ns1:Tuoi>123</ns1:Tuoi>\n" +
                "                    <ns1:Tuoi1>123</ns1:Tuoi1>\n" +
                "                </ns1:HoVaTen>\n" +
                "            </ns1:CongDan>\n" +
                "        </ns1:CongdanCollection>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        String JsonString = "{\n" +
                "    \"Header\": \"\",\n" +
                "    \"Body\": {\n" +
                "        \"CongdanCollection\": {\n" +
                "            \"CongDan\": {\n" +
                "                \"SoDinhDanh\": \"100000010836\",\n" +
                "                \"SoCMND\": \"222222222\",\n" +
                "                \"HoVaTen\": {\n" +
                "                    \"Ten\": [\n" +
                "                        {\n" +
                "                            \"Age\": {\n" +
                "                                \"Age1\": \"123\"\n" +
                "                            },\n" +
                "                            \"Ho1\": \"123\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"Tuoi1\": \"123\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        return transformerService.transformXmlBasedOnJson(XMLString, JsonString);
    }

    // Simple request class to wrap the XML and JSON input
    public static class TransformationRequest {
        private String xmlString;
        private String jsonString;

        public String getXmlString() {
            return xmlString;
        }

        public void setXmlString(String xmlString) {
            this.xmlString = xmlString;
        }

        public String getJsonString() {
            return jsonString;
        }

        public void setJsonString(String jsonString) {
            this.jsonString = jsonString;
        }
    }
}

