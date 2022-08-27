package com.crazy.scientist.crazyjavascientist.satisfactory.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum Ingots {

    ALUMINUM_INGOT(0,null),
    CATERIUM_INGOT(3,Resources.CATERIUM_ORE),
    COPPER_INGOT(1,Resources.COPPER_ORE),
    IRON_INGOT(1,Resources.IRON_ORE),
    STEEL_INGOT(3,3,Resources.IRON_ORE,Resources.COAL);

    private int secondValue;


    private int value;
    private Resources resource1;
    private Resources resource2;
    Ingots(int value, Resources resource1) {
        this.value = value;
        this.resource1= resource1;
    }

     Ingots(int value, int secondValue, Resources resource1,Resources resource2) {
        this.value = value;
        this.secondValue=secondValue;
        this.resource1= resource1;
        this.resource2=resource2;
    }
}
