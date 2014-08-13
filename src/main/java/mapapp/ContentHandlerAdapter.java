package mapapp;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author tbaum
 * @since 01.08.2014
 */
public class ContentHandlerAdapter implements ContentHandler {

    protected String currentValue;

    @Override public void setDocumentLocator(Locator locator) {
    }

    @Override public void startDocument() throws SAXException {
    }

    @Override public void endDocument() throws SAXException {
    }

    @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    }

    @Override public void endElement(String uri, String localName, String qName) throws SAXException {
    }

    @Override public void characters(char[] ch, int start, int length) {
        currentValue = new String(ch, start, length);
    }

    @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    @Override public void processingInstruction(String target, String data) throws SAXException {
    }

    @Override public void skippedEntity(String name) throws SAXException {
    }
}