package com.takisoft.talkr.data;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {

    SAXParserFactory factory;
    SAXParser saxParser;
    XMLParserListener listener;
    PageData currentPage;
    String currentTag;

    public XMLParser(XMLParserListener listener) {
        try {
            factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
            this.listener = listener;

        } catch (ParserConfigurationException | SAXException e) {
            throw new Error("SAX parser cannot be instantiated!" + e);
        }
    }

    public void parse(File file) {
        try {
            System.out.println("saxParser: " + saxParser + " | file: " + file);
            saxParser.parse(file, this);
        } catch (SAXException | IOException ex) {
            throw new Error("SAX parser error: " + ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case PageData.TAG_PAGE:
                currentPage = new PageData();
                break;
            default:
                currentTag = qName;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentTag != null) {
            String data = new String(ch, start, length);
            switch (currentTag) {
                case PageData.TAG_PAGE_TITLE:
                    currentPage.setTitle(data);
                    currentTag = null;
                    break;
                case PageData.TAG_PAGE_NS:
                    currentPage.setNamespace(data);
                    currentTag = null;
                    break;
                case PageData.TAG_PAGE_REVISION_TEXT:
                    currentPage.setText(data);
                    currentTag = null;
                    break;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(PageData.TAG_PAGE)) {
            if (currentPage.checkText()) {
                listener.onPageDataAvailable(currentPage);
            } else {
                currentPage = null;
            }
        } else if (qName.equals(currentTag)) {
            currentTag = null;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        listener.onXMLParsingFinished();
        //System.out.println("### END OF XML ###");
    }

    public static interface XMLParserListener {

        public abstract void onPageDataAvailable(PageData page);
        public void onXMLParsingFinished();
    }
}