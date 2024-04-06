package localsearch;

import localgeneral.LocalGeneralDefaultImp;
import ltlparse.Formula;
import ltlsolver.BCCheckResult;
import ltlsolver.LTLSolver;
import main.InitialConfiguration;
import main.LogionState;
import main.StatisticState;
import modelcounting.ModelCounter;
import org.jamesframework.core.search.neigh.Neighbourhood;
import utils.DiscreteDistribution;
import utils.Pair;

import java.math.BigInteger;
import java.util.*;


public final class BCNeighbourhood implements Neighbourhood<BCSolution> {
    /**
     * 三个公式编辑操作的权重
     */
    private double RENAMEWEIGHT       = InitialConfiguration.renameWeight;
    private double INSERTIONWEIGHT    = InitialConfiguration.insertWeight;
    private double DELETIONWEIGHT     = InitialConfiguration.deleteWeight;

    /**
     * 每个运算符的权重
     */
    // Unary Operator
    private double NOTWEIGHT          = InitialConfiguration.notWeight;
    private double NEXTWEIGHT         = InitialConfiguration.nextWeight;
    private double GLOBALLYWEIGHT     = InitialConfiguration.globallyWeight;
    private double FUTUREWEIGHT       = InitialConfiguration.futureWeight;
    // Binary Operator
    private double ANDWEIGHT          = InitialConfiguration.andWeight;
    private double ORWEIGHT           = InitialConfiguration.orWeight;
    private double IFFWEIGHT          = InitialConfiguration.iffWeight;
    private double UNTILWEIGHT        = InitialConfiguration.untilWeight;
    private double WEAK_UNTILWEIGHT   = InitialConfiguration.weakUntilWeight;

    private ModelCounter modelCounter = null;

    private DiscreteDistribution formulaEditOprDist;
    private DiscreteDistribution operatorsDist;

    private List<Formula<String>> variables;

    private List<Formula.Content> contents;

    private Random rnd = LogionState.random;

    private int MAXNIGHBOURHOODS = InitialConfiguration.neighbourhoodsKsize;
    private BCObjective objective;

    public BCNeighbourhood() {
        this.variables = LogionState.bcData.getVariables();
        this.contents = LogionState.bcData.getContents();
        this.objective = LogionState.bcObjective;

        Collections.shuffle(variables);
        Collections.shuffle(contents);

        initWeight();
    }

    public BCNeighbourhood(BCData bcData, BCObjective objective) {
        this.variables = bcData.getVariables();
        this.contents = bcData.getContents();
        this.objective = objective;

        if(InitialConfiguration.usingModelCounting){
            this.modelCounter = LogionState.modelCounterFactory.getCounter();
        }

        Collections.shuffle(variables);
        Collections.shuffle(contents);

        initWeight();
    }

    private void initWeight() {
        formulaEditOprDist = new DiscreteDistribution(Arrays.asList(RENAMEWEIGHT, INSERTIONWEIGHT, DELETIONWEIGHT));
        operatorsDist = new DiscreteDistribution(Arrays.asList(
                NOTWEIGHT,
                NEXTWEIGHT,
                GLOBALLYWEIGHT,
                FUTUREWEIGHT,
                ANDWEIGHT,
                ORWEIGHT,
                IFFWEIGHT,
                UNTILWEIGHT,
                WEAK_UNTILWEIGHT
                ));
    }

    private BCMove.MoveOperator randomMoveOperator(Random rnd) {
        BCMove.MoveOperator operator = null;
        int randnum = formulaEditOprDist.getNext(rnd);
        switch (randnum) {
            case 0:
                operator = BCMove.MoveOperator.EXCHANGE;
                break;
            case 1:
                operator = BCMove.MoveOperator.ADD;
                break;
            case 2:
                operator = BCMove.MoveOperator.DELETE;
                break;
            default:
                assert false: "random Int(" + randnum + ") out of bound";
        }
        return operator;
    }

    private BCMove.MoveOperator selectOperator(Formula<String> formula) {
        BCMove.MoveOperator operator = null;
        if (formula.isLiteral()) {
            operator = rnd.nextBoolean()? BCMove.MoveOperator.EXCHANGE : BCMove.MoveOperator.ADD;
        } else {
            operator = randomMoveOperator(rnd);
        }
        return operator;
    }

    private Formula.Content selectContent(Formula<String> formula) {
        Formula.Content content;
        do {
//            content = contents.get(rnd.nextInt(contents.size()));
            content = contents.get(formulaEditOprDist.getNext(this.rnd));
        } while (content == formula.getContent());
        return content;
    }

