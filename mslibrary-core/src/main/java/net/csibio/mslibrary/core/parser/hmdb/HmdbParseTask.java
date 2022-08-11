package net.csibio.mslibrary.core.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.hmdb.*;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.service.CompoundService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class HmdbParseTask {

    @Autowired
    CompoundService compoundService;

    public void parse(InputStream stream, String libraryId) throws DocumentException, ParseException {
        List<CompoundDO> compounds = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);
        Element rootElement = document.getRootElement();
        Iterator<Element> iterator = rootElement.elementIterator();
        while (iterator.hasNext()) {
            Element main = iterator.next();
            if (main.getName().equals("metabolite")) {
                //以metabolite标签为一个迭代来读取文件信息
                CompoundDO compound = new CompoundDO();
                compound.setLibraryId(libraryId);
                HmdbInfo hmdbInfo = new HmdbInfo();
                //此迭代用以遍历两个metabolite标签之间的内容
                Iterator<Element> iter = main.elementIterator();
                while (iter.hasNext()) {
                    Element ele = iter.next();
                    String value = ele.getStringValue();
                    if (value.isEmpty()) {
                        continue;
                    }
                    switch (ele.getName()) {
                        case "name" -> compound.setName(value);
                        case "accession" -> compound.setHmdbId(value);
                        case "status" -> compound.setStatus(value);
                        case "chemical_formula" -> compound.setFormula(value);
                        case "average_molecular_weight" -> compound.setAvgMw(Double.parseDouble(value));
                        case "monisotopic_molecular_weight" -> compound.setMonoMw(Double.parseDouble(value));
                        case "smiles" -> compound.setSmiles(value);
                        case "description" -> compound.setDescription(value);
                        case "iupac_name" -> compound.setIupac(value);
                        case "traditional_iupac" -> compound.setTraditionalIupac(value);
                        case "cas_registry_number" -> compound.setCasId(value);
                        case "chemspider_id" -> compound.setChemSpiderId(value);
                        case "drugbank_id" -> compound.setDrugBankId(value);
                        case "pubchem_compound_id" -> compound.setPubChemId(value);
                        case "phenol_explorer_compound_id" -> compound.setPhenolExplorerId(value);
                        case "knapsack_id" -> compound.setKnapsackId(value);
                        case "kegg_id" -> compound.setKeggId(value);
                        case "foodb_id" -> compound.setFoodbId(value);
                        case "biocyc_id" -> compound.setBiocycId(value);
                        case "bigg_id" -> compound.setBiggId(value);
                        case "wikipedia_id" -> compound.setWikipediaId(value);
                        case "metlin_id" -> compound.setMetlinId(value);
                        case "vmh_id" -> compound.setVmhId(value);
                        case "fbonto_id" -> compound.setFbontoId(value);
                        case "synthesis_reference" -> hmdbInfo.setSynthesisReference(value);
                        case "inchi" -> compound.setInchi(value);
                        case "inchikey" -> compound.setInchikey(value);
                        case "state" -> compound.setState(value);
                        case "creation_date" ->
                                compound.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(value));
                        case "update_date" ->
                                compound.setLastModifiedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(value));
                        case "secondary_accessions" -> compound.setHmdbIds(parseList(ele));
                        case "synonyms" -> compound.setSynonyms(parseList(ele));
                        case "taxonomy" -> hmdbInfo.setTaxonomy(parseTaxonomy(ele));
                        case "ontology" -> hmdbInfo.setOntology(parseDescendantList(ele));
                        case "experimental_properties" -> hmdbInfo.setExperimentalProperties(parseProperties(ele));
                        case "predicted_properties" -> hmdbInfo.setPredictedProperties(parseProperties(ele));
                        case "spectra" -> hmdbInfo.setSpectra(parseSpectra(ele));
                        case "biological_properties" -> hmdbInfo.setBiological(parseBiological(ele));
                        case "concentrations" -> hmdbInfo.setConcentrations(parseConcentrations(ele));
                        case "diseases" -> hmdbInfo.setDiseases(parseDiseases(ele));
                        case "general_references" -> hmdbInfo.setReferences(parseReferences(ele));
                        case "protein_associations" -> hmdbInfo.setProteinAssociations(parseProteinAssociations(ele));
                    }
                }
                compound.setId(compound.getHmdbId());
                compound.setHmdbInfo(hmdbInfo);
                compound.encode();
                compounds.add(compound);
            }
        }
        compoundService.insert(compounds, libraryId);
    }

    private Taxonomy parseTaxonomy(Element taxonomyElement) {
        Iterator<Element> iter = taxonomyElement.elementIterator();
        Taxonomy taxonomy = new Taxonomy();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "description" -> taxonomy.setDescription(value);
                case "direct_parent" -> taxonomy.setDirectParent(value);
                case "kingdom" -> taxonomy.setKingdom(value);
                case "super_class" -> taxonomy.setSuperClass(value);
                case "class" -> taxonomy.setClazz(value);
                case "sub_class" -> taxonomy.setSubClass(value);
                case "molecular_framework" -> taxonomy.setMolecularFramework(value);
                case "alternative_parents" -> taxonomy.setAlterParents(parseList(ele));
                case "substituents" -> taxonomy.setSubstituents(parseList(ele));
                case "external_descriptors" -> taxonomy.setExtDesc(parseList(ele));
            }
        }
        return taxonomy;
    }

    private List<Descendant> parseDescendantList(Element ontology) {
        Iterator<Element> iter = ontology.elementIterator();
        List<Descendant> descendants = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            Descendant descendant = parseDescendant(ele);
            descendants.add(descendant);
        }
        return descendants;
    }

    private Descendant parseDescendant(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Descendant descendant = new Descendant();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "term" -> descendant.setTerm(value);
                case "definition" -> descendant.setDefinition(value);
                case "parent_id" -> descendant.setParentId(value);
                case "level" -> descendant.setLevel(Integer.parseInt(value));
                case "type" -> descendant.setType(value);
                case "synonyms" -> descendant.setSynonyms(parseList(ele));
                case "descendants" -> descendant.setDescendants(parseDescendantList(ele));
            }
        }
        return descendant;
    }

    private List<Property> parseProperties(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<Property> properties = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            Property property = parseProperty(ele);
            properties.add(property);
        }
        return properties;
    }

    private Property parseProperty(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Property property = new Property();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "kind" -> property.setKind(value);
                case "value" -> property.setValue(value);
                case "source" -> property.setSource(value);
            }
        }
        return property;
    }

    private List<SpectrumLink> parseSpectra(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<SpectrumLink> spectra = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            SpectrumLink link = parseSpectrum(ele);
            spectra.add(link);
        }
        return spectra;
    }

    private SpectrumLink parseSpectrum(Element element) {
        Iterator<Element> iter = element.elementIterator();
        SpectrumLink link = new SpectrumLink();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "type" -> link.setType(value);
                case "spectrum_id" -> link.setSpectrumId(value);
            }
        }
        return link;
    }

    private Biological parseBiological(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Biological biological = new Biological();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "cellular_locations" -> biological.setCellulars(parseList(ele));
                case "biospecimen_locations" -> biological.setBioSpecimens(parseList(ele));
                case "tissue_locations" -> biological.setTissues(parseList(ele));
                case "pathways" -> biological.setPathways(parsePathways(ele));
            }
        }
        return biological;
    }

    private List<Pathway> parsePathways(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<Pathway> pathways = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            pathways.add(parsePathway(ele));
        }
        return pathways;
    }

    private Pathway parsePathway(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Pathway pathway = new Pathway();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "name" -> pathway.setName(value);
                case "smpdb_id" -> pathway.setSmpdbId(value);
                case "kegg_map_id" -> pathway.setKeggMapId(value);
            }
        }
        return pathway;
    }

    private List<Concentration> parseConcentrations(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<Concentration> concentrations = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            concentrations.add(parseConcentration(ele));
        }
        return concentrations;
    }

    private Concentration parseConcentration(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Concentration concentration = new Concentration();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "biospecimen" -> concentration.setBiospecimen(value);
                case "concentration_value" -> concentration.setValue(value);
                case "concentration_units" -> concentration.setUnits(value);
                case "subject_age" -> concentration.setSubjectAge(value);
                case "subject_sex" -> concentration.setSubjectSex(value);
                case "patient_age" -> concentration.setPatientAge(value);
                case "patient_sex" -> concentration.setPatientSex(value);
                case "patient_information" -> concentration.setPatientInfo(value);
                case "comment" -> concentration.setComment(value);
                case "references" -> concentration.setReferences(parseReferences(ele));
            }
        }
        return concentration;
    }

    private List<Disease> parseDiseases(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<Disease> diseases = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            diseases.add(parseDisease(ele));
        }
        return diseases;
    }

    private Disease parseDisease(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Disease disease = new Disease();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "name" -> disease.setName(value);
                case "omim_id" -> disease.setOmimId(value);
                case "references" -> disease.setReferences(parseReferences(ele));
            }
        }
        return disease;
    }

    private List<Reference> parseReferences(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<Reference> references = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            references.add(parseReference(ele));
        }
        return references;
    }

    private Reference parseReference(Element element) {
        Iterator<Element> iter = element.elementIterator();
        Reference reference = new Reference();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "reference_text" -> reference.setText(value);
                case "pubmed_id" -> reference.setPubMedId(value);
            }
        }
        return reference;
    }

    private List<ProteinAssociation> parseProteinAssociations(Element element) {
        Iterator<Element> iter = element.elementIterator();
        List<ProteinAssociation> associations = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            associations.add(parseProteinAssociation(ele));
        }
        return associations;
    }

    private ProteinAssociation parseProteinAssociation(Element element) {
        Iterator<Element> iter = element.elementIterator();
        ProteinAssociation association = new ProteinAssociation();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()) {
                continue;
            }
            switch (ele.getName()) {
                case "name" -> association.setName(value);
                case "protein_accession" -> association.setProteinAccession(value);
                case "uniprot_id" -> association.setUniprotId(value);
                case "gene_name" -> association.setGeneName(value);
                case "protein_type" -> association.setProteinType(value);
            }
        }
        return association;
    }

    private List<String> parseList(Element node) {
        Iterator<Element> iter = node.elementIterator();
        List<String> list = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = iter.next();
            String value = ele.getStringValue();
            list.add(value);
        }
        return list;
    }
}
