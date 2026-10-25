package net.csibio.metaphoenix.client.constants.enums;

public enum ElementType {

    // 纯电子
    e(0.00054857990924, "e", "Electron"),
    H(1.0078250319, "H", "Hydrogen"),
    D(2.01410178, "D", "Deuterium"),
    T(3.01604927, "T", "Tritium"),
    He(3.0160293191, "He", "Helium"),
    Li(6.015122, "Li", "Lithium"),
    Be(9.0121822, "Be", "Beryllium"),
    B(10.012937, "B", "Bor"),
    C(12.0, "C", "Carbon"),
    N(14.003074, "N", "Nitrogen"),
    O(15.994915, "O", "Oxygen"),
    F(18.99840322, "F", "Fluorine"),
    Na(22.9897692809, "Na", "Sodium"),
    Mg(23.985042, "Mg", "Magnesium"),
    Al(26.98153863, "Al", "Aluminium"),
    Si(27.9769265325, "Si", "Silicon"),
    P(30.97376149, "P", "Phosphorus"),
    S(31.97207073, "S", "Sulfur"),
    Cl(34.96885268, "Cl", "Chlorine"),
    Cl37(36.96590259, "Cl37", "Chlorine"),
    Ar(35.967545106, "Ar", "Argon"),
    K(38.96370668, "K", "Potassium"),
    Ca(39.96259098, "Ca", "Calcium"),
    Ti(45.9526316, "Ti", "Titanium"),
    V(49.9471585, "V", "Vanadium"),
    Cr(49.9460442, "Cr", "Chromium"),
    Mn(54.93805, "Mn", "Manganese"),
    Fe(53.9396105, "Fe", "Ferrum"),
    Co(58.933195, "Co", "Cobalt"),
    Ni(57.935348, "Ni", "Nickel"),
    Cu(62.929601, "Cu", "Copper"),
    Zn(63.929147, "Zn", "Zinc"),
    Ga(68.9255736, "Ga", "Gallium"),
    Ge(69.9242474, "Ge", "Germanium"),
    As(74.9215965, "As", "Arsenic"),
    Se(73.9224764, "Se", "Selenium"),
    Br(78.9183371, "Br", "Bromine"),
    Rb(84.911789738, "Rb", "Rubidium"),
    Sr(83.913425, "Sr", "Strontium"),
    Zr(89.9047044, "Zr", "Zirconium"),
    Mo(91.90681, "Mo", "Molybdenum"),
    Ru(95.907598, "Ru", "Ruthenium"),
    Pd(101.905609, "Pd", "Palladium"),
    Ag(106.905093, "Ag", "Silver"),
    Cd(105.906458, "Cd", "Cadmium"),
    Sn(111.904818, "Sn", "Tin"),
    Sb(120.9038157, "Sb", "Antimony"),
    Te(119.90402, "Te", "Tellurium"),
    I(126.904473, "I", "Iodine"),
    Cs(132.905451933, "Cs", "Caesium"),
    Ba(131.9050613, "Ba", "Barium"),
    Ce(135.907172, "Ce", "Cerium"),
    Gd(151.919791, "Gd", "Gadolinium"),
    Hf(175.9414086, "Hf", "Hafnium"),
    Ta(180.9479958, "Ta", "Tantalum"),
    W(179.946704, "W", "Tungsten"),
    Pt(191.961038, "Pt", "Platinum"),
    Au(196.966551, "Au", "Gold"),
    Hg(195.965833, "Hg", "Mercury"),
    Tl(202.9723442, "Tl", "Thallium"),
    Pb(203.9730436, "Pb", "Lead"),
    Bi(208.9803987, "Bi", "Bismuth"),

    H2O(H.monoMw * 2 + O.monoMw, "H2O", "H2O"),
    NH4(N.monoMw + H.monoMw * 4, "NH4", "NH4"),
    // CH3CN
    ACN(41.026547, "ACN", "CH3CN"),
    // CH3OH
    CH3OH(C.monoMw + 4 * H.monoMw + O.monoMw, "CH3OH", "CH3OH"),
    IsoProp(60.058064, "IsoProp", "C3H8O"),
    DMSO(78.013944, "DMSO", "C2H6OS"),
    // Formic acid甲酸
    FA(46.005477, "FA", "H2CO2"),
    // 以下三个均表示乙酸,CH3COOH乙酸
    AA(60.021127, "AA", "CH3COOH"),
    HAC(60.021127, "HAC", "CH3COOH"),
    HOAC(60.021127, "HOAC", "CH3COOH"),

    // 三氟乙酸
    TFA(113.992862, "TFA", "CF3COOH"),
    Form(44.9971, "Form", "Form"),
    ;

    private final Double monoMw;
    private final String symbol;
    private final String name;

    ElementType(Double monoMw, String symbol, String name) {
        this.monoMw = monoMw;
        this.symbol = symbol;
        this.name = name;
    }

    public static ElementType getBySymbol(String symbol) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].getSymbol().equals(symbol)) {
                return values()[i];
            }
        }
        return null;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getMonoMw() {
        return monoMw;
    }

    public String getName() {
        return name;
    }
}