    @Override
    public BCMove getRandomMove(BCSolution solution, Random rnd) {
        BCFormulaOperator bcFormulaOperator = new BCFormulaOperator(solution.getFormula());
        List<BCFormulaOperator.FormulaNode> nodes = bcFormulaOperator.getNodesRef();
        int size = nodes.size();
        int level = rnd.nextInt(size);

        /**
         * 根据需要改变的公式动态选择改变策略
         * level: 变动的点位
         * curformula: 变动点位对应的子公式
         * content: 变化后的 运算符
         * operator: 变化的 公式编辑操作类型： rename, insertion, deletion.
         *
         * 策略：
         * 1. 三个公式编辑操作类型的权重
         * 2. 变化后的运算符的权重:
         *    一元运算符: !, X, G, F
         *    二元运算符: &, |, <=>, U, W
         */
        Formula<String> curformula = nodes.get(level).f;
        Formula.Content content = selectContent(curformula);
        BCMove.MoveOperator operator = selectOperator(curformula);

        Formula<String> literal = variables.get(rnd.nextInt(variables.size()));
        literal = rnd.nextBoolean() ? literal : Formula.Not(literal);

        return new BCMove(operator, level, content, literal, rnd.nextBoolean());
    }

    @Override
    public List<BCMove> getAllMoves(BCSolution solution) {
        List<BCMove> ret;
        if (InitialConfiguration.swlsFlag) {
            ret = swlsStrategy(solution);
        } else {
            ret = defaultStrategy(solution);
        }

        if(InitialConfiguration.localGeneral){
            ret = localGeneralStrategy(ret, solution);
        }
        if(InitialConfiguration.usingModelCounting){
            ret = modelCountingStrategy(ret, solution);
        }
        return ret;
    }

    /**
     * swls: strongthen and weaken local search
     *  0. 候选邻居集合 = {}
     *  基于公式编辑距离找到当前公式的1个邻居 \phi （重复50次）
     * 	 1. 如果\phi是BC，加入到候选邻居集合
     * 	 2. 如果\phi不是BC，
     * 	    2.1 如果不满足极小性M：以概率p_1：
     * 	    		进行 weaken，得到公式 Formula(第一个满足极小性的弱化公式)；
     * 	    			候选邻居集合加入Formula
     * 	    		若不存在这样的 Formula：
     * 	    			候选邻居集合加入\phi;
     * 	         以概率 1 - p_1:
     * 	         	候选邻居集合加入\phi
     * 	    2.2 如果不满足逻辑不相容性质LI：以概率p_2：
     * 	    		进行strengthen，得到公式 Formula(第一个满足不相容性的强化公式)；
     * 	    			候选邻居集合加入Formula
     * 	    		若不存在这样的 Formula：
     * 	    			候选邻居集合加入\phi;
     * 	    		以概率 1 - p_2:
     * 	                候选邻居集合加入\phi
     * 	    2.3 如果不满足非平凡性NT：
     * 	    	候选邻居集合加入\phi;
     * @param solution
     * @return
     */
    protected List<BCMove> swlsStrategy(BCSolution solution) {
        StatisticState.swlsStrategyCount++;
        long swlsStrategyStartTime = System.currentTimeMillis();

        List<BCMove> moves = new ArrayList<>();
        final LTLSolver ltlSATCheck = LogionState.ltlSolverFactory.getSolver(InitialConfiguration.solverType);
        final List<String> goals = LogionState.goals;

        for (int i = 0; i < MAXNIGHBOURHOODS; i++) {
            BCMove originalMove = getRandomMove(solution, this.rnd);
            originalMove.apply(solution);
            double objectValue = objective.evaluate(solution, null).getValue();
            originalMove.undo(solution);

            boolean swlsFlag = false;
            if (objectValue < InitialConfiguration.objectiveValueMinimality) {
                // 如果不满足M，以概率swlsWeakenProbability，进行弱化公式
                if (rnd.nextDouble() <= InitialConfiguration.swlsWeakenProbability) {
                    swlsFlag = true;
                    long swlsWeakenStartTime = System.currentTimeMillis();

                    /**
                     * 检查弱化后的公式 是否满足极小性，寻找第一个满足极小的弱化公式
                     */
                    List<Formula<String>> weakenFormulae = solution.getFormula().weakenFormulaForLevelTraversal();
                    for (Formula<String> weakenFormula : weakenFormulae) {
                        boolean satisfyMinimality = true;
                        for (int index = 0; index < goals.size(); index++) {
                            BCCheckResult ret = ltlSATCheck.checkMinimality(weakenFormula, index, InitialConfiguration.ltlCheckTimeout);
                            if (ret != BCCheckResult.YES) {
                                satisfyMinimality = false;
                                break;
                            }
                        }
                        if (satisfyMinimality) {
                            moves.add(new BCMove(weakenFormula));
                            StatisticState.swlsWeakenFormulae.add(new Pair<>(
                                    solution.getFormula(),
                                    weakenFormula
                            ));
                            break;
                        }
                    }

                    StatisticState.swlsWeakenTime += (System.currentTimeMillis() - swlsWeakenStartTime);
                    StatisticState.swlsWeakenCount++;
                }
            } else if (objectValue < InitialConfiguration.objectiveValueMinimality + InitialConfiguration.objectiveValueInconsistency) {
                // 不满足 LI，以概率 swlsStrengthenProbability 强化公式
                if (rnd.nextDouble() <= InitialConfiguration.swlsStrengthenProbability) {
                    swlsFlag = true;
                    long swlsStrengthStartTime = System.currentTimeMillis();
                    List<Formula<String>> strengthenFormulae = solution.getFormula().strengthenFormulaForLevelTraversal();
                    for (Formula<String> strengthenFormula : strengthenFormulae) {
                        BCCheckResult ret = ltlSATCheck.checkInconsistency(strengthenFormula, InitialConfiguration.ltlCheckTimeout);
                        if (ret == BCCheckResult.YES) {
                            moves.add(new BCMove(strengthenFormula));
                            StatisticState.swlsStrengthenFormulae.add(new Pair<>(
                                    solution.getFormula(),
                                    strengthenFormula
                            ));
                            break;
                        }
                    }
                    StatisticState.swlsStrengthenTime += (System.currentTimeMillis() - swlsStrengthStartTime);
                    StatisticState.swlsStrengthenCount++;
                }
            } else if (objectValue < InitialConfiguration.objectiveValueMinScore) {
                // 不满足 non-trivial
                StatisticState.swlsTrivialFormula++;
            }

            if (!swlsFlag) {
                moves.add(originalMove);
            }
        }

        StatisticState.swlsStrategyTime += (System.currentTimeMillis() - swlsStrategyStartTime);
        return moves;
    }

