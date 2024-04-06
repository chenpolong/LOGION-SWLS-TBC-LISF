package localsearch;

import ltlparse.Formula;
import org.jamesframework.core.exceptions.SearchException;
import org.jamesframework.core.search.neigh.Move;
import java.util.List;
import java.util.Objects;

public final class BCMove implements Move<BCSolution> {
    private MoveOperator operator;

    /**
     * EXCHANGE, ADD 才有内容，新的操作数
     */
    private Formula.Content newContent;

    /**
     * ADD 才有内容, 第二操作数
     */
    private Formula<String> newListeral = null;

    /**
     * 在 公式的哪一层上 Move
     */
    private int level;

    private Boolean isRight;

    private BCSolution innerBCSolution;

//    private Formula<String> oldFormula = null;
//    private Formula<String> newFormula = null;

    static public enum MoveOperator {
        EXCHANGE,   // 交换算子 or 文字
        DELETE,     // 删除算子和对应的子树
        ADD         // 增加一个算子，如果加入二元运算符，第二操作数为文字
    }

    public BCMove(MoveOperator operator, int level, Formula.Content newContent, Formula<String> newListeral, Boolean isRight) {
        this.operator = operator;
        this.newContent = newContent;
        this.newListeral = newListeral;
        this.level = level;
        this.isRight = isRight;
    }

    public BCMove(Formula<String> formula) {
        innerBCSolution = new BCSolution(formula);
    }

    public BCMove(BCMove move) {
        this.operator = move.operator;
        this.newContent = move.newContent;
        this.newListeral = move.newListeral;
        this.level = move.level;
        this.isRight = move.isRight;
        this.innerBCSolution = move.innerBCSolution;
    }

    private void generateNeighborBCSolution(Formula<String> unchangeFormula) {
        Formula<String> dealFormula = unchangeFormula.copy();
        BCFormulaOperator bcFormulaOperator = new BCFormulaOperator(dealFormula);
        List<BCFormulaOperator.FormulaNode> nodes = bcFormulaOperator.getNodesRef();

        int size = nodes.size();
        if (level < 0 || level >= size) {
            throw new IndexOutOfBoundsException();
        }

        Formula<String> newFormula = null;
        switch (operator) {
            case ADD:
                newFormula = bcFormulaOperator.addOperator(level, newContent, newListeral, isRight);
                break;
            case EXCHANGE:
                if (nodes.get(level).f.isLiteral()) {
                    newFormula = bcFormulaOperator.changeLiteral(level, newListeral);
                } else {
                    newFormula = bcFormulaOperator.changeOperator(level, newContent, newListeral, isRight);
                }
                break;
            case DELETE:
                newFormula = bcFormulaOperator.deleteOperator(level, isRight);
                break;
            default:
                throw new SearchException("Unknow MoveOperator in BCMove");
        }
        innerBCSolution = new BCSolution(newFormula);
    }

    @Override
    public void apply(BCSolution solution) {
        Formula<String> formula = solution.getFormula();
        if (this.innerBCSolution == null) {
            generateNeighborBCSolution(formula);
        }
        solution.swap(innerBCSolution);
    }

    /**
     * Undo this move after it has been applied to the given solution.
     * It is assumed that the solution has not been modified in any way since the move was applied;
     * if so, the behaviour of this method is undefined.
     * @param solution
     */
    @Override
    public void undo(BCSolution solution) {
        solution.swap(innerBCSolution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BCMove bcMove = (BCMove) o;
        if (innerBCSolution != null && bcMove.innerBCSolution != null) {
            return innerBCSolution.getFormula().equals(bcMove.innerBCSolution.getFormula());
        } else {
            return level == bcMove.level &&
                    operator == bcMove.operator &&
                    newContent == bcMove.newContent &&
                    Objects.equals(newListeral, bcMove.newListeral) &&
                    Objects.equals(isRight, bcMove.isRight);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, newContent, newListeral, level, isRight, innerBCSolution);
    }
}
