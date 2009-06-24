package com.g3d.font;


/// <summary>Represents kerning information for a character.</summary>
public class Kerning {

    private int second;
    private int amount;

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}