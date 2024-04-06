package modelcounting;

import ltlparse.Formula;

import java.math.BigInteger;
import java.util.List;

public interface ModelCounter {
    BigInteger count(Formula<String> BC);
}
