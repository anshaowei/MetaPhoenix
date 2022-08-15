package net.csibio.mslibrary.client.parser.fasta;

import com.google.common.collect.Lists;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.exceptions.XException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Fasta文件的存储格式为（一行Protein信息，跟着多行该protein的完整序列）
 */
@Component("fastaParser")
public class FastaParser {

    public static final String UNIPROT = ">";
    public static final String HMDB = "HMDBP";

    /**
     * 支持解码从Uniprot或者HMDBP下载的链表
     *
     * @param path
     * @return key为fasta文件的tag行, value为对应的sequence
     */
    public HashMap<String, String> parse(String path) throws XException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = reader.readLine();
            String proteinTag = line;
            //设置初始容量为128，避免重新分配空间造成的性能损失
            StringBuilder sequence = new StringBuilder(1024);
            HashMap<String, String> map = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(UNIPROT) || line.startsWith(HMDB)) {
                    map.put(proteinTag, sequence.toString());
                    //进入下一个protein的分析
                    proteinTag = line;
                    sequence.setLength(0);
                } else {
                    sequence.append(line);
                }
            }
            map.put(proteinTag, sequence.toString());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XException(ResultCode.FASTA_FILE_FORMAT_NOT_SUPPORTED);
        }
    }

    public void uniprotFormat(ProteinDO protein) {
        StringBuilder keyBuilder = new StringBuilder(protein.getIdentifyLine());
        keyBuilder.delete(0, 1);
        String firstLine = keyBuilder.toString();
        String newString = keyBuilder.toString();
        String[] s = firstLine.split(SymbolConst.SPACE);
        String newS = StringUtils.substringAfter(firstLine, s[0]);
        String name = StringUtils.substringBefore(newS, "OS=");
        String identifier = StringUtils.substringBefore(firstLine, "OS=");
        protein.setId(identifier);
        String gn = StringUtils.substringBetween(newString, "GN=", "PE=");
        String os = StringUtils.substringBetween(firstLine, "OS=", "OX=");
        protein.setGene(gn);
        protein.setOrganism(os);
        protein.setId(s[0]);
        String[] identifiers = protein.getId().split("\\|", -1);
        if (identifiers.length == 3) {
            protein.setUniProtId(identifiers[1]);
        }
        String substringName = name.substring(1, name.length() - 1);
        protein.setNames(Lists.newArrayList(substringName));
    }

    public void hmdbFormat(ProteinDO protein) {
        String identifyLine = protein.getIdentifyLine();
        String[] identifies = identifyLine.split(SymbolConst.COMMA);
        String nameTagStr = identifies[0];
        String[] nameTagArray = nameTagStr.split(SymbolConst.SPACE);
        protein.setId(nameTagArray[0]);
        if (identifies.length > 1){
            protein.setOrganism(identifies[1]);
        }
        protein.setNames(Lists.newArrayList(nameTagStr.replace(nameTagArray[0],"").trim()));
    }

    public void hmdbFormat(GeneDO gene) {
        String identifyLine = gene.getIdentifyLine();
        String[] identifies = identifyLine.split(SymbolConst.COMMA);
        String nameTagStr = identifies[0];
        String[] nameTagArray = nameTagStr.split(SymbolConst.SPACE);
        gene.setId(nameTagArray[0]);
        if (identifies.length > 1){
            gene.setOrganism(identifies[1]);
        }
        gene.setNames(Lists.newArrayList(nameTagStr.replace(nameTagArray[0],"").trim()));
    }
}
