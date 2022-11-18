package net.csibio.mslibrary.client.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Date;

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
            NodeList nodeList = document.getElementsByTagName("ms-ms");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList childNodes = node.getChildNodes();
                SpectrumDO spectrumDO = new SpectrumDO();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    //id
                    if (childNodes.item(j).getNodeName().equals("id")) {
                        spectrumDO.setSpectrumId(childNodes.item(j).getTextContent());
                    }
                    //notes
                    if(childNodes.item(j).getNodeName().equals("notes")){
                        spectrumDO.setDescription(childNodes.item(j).getTextContent());
                    }
                    //sample-concentration
                    if(childNodes.item(j).getNodeName().equals("sample-concentration")){
                    }
                    //solvent
                    if(childNodes.item(j).getNodeName().equals("solvent")){
                    }
                    //sample-mass
                    if(childNodes.item(j).getNodeName().equals("sample-mass")){
                    }
                    //sample-assessment
                    if(childNodes.item(j).getNodeName().equals("sample-assessment")){
                    }
                    //sample-source
                    if(childNodes.item(j).getNodeName().equals("sample-source")){
                    }
                    //collection-date
                    if(childNodes.item(j).getNodeName().equals("collection-date")){
                    }
                    //instrument-type
                    if(childNodes.item(j).getNodeName().equals("instrument-type")){
                        spectrumDO.setInstrument(childNodes.item(j).getTextContent());
                    }
                    //peak-counter
                    if(childNodes.item(j).getNodeName().equals("peak-counter")){
                    }
                    //created-at
                    if(childNodes.item(j).getNodeName().equals("created-at")){
                    }
                    //updated-at
                    if(childNodes.item(j).getNodeName().equals("updated-at")){
                    }
                    //mono-mass
                    if(childNodes.item(j).getNodeName().equals("mono-mass")){
                    }
                    //energy-filed
                    if(childNodes.item(j).getNodeName().equals("energy-filed")){
                    }
                    //collision-energy-level
                    if(childNodes.item(j).getNodeName().equals("collision-energy-level")){
                    }
                    //collision-energy-voltage
                    if(childNodes.item(j).getNodeName().equals("collision-energy-voltage")){
                    }
                    //ionization-mode
                    if(childNodes.item(j).getNodeName().equals("ionization-mode")){
                        spectrumDO.setIonMode(childNodes.item(j).getTextContent());
                    }
                    //sample-concentration-units
                    if(childNodes.item(j).getNodeName().equals("sample-concentration-units")){
                    }
                    //sample-mass-units
                    if(childNodes.item(j).getNodeName().equals("sample-mass-units")){
                    }
                    //predicted
                    if(childNodes.item(j).getNodeName().equals("predicted")){
                    }
                    //structure-id
                    if(childNodes.item(j).getNodeName().equals("structure-id")){
                    }
                    //splash-key
                    if(childNodes.item(j).getNodeName().equals("splash-key")){
                        spectrumDO.setSplash(childNodes.item(j).getTextContent());
                    }
                    //chromatography-type
                    if(childNodes.item(j).getNodeName().equals("chromatography-type")){
                    }
                    //analyzer-type
                    if(childNodes.item(j).getNodeName().equals("analyzer-type")){
                    }
                    //ionization-type
                    if(childNodes.item(j).getNodeName().equals("ionization-type")){
                    }
                    //charge-type
                    if(childNodes.item(j).getNodeName().equals("charge-type")){
                    }
                    //data-source
                    if(childNodes.item(j).getNodeName().equals("data-source")){
                    }
                    //data-source-id
                    if(childNodes.item(j).getNodeName().equals("data-source-id")){
                    }
                    //adduct
                    if(childNodes.item(j).getNodeName().equals("adduct")){
                        spectrumDO.setAdduct(childNodes.item(j).getTextContent());
                    }
                    //adduct-type
                    if(childNodes.item(j).getNodeName().equals("adduct-type")){
                    }
                    //adduct-mass
                    if(childNodes.item(j).getNodeName().equals("adduct-mass")){
                    }
                    //database-id
                    if(childNodes.item(j).getNodeName().equals("database-id")){
                        spectrumDO.setCompoundId(childNodes.item(j).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
