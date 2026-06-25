# Entropy-Based Methods for Improved False Discovery Rate Control in Metabolomics

Accurate identification of metabolites and estimation of false discovery rates (FDRs) remain ongoing challenges in metabolomics, particularly for large-scale studies. While methods from proteomics have provided a useful starting point, metabolite annotations present unique complexities. Target-decoy strategies have shown promise for FDR control, but generating reliable decoy libraries is difficult for diverse metabolite structures. There is a continued need for bioinformatics innovation to maximize information from expanding spectral databases while minimizing false identifications.

In this project, we introduce the novel concept of ion entropy for metabolomics and develop two entropy-based strategies for decoy spectral library generation. We demonstrate the utility of ion entropy for quantifying information content in large-scale tandem MS datasets. Benchmarking assessments show our entropy-decoy approaches outperform current leading methods in metabolomics for accurate FDR estimation across diverse search conditions. Analysis of 46 public metabolomics datasets provides practical guidance for real-world applications.

Overall, this work presents new entropy-driven methods to address the pressing needs for sensitive metabolite identification and rigorous FDR control in high-throughput metabolomics studies. The proposed strategies leverage emerging big data resources to improve false discovery metrics without sacrificing identification power. This project exemplifies the immense opportunities at the intersection of information theory, statistical learning, and metabolomics.

# Prerequisites

* Java 17-21
* Maven 3.9 or higher
* MongoDB 4.4 or higher
* Redis is optional for the current web/API workflow
* Sirius is optional and only required for Sirius-based decoy generation

# Usage

## 1. Download
Download the latest source code from the repository.

## 2. Install MongoDB
Follow the [MongoDB installation guide](https://www.mongodb.com/docs/) to install MongoDB on your system.

## 3. Configure
Default configuration lives in [application.properties](mslibrary-core/src/main/resources/application.properties). Copy [application-example.properties](mslibrary-core/src/main/resources/application-example.properties) to `mslibrary-core/src/main/resources/application-local.properties` before packaging for local overrides, or use environment variables.

Common overrides:

```bash
export MONGODB_URI=mongodb://localhost:27017/metaphoenix
export METAPHOENIX_REPOSITORY=$HOME/metaphoenix-data
export SIRIUS_PATH=/Applications/sirius.app/Contents/MacOS/sirius
export SIRIUS_PROJECT_SPACE=$HOME/metaphoenix-data/sirius
```

Enable Redis integration only when a local or remote Redis service is available:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_HEALTH_ENABLED=true
```

## 4. Run
Build the project first:

```bash
mvn -DskipTests clean package
```

Run the Spring Boot app:

```bash
java -jar mslibrary-core/target/mslibrary-core-1.0.0.jar
```

To include local overrides:

```bash
java -jar mslibrary-core/target/mslibrary-core-1.0.0.jar --spring.profiles.active=local
```

The interactive command runner is disabled by default for web startup. To enable it, run the app with `--command.runner.enabled=true`.

## 5. Commands
Import a spectral library:

```import -f library.msp -l library_name```

Filter a library:

```filter -l library_name```

Generate decoys:

```decoy -l library_name```

Identify spectra:

```identify -f spectra.mgf -l library_name -fdr 0.05```
