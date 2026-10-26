# Implementation notes: differences from the paper text

The results and figures in the paper ([An et al., 2024](https://doi.org/10.1093/bib/bbae056)) were produced with this code. In a few places the paper's Methods text describes the computation differently from what the code does. The code is the reference: to reproduce the published figures, use the code as it is rather than re-implementing from the text. These differences do not change the paper's conclusions.

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
* Code (`Similarity.getEntropySimilarity`): uses the weighted version from that publication ([Li et al., 2021](https://doi.org/10.1038/s41592-021-01331-z)). Intensities of spectra with spectral entropy < 3 are reweighted before the similarity is computed. The unweighted version is available as `SpectrumMatchMethod.Unweighted_Entropy`.
* The 0.05 Da fragment tolerance stated in the paper is hard-coded in `Similarity.getScore`, so the `mzTolerance` argument passed to it has no effect.

## Note on low FDR thresholds

In the MassBank-MoNA vs. GNPS benchmark, the true FDR cannot go below about 1.8%. Even among hits with a perfect score of 1.0, about 1.8% have a different first InChIKey block from the query (different compounds with nearly identical spectra). As a result, an estimated FDR of 0.01 corresponds to a true FDR of about 2–3% in this benchmark. The estimate is accurate from about 0.05 upward. This is why the curves in Fig. 5 start at a true FDR of about 0.02.
