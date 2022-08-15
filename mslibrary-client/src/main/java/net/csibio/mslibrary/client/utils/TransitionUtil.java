package net.csibio.mslibrary.client.utils;

import net.csibio.mslibrary.client.domain.bean.parser.model.chemistry.AminoAcid;

import java.util.List;

public class TransitionUtil {

    /**
     * @param acidList
     * @return
     */
    public static String toSequence(List<AminoAcid> acidList, boolean withUnimod) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < acidList.size(); i++) {
            AminoAcid aa = acidList.get(i);
            sb.append(aa.getName());

            if (withUnimod && aa.getModId() != null && !aa.getModId().isEmpty()) {
                sb.append("(UniMod:").append(aa.getModId()).append(")");
            }
        }

        return sb.toString();
    }

}
