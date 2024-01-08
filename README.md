# Entropy-Based Methods for Improved False Discovery Rate Control in Metabolomics

Accurate identification of metabolites and estimation of false discovery rates (FDRs) remain ongoing challenges in metabolomics, particularly for large-scale studies. While methods from proteomics have provided a useful starting point, metabolite annotations present unique complexities. Target-decoy strategies have shown promise for FDR control, but generating reliable decoy libraries is difficult for diverse metabolite structures. There is a continued need for bioinformatics innovation to maximize information from expanding spectral databases while minimizing false identifications.

In this project, we introduce the novel concept of ion entropy for metabolomics and develop two entropy-based strategies for decoy spectral library generation. We demonstrate the utility of ion entropy for quantifying information content in large-scale tandem MS datasets. Benchmarking assessments show our entropy-decoy approaches outperform current leading methods in metabolomics for accurate FDR estimation across diverse search conditions. Analysis of 46 public metabolomics datasets provides practical guidance for real-world applications.

Overall, this work presents new entropy-driven methods to address the pressing needs for sensitive metabolite identification and rigorous FDR control in high-throughput metabolomics studies. The proposed strategies leverage emerging big data resources to improve false discovery metrics without sacrificing identification power. This project exemplifies the immense opportunities at the intersection of information theory, statistical learning, and metabolomics.

# Preprequisites

* Java 16 or higher
* MongoDB 4.4 or higher

# Usage

## 1. Download
Download the latest source code from the repository.

## 2. Install MongoDB
Follow the [MongoDB installation guide](https://www.mongodb.com/docs/) to install MongoDB on your system.

## 3. Configure
In [applications.properties](mslibrary-core/src/main/resources/application.properties), configure the MongoDB address and port.

```spring.data.mongodb.uri=mongodb://localhost:27017/mslibrary```

## 4. Run
Run the Spring Boot app from [MSLibraryApplication.java](mslibrary-core/src/main/java/net/csibio/mslibrary/core/MSLibraryApplication.java).

## 5. Commands
Import a spectral library:

```import -f library.msp -l library_name```

Filter a library:

```filter -l library_name```

Generate decoys:

```decoy -l library_name```

Identify spectra:

```identify -f spectra.mgf -l library_name -fdr 0.05```