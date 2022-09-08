package net.csibio.mslibrary.client.constants;

import com.google.common.collect.Lists;
import net.csibio.mslibrary.client.constants.enums.ElementType;
import net.csibio.mslibrary.client.domain.bean.adduct.Adduct;
import net.csibio.mslibrary.client.domain.bean.chemical.OpElement;

import java.util.HashMap;
import java.util.List;

public class AdductConst {

    public static String ESI_Adducts = "ESIAdducts";

    public static final HashMap<String, Double> adductMap = new HashMap<String, Double>() {{
        put("[M-H]-", -1.00782503207);
        put("[M-H2O-H]-", -19.01838971207);
        put("[M+Na-2H]-", 20.97411921676);
        put("[M+Cl]-", 34.96885268);
        put("[M+K-2H]-", 36.94805661586);
        put("[M+FA-H]-", 44.99765396793);
        put("[M+Hac-H]-", 59.01330396793);
        put("[M+C2H3N+Na-2H]-", 62.00066831777);
        put("[M+Br]-", 78.9183371);
        put("[M+TFA-H]-", 112.98503896793);
        put("[M-C6H10O4-H]-", -147.06573383101);
        put("[M-C6H10O5-H]-", -163.06064845057);
        put("[M-C6H8O6-H]-", -177.03991300599);
        put("[M+CH3COONa-H]-", 80.99524996793);
        put("[2M-H]-", -1.00782503207);
        put("[2M+FA-H]-", 44.99765396793);
        put("[2M+Hac-H]-", 59.01330396793);
        put("[3M-H]-", -1.00782503207);
        put("[M-2H]2-", -2.01565006414);
        put("[M-3H]3-", -3.02347509621);
    }};

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

    public static final List<Adduct> ESIAdducts = Lists.newArrayList(
            // 正离子
            Adduct.build(1, 3, null, OpElement.build("+", 3, ElementType.H)),
            Adduct.build(1, 3, null, OpElement.build("+", 2, ElementType.H), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 3, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 2, ElementType.Na)),
            Adduct.build(1, 3, null, OpElement.build("+", 3, ElementType.Na)),
            Adduct.build(1, 2, null, OpElement.build("+", 2, ElementType.H)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.NH4)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(1, 2, null, OpElement.build("+", 1, ElementType.H), OpElement.build("+", 1, ElementType.K)),
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
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.DMSO), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.ACN), OpElement.build("+", 1, ElementType.H)),
            Adduct.build(1, 1, null, OpElement.build("+", 2, ElementType.IsoProp), OpElement.build("+", 1, ElementType.Na)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.H)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.NH4)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.Na)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.K)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.ACN), OpElement.build("+", 2, ElementType.H)),
            Adduct.build(2, 1, null, OpElement.build("+", 2, ElementType.ACN), OpElement.build("+", 2, ElementType.Na)),

            // 负离子
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
