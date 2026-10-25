package net.csibio.metaphoenix.client.constants;

import com.google.common.collect.Lists;
import net.csibio.metaphoenix.client.constants.enums.ElementType;
import net.csibio.metaphoenix.client.domain.bean.adduct.Adduct;
import net.csibio.metaphoenix.client.domain.bean.chemical.OpElement;

import java.util.List;

/**
 * Adduct列表参数参照链接
 * https://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator
 */
public class AdductConst {
    public static final List<Adduct> adductProbabilitiesList = Lists.newArrayList(
            Adduct.build(3, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(4, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(5, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(3, 1, 1d, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(4, 1, 1d, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(5, 1, 1d, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 1, 100d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, -1, 10d),
            Adduct.build(2, 1, 50d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, 90d, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(2, 1, 25d, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 1, 10d, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(2, 1, 5d, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(1, -1, 100d, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(4, 1, 1d, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(2, -1, 50d, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, 1, 99d, OpElement.build("+", 1, ElementType.Form)),
            Adduct.build(1, 0, 80d, OpElement.build("-", 1, ElementType.H2O)),
            Adduct.build(5, 1, 1d, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(3, -1, 1d, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(4, -1, 1d, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(5, -1, 1d, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(2, 1, 50d, OpElement.build("+", 1, ElementType.Na), OpElement.build("-", 2, ElementType.H)),
            Adduct.build(6, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(7, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(8, 1, 1d, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, 1d, OpElement.build("+", 1, ElementType.H), OpElement.build("-", 1, ElementType.H2O)),
            Adduct.build(1, 1, 12d, OpElement.build("+", 1, ElementType.Cl)),
            Adduct.build(1, 1, 4d, OpElement.build("+", 1, ElementType.Cl37)),
            Adduct.build(1, 1, 1d, OpElement.build("+", 1, ElementType.NH4))
    );

    // 正离子
    public static final List<Adduct> ESIAdducts_Positive = Lists.newArrayList(
            Adduct.build(1, 3, null, OpElement.build("+", 3, ElementType.H)),
            Adduct.build(1, 3, null, OpElement.build("+", 2, ElementType.H), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 3, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 2, ElementType.Na)),
            Adduct.build(1, 3, null, OpElement.build("+", 3, ElementType.Na)),
            Adduct.build(1, 2, null, OpElement.build("+", 2, ElementType.H)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.NH4)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.K)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.ACN), OpElement.build("+", 2, ElementType.H)),
            Adduct.build(1, 2, null, OpElement.build("+", 2, ElementType.Na)),
            Adduct.build(1, 2, null, OpElement.build("+", 2, ElementType.ACN), OpElement.build("+", 2, ElementType.H)),
            Adduct.build(1, 2, null, OpElement.build("+", 3, ElementType.ACN), OpElement.build("+", 2, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.NH4)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.CH3OH), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.ACN), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.Na), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.IsoProp), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.ACN), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.K), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.DMSO), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.ACN), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 1, ElementType.IsoProp), OpElement.build("+", 1, ElementType.Na), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.H)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.NH4)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.K)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.ACN), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(2, 1, null, OpElement.build("+", 1, ElementType.ACN), OpElement.build("+", 1, ElementType.Na))
    );

    // 负离子
    public static final List<Adduct> ESIAdducts_Negative = Lists.newArrayList(
            Adduct.build(1, -3, null, OpElement.build("-", 3, ElementType.H)),
            Adduct.build(1, -2, null, OpElement.build("-", 2, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("-", 1, ElementType.H2O), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.Na), OpElement.build("-", 2, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.Cl)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.K), OpElement.build("-", 2, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.FA), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.AA), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.Br)),
            Adduct.build(1, -1, null, OpElement.build("+", 1, ElementType.TFA), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(2, -1, null, OpElement.build("-", 1, ElementType.H)),
            Adduct.build(2, -1, null, OpElement.build("+", 1, ElementType.FA), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(2, -1, null, OpElement.build("+", 1, ElementType.AA), OpElement.build("-", 1, ElementType.H)),
            Adduct.build(3, -1, null, OpElement.build("-", 1, ElementType.H))
    );
}
