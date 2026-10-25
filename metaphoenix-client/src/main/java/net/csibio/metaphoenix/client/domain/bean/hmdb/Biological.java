package net.csibio.metaphoenix.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class Biological {
    List<String> cellulars;
    List<String> bioSpecimens;
    List<String> tissues;
    List<Pathway> pathways;
}
