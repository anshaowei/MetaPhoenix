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
Default configuration lives in [application.properties](metaphoenix-core/src/main/resources/application.properties). Copy [application-example.properties](metaphoenix-core/src/main/resources/application-example.properties) to `metaphoenix-core/src/main/resources/application-local.properties` before packaging for local overrides, or use environment variables.

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
java -jar metaphoenix-core/target/metaphoenix-core-1.0.0.jar
```

To include local overrides:

```bash
java -jar metaphoenix-core/target/metaphoenix-core-1.0.0.jar --spring.profiles.active=local
```

The interactive command runner is disabled by default for web startup. To enable it, run the app with `--command.runner.enabled=true`.

## 5. Commands
Import a spectral library:

```import -f library.msp -l library_name```

Filter a library:

```filter -l library_name```

Generate decoys:

```decoy -l library_name```

If the decoy library already exists, nothing is generated, so existing decoys (such as the ones used for the paper) are never replaced by accident. To regenerate it, add `-overwrite`:

```decoy -l library_name -overwrite```

Identify spectra:

```identify -f spectra.mgf -l library_name -fdr 0.05```

If no score threshold reaches the requested FDR, no hits are reported.

## 6. Using databases created by earlier versions (MSLibrary)

This project was previously named MSLibrary. Databases created with earlier versions, including the `mslibrary` database used for the paper, can be used as they are without migration:

```bash
export MONGODB_URI=mongodb://localhost:27017/mslibrary
```

* Documents written by earlier versions store their Java type as `_class: net.csibio.mslibrary.*`. MetaPhoenix maps these to the current `net.csibio.metaphoenix.*` classes when reading (see `LegacyTypeInformationMapper`). Documents written from now on use the new class names, and old and new documents can live in the same collection.
* Collection names (`spectrum-<libraryId>`, `spectrum-<libraryId>-<DecoyStrategy>`, `library`, ...) are unchanged.
* Statistics are still stored under the key `MSLibraryCompoundNum` so that existing `stat` documents stay readable.
* To analyse an old database without changing it, also start with `--spring.data.mongodb.auto-index-creation=false`. Otherwise Spring creates any missing indexes at startup. Commands such as `filter` and `decoy` write to the database.
* If Redis caching is enabled, flush the cache once after upgrading. Cached entries store class names under the old package and cannot be read by the new code.

# Implementation notes: differences from the paper text

The results and figures in the paper were produced with this code. In a few places the paper's Methods text describes the computation differently from what the code does. The code is the reference: to reproduce the published figures, use the code as it is rather than re-implementing from the text. These differences do not change the paper's conclusions.

## 1. Estimated FDR formula

* Paper text: Estimated FDR = N<sub>D</sub> / N<sub>T</sub> (decoy hits above threshold / target hits above threshold).
* Code: Estimated FDR = N<sub>D</sub> / (N<sub>T</sub> + N<sub>D</sub>). This is used by `Identify.execute` and by the `STDS_FDR` column used in `Reporter.compareDecoyStrategy`. N<sub>D</sub> / N<sub>T</sub> is still exported as the `pValue` column.
* The estimated-vs-true FDR curves in Fig. 5 were computed with N<sub>D</sub> / (N<sub>T</sub> + N<sub>D</sub>), which is why all curves level off at 0.5.
* Effect: at the FDR levels used in practice (0.01–0.05) the two formulas give almost the same threshold, because N<sub>D</sub> is much smaller than N<sub>T</sub>. At higher FDR, N<sub>D</sub> / N<sub>T</sub> is larger and more conservative. Under both formulas the ion entropy–based method gives the most accurate estimate.

## 2. Fragment m/z tolerance in the ion entropy calculation

Ion entropy is computed in two matching steps:

1. Collect library spectra whose precursor m/z is within 10 ppm of the target spectrum's precursor m/z. Paper and code agree.
2. In those spectra, collect fragment ions that match the target fragment.
   * Paper text: within 10 ppm of the fragment's own m/z.
   * Code (`SpectrumGenerator.ionEntropyBased`): within 10 ppm of the precursor m/z, using the same tolerance as step 1.

For fragments much lighter than the precursor, the code's window is wider. For example, with precursor m/z 500 the window for a fragment at m/z 100 is ±0.005 Da instead of ±0.001 Da. The decoy libraries used in the paper were generated with the code's behavior.

Effect: very small. Re-generating the GNPS decoys with the paper's tolerance changes the true FDR at estimated FDR 0.05 from 0.058 to 0.055, and at 0.10 from 0.101 to 0.099. The spectral entropy distribution of the decoys is unchanged.

Note: `Reporter.ionEntropyDistributionGraph`, used for the ion entropy statistics in Fig. 1, uses the paper's tolerance (10 ppm of the fragment m/z).

## 3. Spectral entropy similarity

* Paper text: describes the unweighted entropy similarity formula and refers to the original publication for details.
* Code (`Similarity.getEntropySimilarity`): uses the weighted version from that publication (Li et al., 2021). Intensities of spectra with spectral entropy < 3 are reweighted before the similarity is computed. The unweighted version is available as `SpectrumMatchMethod.Unweighted_Entropy`.
* The 0.05 Da fragment tolerance stated in the paper is hard-coded in `Similarity.getScore`, so the `mzTolerance` argument passed to it has no effect.

## Note on low FDR thresholds

In the MassBank-MoNA vs. GNPS benchmark, the true FDR cannot go below about 1.8%. Even among hits with a perfect score of 1.0, about 1.8% have a different first InChIKey block from the query (different compounds with nearly identical spectra). As a result, an estimated FDR of 0.01 corresponds to a true FDR of about 2–3% in this benchmark. The estimate is accurate from about 0.05 upward. This is why the curves in Fig. 5 start at a true FDR of about 0.02.
