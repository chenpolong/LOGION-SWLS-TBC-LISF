package localsearch;


import ltlparse.Formula;
import org.jamesframework.core.exceptions.SearchException;

import java.util.ArrayList;
import java.util.List;

public final class BCFormulaOperator {
    private Formula<String> formula;

    private List<FormulaNode> nodes = new ArrayList<>();

    public class FormulaNode {
        public Formula<String> f;
        public int fatherIndex;
        public int leftIndex;
        public int rightIndex;

        public FormulaNode(Formula<String> f, int fatherIndex, int leftIndex, int rightIndex) {
            this.f = f;
            this.fatherIndex = fatherIndex;
            this.leftIndex = leftIndex;
            this.rightIndex = rightIndex;
        }

        @Override
        public String toString() {
            return "FormulaNode{" +
                    "f=" + f +
                    ", fatherIndex=" + fatherIndex +
                    ", leftIndex=" + leftIndex +
                    ", rightIndex=" + rightIndex +
                    '}';
        }
    }

    public BCFormulaOperator(Formula<String> formula) {
        this.formula = formula;
        generateNodesRef();
    }

    public List<FormulaNode> getNodesRef() {
        return nodes;
    }

    private void generateNodesRef() {
        nodes.clear();
        nodes.add(new FormulaNode(formula, -2, -1, -1));

        for (int i = 0; i < nodes.size(); i++) {
            FormulaNode curNode = nodes.get(i);
            Formula<String> curFormula = curNode.f;
            // add left and right formula
            Formula<String> left = curFormula.getSub1();
            if (left != null) {
                nodes.add(new FormulaNode(left, i, -1, -1));
                curNode.leftIndex = nodes.size()-1;
            }
            Formula<String> right = curFormula.getSub2();
            if (right != null) {
                nodes.add(new FormulaNode(right, i, -1, -1));
                curNode.rightIndex = nodes.size()-1;
            }
        }
    }

    public Formula<String> addOperator(int level, Formula.Content newContent, Formula<String> newFormula, Boolean addToRight) {
        Formula<String> ret = null;
        switch (newContent) {
            case NOT:
            case NEXT:
            case GLOBALLY:
            case FUTURE:
                ret = addUnaryExpr(level, newContent);
                break;
            case AND:
            case OR:
            case IFF:
            case UNTIL:
            case WEAK_UNTIL:
            case RELEASE:
                if (newFormula == null) { throw new SearchException("add binary operator but no second formula: current formula: " + nodes.get(level).f +
                        ", add Operator: " + newContent + ", second formula: null"); }
                ret = addBinaryExpr(level, newContent, newFormula, addToRight);
                break;
            default:
                assert false: formula + " add operator failed: " + "level=" + level + ", newContent=" + newContent + ", newFormula" + newFormula;
        }
        return ret;
    }

    /**
     *
     * @param level         不能是 文字
     * @param newContent
     * @param newFormula    一元公式 -> 二元公式 用到
     * @param deleteRight   二元公式 -> 一元公式 用到
     */
    public Formula<String> changeOperator(int level, Formula.Content newContent, Formula<String> newFormula, Boolean deleteRight) {
        Formula<String> ret = null;
        if (nodes.get(level).f.isLiteral()) {
            assert false: "change Operator, formula can not be literal, formula=" + nodes.get(level).f;
        }
        switch (newContent) {
            case NOT:
            case NEXT:
            case GLOBALLY:
            case FUTURE:
                ret = changeUnaryExpr(level, newContent, deleteRight);
                break;
            case AND:
            case OR:
            case IFF:
            case UNTIL:
            case WEAK_UNTIL:
            case RELEASE:
                ret = changeBinaryExpr(level, newContent, newFormula);
                break;
            default:
                assert false: formula + " change operator failed: " + "level=" + level + ", newContent=" + newContent + ", newFormula" + newFormula;
        }
        return ret;
    }

    public Formula<String> changeLiteral(int level, Formula<String> literal) {
        if (!nodes.get(level).f.isLiteral()) {
            assert false: "change literal, formula must be literal, formula=" + nodes.get(level).f;
        }
        if (literal == null) {
            assert false: "change literal: new literal is null, current formula=" + nodes.get(level).f;
        }
        return changeFormula(level, literal);
    }

