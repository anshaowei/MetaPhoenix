# Ion entropy and accurate entropy-based FDR estimation in metabolomics

MetaPhoenix is a Java/Spring Boot implementation of the ion entropy and entropy-based target-decoy workflows described in our Briefings in Bioinformatics paper. The project provides tools for importing tandem MS spectral libraries, filtering spectra, generating decoy spectral libraries, and identifying query spectra with false discovery rate (FDR) control.

The underlying method introduces ion entropy as a way to quantify ion-level information in large-scale metabolomics MS/MS data. Based on this concept, MetaPhoenix implements entropy-driven decoy generation strategies designed to improve target-decoy FDR estimation for metabolite annotation, where reliable decoy construction is difficult because small-molecule spectra and structures are highly diverse.

The software supports common metabolomics spectral formats and library sources, including MSP, MGF, mzML, GNPS, HMDB, and MassBank-related workflows. It is intended for reproducible research, method evaluation, and practical spectral-library-based metabolite identification using entropy-based scoring and FDR estimation.

# Citation

If you use this project, please cite:

An S, Lu M, Wang R, Wang J, Jiang H, Xie C, Tong J, Yu C. Ion entropy and accurate entropy-based FDR estimation in metabolomics. Briefings in Bioinformatics. 2024;25(2):bbae056. doi:10.1093/bib/bbae056

```bibtex
@article{an2024ionentropy,
  title = {Ion entropy and accurate entropy-based FDR estimation in metabolomics},
  author = {An, Shaowei and Lu, Miaoshan and Wang, Ruimin and Wang, Jinyin and Jiang, Hengxuan and Xie, Cong and Tong, Junjie and Yu, Changbin},
  journal = {Briefings in Bioinformatics},
  volume = {25},
  number = {2},
  pages = {bbae056},
  year = {2024},
  doi = {10.1093/bib/bbae056}
}
```

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
