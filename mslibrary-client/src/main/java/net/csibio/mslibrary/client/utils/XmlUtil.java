package net.csibio.mslibrary.client.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class XmlUtil {

    /**
     * xml to bean
     * @param xmlContent
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T xmlToBean(String xmlContent, Class<?> beanClass) {
        Reader reader = null;
        try {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            reader = new StringReader(xmlContent);
            return (T) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new RuntimeException("xml parse error.", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