    public Formula<String> deleteOperator(int level, Boolean deleteRight) {
        Formula<String> oldFormula = nodes.get(level).f;
        if (oldFormula.isLiteral()) {
            assert false: "delete operator, formula can not be literal, deleted formula=" + oldFormula + ", level=" + level
                    + ", current formula: " + formula;
        }

        Formula<String> newFormula = (null == oldFormula.getSub2() || deleteRight) ? oldFormula.getSub1() : oldFormula.getSub2();

        if (newFormula == null) {
            assert false: "delete operator, new formula is null, formula=" + oldFormula;
        }

        return changeFormula(level, newFormula);
    }

    private Formula<String> addUnaryExpr(int level, Formula.Content newContent) {
        Formula<String> newFormula = buildFormula(nodes.get(level).f, null, newContent);
        return changeFormula(level, newFormula);
    }

    private Formula<String> addBinaryExpr(int level, Formula.Content newContent, Formula<String> newSubFormula, Boolean addToRight) {
        Formula<String> newFormula = addToRight ?
                buildFormula(nodes.get(level).f, newSubFormula, newContent) :
                buildFormula(newSubFormula, nodes.get(level).f, newContent);
        return changeFormula(level, newFormula);
    }

    private Formula<String> changeUnaryExpr(int level, Formula.Content newContent, Boolean deleteRigth) {
        Formula<String> oldFormula = nodes.get(level).f;
        Formula<String> subFormula = (null == oldFormula.getSub2() || deleteRigth) ? oldFormula.getSub1() : oldFormula.getSub2();
        Formula<String> newFormula = buildFormula(subFormula, null, newContent);
        return changeFormula(level, newFormula);
    }

    private Formula<String> changeBinaryExpr(int level, Formula.Content newContent, Formula<String> newSubFormula) {
        Formula<String> oldFormula = nodes.get(level).f;
        Formula<String> rightFormula = null == oldFormula.getSub2() ? newSubFormula : oldFormula.getSub2();

        if (rightFormula == null) {
            assert false : "changeBinaryExpr failed, need right formula, newContent=" + newContent +
                    ", oldFormula=" + oldFormula + ", newSubFormula=" + newSubFormula;
        }

        Formula<String> newFormula = buildFormula(oldFormula.getSub1(), rightFormula, newContent);

        return changeFormula(level, newFormula);
    }

    private Formula<String> changeFormula(int level, Formula<String> newFormula) {
        if (level == 0) {
            return newFormula;
        } else {
            FormulaNode fatherNode = nodes.get(nodes.get(level).fatherIndex);
            Formula<String> father = fatherNode.f;
            if (fatherNode.leftIndex == level) {
                father.addSub1(newFormula);
            } else {
                father.addSub2(newFormula);
            }
            return formula;
        }
    }

    private Formula<String> buildFormula(Formula<String> leftFormula, Formula<String> rightFormula, Formula.Content content) {
        Formula<String> newFormula = null;
        switch (content) {
            case NOT:
                newFormula = Formula.Not(leftFormula);
                break;
            case NEXT:
                newFormula = Formula.Next(leftFormula);
                break;
            case GLOBALLY:
                newFormula = Formula.Always(leftFormula);
                break;
            case FUTURE:
                newFormula = Formula.Eventually(leftFormula);
                break;
            /**
             * Binary Operator
             */
            case AND:
                newFormula = Formula.And(leftFormula, rightFormula);
                break;
            case OR:
                newFormula = Formula.Or(leftFormula, rightFormula);
                break;
            case IFF:
                newFormula = Formula.Iff(leftFormula, rightFormula);
                break;
            case UNTIL:
                newFormula = Formula.Until(leftFormula, rightFormula);
                break;
            case WEAK_UNTIL:
                newFormula = Formula.WUntil(leftFormula, rightFormula);
                break;
            case RELEASE:
                newFormula = Formula.Release(leftFormula, rightFormula);
                break;
            default:
                assert false: " buildFormula operator failed: " + "leftFormula=" + leftFormula + ", rightFormula=" + rightFormula + ", content=" + content;
        }
        return newFormula;
    }
}

