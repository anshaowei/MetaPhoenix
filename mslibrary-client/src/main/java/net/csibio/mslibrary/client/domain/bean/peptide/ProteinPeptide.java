package net.csibio.mslibrary.client.domain.bean.peptide;

import java.util.List;

public record ProteinPeptide(List<String> proteins, String peptideRef, Boolean isUnique) {
}
