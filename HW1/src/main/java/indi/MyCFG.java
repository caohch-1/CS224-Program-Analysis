package indi;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;

public class MyCFG {
    UnitGraph unitGraph;
    ArrayList<CFGNode> cfgNodes;

    public MyCFG(Body body) {
        this.unitGraph = new ClassicCompleteUnitGraph(body);
        this.cfgNodes = new ArrayList<>();
        for (Unit unit : body.getUnits()) {
            cfgNodes.add(new CFGNode(unit));
        }

        for (CFGNode tailNode : getTails()) {
            tailNode.setTail(true);
        }

        for (CFGNode headNode : getHeads()) {
            headNode.setHead(true);
        }
    }

    CFGNode searchNodeByUnit(Unit unit) {
        for (CFGNode cfgNode : cfgNodes) {
            if (cfgNode.getUnit() == unit) {
                return cfgNode;
            }
        }
        System.out.println("Warning: Can't find CFGNodeByUnit...");
        return null;
    }

    ArrayList<CFGNode> getPredsOf(CFGNode cfgNode) {
        ArrayList<CFGNode> predNodes = new ArrayList<>();
        List<Unit> predUnits = unitGraph.getPredsOf(cfgNode.getUnit());
        for (Unit predUnit : predUnits) {
            predNodes.add(searchNodeByUnit(predUnit));
        }
        return predNodes;
    }

    ArrayList<CFGNode> getSuccsOf(CFGNode cfgNode) {
        ArrayList<CFGNode> succNodes = new ArrayList<>();
        List<Unit> succUnits = unitGraph.getSuccsOf(cfgNode.getUnit());
        for (Unit predUnit : succUnits) {
            succNodes.add(searchNodeByUnit(predUnit));
        }
        return succNodes;
    }

    ArrayList<CFGNode> getTails() {
        List<Unit> tailUnits = unitGraph.getTails();
        ArrayList<CFGNode> tailCFGNodes = new ArrayList<>();
        for (Unit tailUnit : tailUnits) {
            CFGNode tail = searchNodeByUnit(tailUnit);
            tailCFGNodes.add(tail);
        }

        return tailCFGNodes;
    }

    ArrayList<CFGNode> getHeads() {
        List<Unit> headUnits = unitGraph.getHeads();
        ArrayList<CFGNode> headCFGNodes = new ArrayList<>();
        for (Unit headUnit : headUnits) {
            CFGNode head = searchNodeByUnit(headUnit);
            headCFGNodes.add(head);
        }

        return headCFGNodes;
    }

    public UnitGraph getUnitGraph() {
        return unitGraph;
    }

    public void setUnitGraph(UnitGraph unitGraph) {
        this.unitGraph = unitGraph;
    }

    public ArrayList<CFGNode> getCfgNodes() {
        return cfgNodes;
    }

    public void setCfgNodes(ArrayList<CFGNode> cfgNodes) {
        this.cfgNodes = cfgNodes;
    }
}
