package net.csibio.metaphoenix.client.constants.enums;

public enum Operator {

    POSITIVE("+"),  //正
    NEGATIVE("-"),  //负

    ;

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