    /**
     * 默认策略, 随机选择 MAXNIGHBOURHOODS 个邻居返回
     */
    protected List<BCMove> defaultStrategy(BCSolution solution) {
        List<BCMove> moves = new ArrayList<>();
        /**
         * 这里调用 trpuc 判断 solution 对应要改动formula哪些地方，然后针对性的修改这些地方
         */
        for (int i = 0; i < MAXNIGHBOURHOODS; i++) {
            BCMove move = getRandomMove(solution, this.rnd);
            moves.add(move);
        }
        return moves;
    }

    /**
     * local general 策略
     * @param solution
     * @return
     */
    private List<BCMove> localGeneralStrategy(List<BCMove> moves, BCSolution solution) {
        /**
         * 全局general集合 S
         * 本次迭代general集合 S1
         *
         * 对每个邻居都评定分数,然后选择最高分的那一个
         * 对每一个邻居
         * 1. 首先判定它是否是BC, 如果是bc, 执行下面流程
         * 2. 检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      a. 是否 存在 bc \in S1, 当前邻居 |= bc
         *          如果存在, 忽略当前邻居, 结束流程
         *      b. 是否 存在 bc \in S1, bc |= 当前邻居
         *          如果存在, 删除S1 中这样的 bc, 同时删除它在 S
         *      c. 将 当前邻居 加入到 S1 中
         * 3. 检查该邻居 与 S集合的关系
         *      a. 是否 存在 bc \in S, 当前邻居 |= bc
         *          如果存在在, 忽略当前邻居, 结束流程
         *      b. 是否 存在 bc \in S, bc |= 当前邻居
         *          如果存在, 删除该bc
         *      c. 将当前邻居加入到 S 中
         *
         * 优化后伪代码:
         * 1. 首先判定它是否是BC, 如果是bc, 执行下面流程
         * 2. (前置检查)检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      a. 是否 存在 bc \in S1, 当前邻居 |= bc
         *          如果存在, 忽略当前邻居, 结束流程
         * 3. (前置检查)检查该邻居 与 S集合的关系
         *      a. 是否 存在 bc \in S, 当前邻居 |= bc
         *          如果存在在, 忽略当前邻居, 结束流程
         * 4. 检查该邻居 与 当前邻居BC集合 S1 的关系,
         *      b. 是否 存在 bc \in S1, bc |= 当前邻居
         *          如果存在, 删除S1 中这样的 bc, 同时删除它在 S
         *      c. 将 当前邻居 加入到 S1 中
         * 5. 检查该邻居 与 S集合的关系
         *      b. 是否 存在 bc \in S, bc |= 当前邻居
         *          如果存在, 删除该bc
         *      c. 将当前邻居加入到 S 中
         * 6. 下一个当前解中 S1 中考虑. 如果S1为空,则从全部邻居中考虑
         */
        LocalGeneralDefaultImp localGeneralDefaultImp = LogionState.localGeneralImp;
        /**
         * 每次迭代都需要清空 localGeneralBCSet
         */
        localGeneralDefaultImp.preconditions();

        for (BCMove move : moves) {
            move.apply(solution);
            objective.evaluate(solution, null);
            move.undo(solution);
        }

        /**
         * 如果 localGeneralBCSet 不为空,那么下一个当前解只冲 localGeneralBCSet 中考虑
         */
        if (!localGeneralDefaultImp.getLocalGeneralBCSet().isEmpty()) {
            List<BCMove> localGeneralMoves = new LinkedList<>();
            for (Formula<String> formula : localGeneralDefaultImp.getLocalGeneralBCSet()) {
                localGeneralMoves.add(new BCMove(formula));
            }
            return localGeneralMoves;
        }
        return moves;
    }

