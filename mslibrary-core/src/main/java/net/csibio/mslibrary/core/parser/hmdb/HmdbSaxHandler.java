package net.csibio.mslibrary.core.parser.hmdb;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HmdbSaxHandler extends DefaultHandler {

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes){

    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        // no op
    }
}
