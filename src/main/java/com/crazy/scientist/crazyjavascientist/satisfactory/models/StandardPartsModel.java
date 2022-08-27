package com.crazy.scientist.crazyjavascientist.satisfactory.models;

import com.crazy.scientist.crazyjavascientist.satisfactory.enums.StandardParts;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StandardPartsModel {

    private int amount;
    private StandardParts standardParts;
}
