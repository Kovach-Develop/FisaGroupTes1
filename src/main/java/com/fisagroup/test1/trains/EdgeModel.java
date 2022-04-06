package com.fisagroup.test1.trains;

import lombok.Data;

@Data
public class EdgeModel {
    private char startPoint;
    private char endPoint;
    private int length;

    public EdgeModel(char startPoint, char endPoint, int length) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.length = length;
    }

}
