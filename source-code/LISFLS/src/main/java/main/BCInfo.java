package main;

import java.math.BigDecimal;

/**
 * BC的结构，表明需要记录BC的信息
 */
public class BCInfo {
    double value;
    String bc;
    BigDecimal modelCounting;
    double regularValue;
    double time;
    double localGeneralValue;

    public BCInfo(String bc) { this.bc = bc; }

    public BCInfo(double value, String bc, BigDecimal modelCounting, double regularValue, double time) {
        this.value = value;
        this.bc = bc;
        this.modelCounting = modelCounting;
        this.regularValue = regularValue;
        this.time = time;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setModelCounting(BigDecimal modelCounting) {
        this.modelCounting = modelCounting;
    }

    public void setRegularValue(double regularValue) {
        this.regularValue = regularValue;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public String getBc() {
        return bc;
    }

    public BigDecimal getModelCounting() {
        return modelCounting;
    }

    public double getRegularValue() {
        return regularValue;
    }

    public double getTime() {
        return time;
    }

    public double getLocalGeneralValue() {
        return localGeneralValue;
    }

    public void setLocalGeneralValue(double localGeneralValue) {
        this.localGeneralValue = localGeneralValue;
    }
}