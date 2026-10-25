# MetaPhoenix

**Ion entropy and accurate entropy-based FDR estimation in metabolomics**

[![DOI](https://img.shields.io/badge/DOI-10.1093%2Fbib%2Fbbae056-blue)](https://doi.org/10.1093/bib/bbae056)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)

MetaPhoenix is a Java/Spring Boot implementation of the ion entropy and entropy-based target-decoy workflows described in our Briefings in Bioinformatics paper. It imports tandem MS spectral libraries, filters spectra, generates decoy spectral libraries and identifies query spectra with false discovery rate (FDR) control.

The method introduces ion entropy as a way to quantify ion-level information in large-scale metabolomics MS/MS data. Based on this concept, MetaPhoenix implements entropy-driven decoy generation strategies that improve target-decoy FDR estimation for metabolite annotation, where reliable decoy construction is difficult because small-molecule spectra and structures are highly diverse.

## Branches and versions

* `dev` (default branch): MetaPhoenix 1.0.0, the maintained version. Spring Boot 4, command-line workflow, reproducible decoys (`-seed`), bug fixes and tests.
* `master`: archive of the original version of the code, published under the name MSLibrary. It is kept unchanged for reference.

MetaPhoenix was previously named MSLibrary. Databases created with MSLibrary, including the one used for the paper, can be read by MetaPhoenix without migration (see [Using databases created by MSLibrary](#using-databases-created-by-mslibrary)).

# Citation

If you use MetaPhoenix, please cite:

> An S, Lu M, Wang R, Wang J, Jiang H, Xie C, Tong J, Yu C. Ion entropy and accurate entropy-based FDR estimation in metabolomics. *Briefings in Bioinformatics*. 2024;25(2):bbae056. [doi:10.1093/bib/bbae056](https://doi.org/10.1093/bib/bbae056)

PubMed: [38426325](https://pubmed.ncbi.nlm.nih.gov/38426325/) · Free full text: [PMC10939419](https://pmc.ncbi.nlm.nih.gov/articles/PMC10939419/)

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

The citation is also available in [CITATION.cff](CITATION.cff) and through the "Cite this repository" button on GitHub.

If you use the spectral entropy similarity, please also cite: Li Y, Kind T, Folz J, et al. Spectral entropy outperforms MS/MS dot product similarity for small-molecule compound identification. *Nature Methods*. 2021;18:1524–1531. [doi:10.1038/s41592-021-01331-z](https://doi.org/10.1038/s41592-021-01331-z)

# Project structure

* `metaphoenix-client`: algorithms (ion entropy, decoy generation, similarity, identification), spectrum parsers, filters, domain classes and data access.
* `metaphoenix-core`: Spring Boot application. Configuration, the command runner and the HTTP controllers.

# Prerequisites

* Java 17 or newer (built and tested with Java 25)
* Maven 3.9 or newer
* MongoDB 4.4 or newer
* Redis: optional, only used as a cache

# Installation

## 1. Get the code

```bash
git clone https://github.com/anshaowei/MetaPhoenix.git
cd MetaPhoenix
```

## 2. Install MongoDB

Follow the [MongoDB installation guide](https://www.mongodb.com/docs/manual/installation/) and start the server. MetaPhoenix uses the database `metaphoenix` on `localhost:27017` by default.

## 3. Build

```bash
mvn -DskipTests clean package
```

Run `mvn test` to run the unit tests. They do not need MongoDB.

## 4. Configure

Default configuration lives in [application.properties](metaphoenix-core/src/main/resources/application.properties). Settings can be changed with environment variables:

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/metaphoenix` | MongoDB connection string, including the database name |
| `METAPHOENIX_REPOSITORY` | `~/metaphoenix-data` | Working directory for data files used by the HTTP API |
| `SERVER_PORT` | `8081` | HTTP port |
| `COMMAND_RUNNER_ENABLED` | `false` | Read commands from standard input (same as `--command.runner.enabled=true`) |
| `REDIS_ENABLED` | `false` | Enable the Redis cache |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_DATABASE` | `localhost`, `6379`, `1` | Redis connection, used only when `REDIS_ENABLED=true` |
| `REDIS_HEALTH_ENABLED` | `false` | Include Redis in the actuator health check |

Alternatively, copy [application-example.properties](metaphoenix-core/src/main/resources/application-example.properties) to `metaphoenix-core/src/main/resources/application-local.properties`, edit it, rebuild and start with `--spring.profiles.active=local`.

## 5. Run

The analysis workflow is driven by commands read from standard input, so start the application with the command runner enabled:

```bash
java -jar metaphoenix-core/target/metaphoenix-core-1.0.0.jar --command.runner.enabled=true
```

The usage is printed at startup. Enter one command per line. A command with missing or invalid parameters prints the usage and the next command is read. Closing the input (Ctrl-D) stops the command runner. Commands can also be piped in from a file:

```bash
java -jar metaphoenix-core/target/metaphoenix-core-1.0.0.jar --command.runner.enabled=true < commands.txt
```

Without `--command.runner.enabled=true` only the HTTP API is started and commands typed into the terminal are ignored.

# Quick start

Build a target library from MassBank-Europe, generate ion entropy–based decoys and identify the spectra in `query.mgf` at 1% FDR:

```text
import -f MassBank_NIST.msp -l massbank -t massbank
filter -l massbank
decoy -l massbank -s IonEntropyBased -seed 42
identify -f query.mgf -l massbank -fdr 0.01 -o query-report.xlsx
```

# Commands

## import

```text
import -f <library.msp> -l <library> [-t <format>]
```

Imports an MSP file into a new library named `<library>`.

| `-t` | Source | Download |
|---|---|---|
| `msp` (default) | Generic MSP (`NAME`, `PRECURSORMZ`, `PRECURSORTYPE`, `FORMULA`, `INCHIKEY`, `SMILES`, `IONMODE`, ...) | |
| `massbank` | MassBank-Europe MSP release (`MassBank_NIST.msp`) | [MassBank-data releases](https://github.com/MassBank/MassBank-data/releases) |
| `mona` | MassBank of North America (MoNA) MSP export | [MoNA downloads](https://mona.fiehnlab.ucdavis.edu/downloads) |

GNPS libraries cannot be imported from the command line yet. The HTTP endpoint `/async/gnps` imports the GNPS JSON export from `<METAPHOENIX_REPOSITORY>/ALL_GNPS.json` ([GNPS library downloads](https://external.gnps2.org/gnpslibrary)) and creates one library per GNPS `library_membership`.

The paper used GNPS (December 2023) as the target library and MoNA (July 2023) as the query set.

## filter

```text
filter -l <library>
```

Cleans a library with the rules described in the paper:

1. Remove spectra without m/z values, intensities, precursor m/z, ion mode or InChIKey.
2. Remove peaks with intensity below 1% of the base peak.
3. Remove spectra without a peak within 10 ppm of the precursor m/z.
4. Remove spectra with fewer than 5 peaks above 2% relative intensity.
5. Remove spectra with precursor m/z or exact mass above 1000.
6. Keep MS2 spectra only.
7. Keep positive ion mode spectra only.

**The library is replaced by the filtered spectra.** Re-import the file to get the unfiltered library back.

## decoy

```text
decoy -l <library> [-s <strategy>] [-ppm <ppm>] [-seed <seed>] [-overwrite]
```

Generates a decoy library, stored as `<library>-<strategy>`.

* `-s`: decoy strategy (default `IonEntropyBased`):
  * `IonEntropyBased`, `IonEntropyBased_PR`: ion entropy–based decoys proposed in the paper.
  * `SpectralEntropyBased`, `SpectralEntropyBased_PR`: spectral entropy–based decoys proposed in the paper.
  * `Naive`, `XYMeta`: existing methods used for comparison.
  * The `_PR` variants keep the precursor ion in the decoy spectrum.
* `-ppm`: m/z tolerance in ppm (default 10).
* `-seed`: random seed. With the same seed and library, the same decoy library is generated. Without it, every run gives a different decoy library.
* `-overwrite`: if the decoy library already exists, nothing is generated, so existing decoys (such as the ones used for the paper) are never replaced by accident. Add `-overwrite` to regenerate it.

The fragmentation tree–based strategy (`FragmentationTreeBased`, using SIRIUS) is not available in this version.

## identify

```text
identify -f <spectra.mgf|spectra.mzML> -l <library> -fdr <fdr> [-s <strategy>] [-ppm <ppm>] [-o <report.xlsx>]
```

Searches query spectra against a target library and its decoy library and reports the hits that pass the FDR threshold.

* `-f`: query spectra in `.mgf` or `.mzML` format. Only MS2 spectra with a precursor m/z are searched, and peaks with zero intensity are removed.
* `-l`: target library.
* `-fdr`: FDR threshold, for example `0.01` or `0.05`.
* `-s`: decoy library to estimate the FDR with (default `IonEntropyBased`). It has to be generated with `decoy` first.
* `-ppm`: precursor m/z tolerance in ppm (default 10).
* `-o`: output report (default `report.xlsx` in the working directory).

Spectra are scored with the weighted spectral entropy similarity (Li et al., 2021). If no score threshold reaches the requested FDR, no hits are reported.

The report has one row per accepted hit:

| Column | Description |
|---|---|
| `querySpectrumId` | 0-based position of the query spectrum among the searched spectra |
| `CompoundName`, `InChIKey`, `Smiles`, `PrecursorAdduct` | Annotation of the matched library spectrum |
| `PrecursorMz` | Precursor m/z of the matched library spectrum |
| `Score` | Spectral entropy similarity |
| `LibrarySpectrumId` | ID of the matched library spectrum in MongoDB |

# HTTP API

When the application is running, `metaphoenix-core` also serves an HTTP API on port 8081 for browsing libraries, spectra and compounds (`/library`, `/spectrum`, `/compound`, `/method`, `/stat`, ...). It was written for an internal web front end that is not part of this repository and is not a stable public interface.

# Using databases created by MSLibrary

Databases created with MSLibrary, including the `mslibrary` database used for the paper, can be used as they are without migration:

```bash
export MONGODB_URI=mongodb://localhost:27017/mslibrary
```

* Documents written by MSLibrary store their Java type as `_class: net.csibio.mslibrary.*`. MetaPhoenix maps these to the current `net.csibio.metaphoenix.*` classes when reading (see `LegacyTypeInformationMapper`). Documents written from now on use the new class names, and old and new documents can live in the same collection.
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

# License

MetaPhoenix is released under the [Apache License 2.0](LICENSE).

# Contact

Questions and bug reports: [GitHub Issues](https://github.com/anshaowei/MetaPhoenix/issues).
