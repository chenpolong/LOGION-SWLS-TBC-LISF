package refine;

import assessment.GoalModifyAssessor;
import gov.nasa.ltl.trans.ParseErrorException;
import ltlparse.Formula;
import ltlsolver.BCCheckResult;
import ltlsolver.LTLSolver;
import ltlsolver.LTLSolverFactory;
import ltlsolver.LTLSolverType;
import main.InitialConfiguration;
import main.LogionState;
import refine.goalimproving.GoalImprover;
import refine.goalimproving.GoalImproverFactory;
import utils.Pair;

import java.util.*;
import java.util.concurrent.*;

public class Refinement {
    private List<Formula<String>> doms = null;
    private List<Formula<String>> goals = null;
    private GoalModifyAssessor goalModifyAssessor = null;
    private GoalImprover goalImprover = null;
    private int maxImproveTimes = 10;
    private int refineTimeout = 10;
    /**
     * 一些统计的变量
     */
    private static int totalRefineTime = 0;
    private static int timeoutRefine = 0;
    private static int validRefine = 0;
    private static int improveTime = 0;

    private static Map<String, Pair<Integer, String>> refineList = new HashMap<>();
    private static List<Set<String>> finalRefineList = new LinkedList<>();
    private static Map<String, Integer> failRefineList = new HashMap<>();

    public Refinement() throws ParseErrorException {
        this.doms = LogionState.formulaDoms;
        this.goals = LogionState.formulaGoals;
        goalModifyAssessor = new GoalModifyAssessor();
        this.refineTimeout = InitialConfiguration.refineTimeout;
        this.maxImproveTimes = InitialConfiguration.modifyMaxTime;
        goalImprover = new GoalImproverFactory().getImprover(InitialConfiguration.goalImproverType);
        for(int i = 0; i < goals.size(); ++i){
            finalRefineList.add(new HashSet<>());
        }
    }

    public void refine(Formula<String> BC){
        final ExecutorService exec = Executors.newFixedThreadPool(1);

        Callable<BCCheckResult> call = new Callable<BCCheckResult>() {
            public BCCheckResult call() throws Exception {
                int targetIndex = goalModifyAssessor.findIndexOfTargetGoal(BC.toRLTL());
                BCCheckResult isBC = BCCheckResult.YES;
                Formula<String> newGoal = null;
                int currIteration = 0;
                while(isBC!=BCCheckResult.NO && currIteration < maxImproveTimes){
                    newGoal = improveGoal(targetIndex);
                    isBC = CheckNewGoal(newGoal, targetIndex, BC);
                    currIteration++;
                    improveTime++;
                }
                if(isBC==BCCheckResult.NO){
                    refineList.put(BC.toPLTLString(), new Pair<Integer, String>(targetIndex, newGoal.toPLTLString()));
                    Set<String> orgin = finalRefineList.get(targetIndex);
                    if(orgin.add(newGoal.toPLTLString())){
                        validRefine++;
                    }
                }
                else{
                    failRefineList.put(BC.toPLTLString(), targetIndex);
                }
                return isBC;
            }
        };

        try {
            Future<BCCheckResult> future = exec.submit(call);
            future.get(refineTimeout*1000, TimeUnit.MILLISECONDS); //任务处理超时时间设为 1 秒
            totalRefineTime++;
        } catch (TimeoutException ex) {
            timeoutRefine++;
        } catch (Exception e) {
            System.out.println("处理失败.");
            e.printStackTrace();
        }
        // 关闭线程池
        exec.shutdown();
    }

    /**
     * TODO 修改某index的goal的内容
     * @param targetIndex
     * @return
     */
    public Formula<String> improveGoal(int targetIndex){
        Formula<String> targetGoal = goals.get(targetIndex);
        return goalImprover.improveGoal(targetGoal);
    }

    /**
     * 检查新生成的goal是否可以让BC消除
     * @param newGoal
     * @param oldGoalIndex
     * @param BC
     * @return
     */
    public BCCheckResult CheckNewGoal(Formula<String> newGoal, int oldGoalIndex, Formula<String> BC){
        List<Formula<String>> newGoals = new LinkedList<>(goals);
        newGoals.add(oldGoalIndex, newGoal);
        LTLSolver solver = new LTLSolverFactory(doms, newGoals).getSolver(LTLSolverType.nuXmv);
        BCCheckResult res = solver.checkInconsistency(BC, InitialConfiguration.ltlCheckTimeout);
        if(res == BCCheckResult.YES){
            for(int i = 0; i < newGoals.size(); ++i){
                res = solver.checkMinimality(BC, i, InitialConfiguration.ltlCheckTimeout);
                if(res != BCCheckResult.YES){
                    break;
                }
            }
        }
        if(res == BCCheckResult.YES){
            res = solver.checkNonTriviality(BC, InitialConfiguration.ltlCheckTimeout);
        }
        return res;
    }



    public void printRefinement(){
        int refineNum = 1;
        System.out.println("**************************************** Refinements ****************************************");
        System.out.println("[IMPROVE]");
        System.out.println("\tImprove Time: "+improveTime);
        System.out.println("[REFINE]");
        System.out.println("\tCall refinement: "+totalRefineTime+"\tSuccess refinement: "+refineList.size());
        System.out.println("\tValid refinement: "+validRefine+"\tTimeout refinement: "+timeoutRefine);
//        for(Map.Entry<String, Pair<Integer, String>> entry : refineList.entrySet()){
//           System.out.println("\t["+refineNum+"] BC: "+entry.getKey());
//           System.out.println("\t\tGoal index: "+entry.getValue().getKey()+"  "+entry.getValue().getValue());
//           refineNum++;
//        }
        System.out.println("[LOG]");
        System.out.println("\tValid list: "+validRefine);
        for(int i = 0; i < finalRefineList.size(); ++i){
            Set<String> curr = finalRefineList.get(i);
            if(curr.size()!=0){
                System.out.println("\t\tGoal index: "+i+"  Goal: "+goals.get(i).toPLTLString());
                Iterator<String> it = curr.iterator();
                while (it.hasNext()) {
                    System.out.println("\t\t\tBC: "+it.next());
                }
            }
        }
        System.out.println("\tFail list: "+failRefineList.size());
        if(failRefineList.size() != 0){
            for(Map.Entry<String, Integer> entry : failRefineList.entrySet()){
                System.out.println("\t\t["+refineNum+"] BC: "+entry.getKey());
                System.out.println("\t\t\tGoal index: "+entry.getValue());
                refineNum++;
            }
        }

    }

}
