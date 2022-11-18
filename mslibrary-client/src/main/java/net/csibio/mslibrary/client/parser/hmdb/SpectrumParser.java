package net.csibio.mslibrary.client.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

@Component
@Slf4j
public class SpectrumParser {

    @Autowired
    SpectrumService spectrumService;

    /**
     * scan all the xml files in the directory and parse them
     *
     * @param directoryPath
     */
    public void parse(String directoryPath) {
        log.info("开始执行文件夹:" + directoryPath + "谱图解析");
        File file = new File(directoryPath);
        File[] files = file.listFiles();
        assert files != null;
        //search all .xml files
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                log.info("开始解析文件:" + f.getName());
                parseSingleXML(f.getAbsolutePath());
            }
        }
    }

    /**
     * parse single xml file to a spectrum
     *
     * @param filePath
     */
    public void parseSingleXML(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            NodeList nodeList = document.getElementsByTagName("spectrum");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