    /**
     * local general 策略
     * @param solution
     * @return
     */
    private List<BCMove> modelCountingStrategy(List<BCMove> moves, BCSolution solution) {
        LocalGeneralDefaultImp localGeneralDefaultImp = LogionState.localGeneralImp;
        List<BCMove> resMoves = new LinkedList<>();
        /**
         * 相当于一步剪枝操作，不需要用apply获得当前的Formula，而是从缓存中的localgeneral中直接得到方法
         */
        if(InitialConfiguration.localGeneral){
            List<Formula<String>> localGeneralBCSet = new ArrayList<>(localGeneralDefaultImp.getLocalGeneralBCSet());
            if (!localGeneralBCSet.isEmpty()) {
                Map<Double, Pair<BigInteger, Integer>>general_value2index = filterByModelCounting(localGeneralBCSet);
                for(Map.Entry<Double, Pair<BigInteger, Integer>> entry : general_value2index.entrySet()){
                    int index = entry.getValue().getValue();
                    resMoves.add(new BCMove(localGeneralBCSet.get(index)));
                }
//                resMoves = filterListByIndex(general_value2index, localGeneralBCSet);
                return resMoves;
            }
        }
        Map<Double, Pair<BigInteger, Integer>> total_value2index = filterByModelCounting(moves, solution);
        resMoves = filterListByIndex(total_value2index, moves);
        return resMoves;
    }

    /**
     * TODO 思考把两个filter函数并作一个
     */

    Map<Double, Pair<BigInteger, Integer>> filterByModelCounting(List<Formula<String>> localGeneralBCSet) {
        Map<Double, Pair<BigInteger, Integer>> total_value2index = new HashMap<>();
        for (int i = 0; i <localGeneralBCSet.size(); ++i) {
            Formula<String> formula = localGeneralBCSet.get(i);
            double value = objective.getValueByFormula(formula);
            if (!total_value2index.containsKey(value)) {
                BigInteger count = modelCounter.count(formula);
                total_value2index.put(value, new Pair<>(count, i));
            } else {
                BigInteger count = total_value2index.get(value).getKey();
                Formula<String> compareFormuala = localGeneralBCSet.get(total_value2index.get(value).getValue());
                BigInteger compareCount = modelCounter.count(compareFormuala);
                if (count.compareTo(compareCount) < 0) {
                    total_value2index.put(value, new Pair<>(compareCount, i));
                }
            }
        }
        return total_value2index;
    }

    Map<Double, Pair<BigInteger, Integer>> filterByModelCounting(List<BCMove> moves, BCSolution solution) {
        Map<Double, Pair<BigInteger, Integer>> total_value2index = new HashMap<>();
        for (int i = 0; i < moves.size(); ++i) {
            BCMove move = moves.get(i);
            move.apply(solution);
            Formula<String> formula = solution.getFormula();
            double value = objective.getValueByFormula(formula);
            if (!total_value2index.containsKey(value)) {
                BigInteger count = modelCounter.count(formula);
                move.undo(solution);
                total_value2index.put(value, new Pair<>(count, i));
            } else {
                move.undo(solution);
                BigInteger count = total_value2index.get(value).getKey();
                BCMove currMove = moves.get(total_value2index.get(value).getValue());
                currMove.apply(solution);
                BigInteger compareCount = modelCounter.count(solution.getFormula());
                currMove.undo(solution);
                if (count.compareTo(compareCount) < 0) {
                    total_value2index.put(value, new Pair<>(compareCount, i));
                }
            }
        }
        return total_value2index;
    }


    /**
     *  TODO 有一部分代码是对List<Formula<String>>做相同操作 但是不能定义新的方法, 会擦除泛型
     */
    List<BCMove> filterListByIndex(Map<Double, Pair<BigInteger, Integer>> value2index, List<BCMove> targetList){
        List<BCMove> res = new ArrayList<>();
        for(Map.Entry<Double, Pair<BigInteger, Integer>> entry : value2index.entrySet()){
            int index = entry.getValue().getValue();
            res.add(targetList.get(index));
        }
        return res;
    }

}
