package com;

import soot.Local;
import soot.Unit;

import java.util.HashSet;
import java.util.Set;

public class CFGNode {
    Set<Local> inSet;
    Set<Local> outSet;
    Unit unit;
    boolean isTail;
    boolean isHead;

    public CFGNode(Set<Local> inSet, Set<Local> outSet, Unit unit) {
        this.inSet = inSet;
        this.outSet = outSet;
        this.unit = unit;
        this.isHead = false;
        this.isTail = false;
    }

    public CFGNode(Unit unit) {
        this.inSet = new HashSet<>();
        this.outSet = new HashSet<>();
        this.unit = unit;
        this.isHead = false;
        this.isTail = false;
    }

    public boolean isTail() {
        return isTail;
    }

    public void setTail(boolean tail) {
        isTail = tail;
    }

    public boolean isHead() {
        return isHead;
    }

    public void setHead(boolean head) {
        isHead = head;
    }

    @Override
    public String toString() {
        return String.format("CFGNode{unit: %s, inSet: %s, outSet: %s, isHead: %s, isTail: %s}",
                unit, inSet, outSet, isHead, isTail);
    }

    public Set<Local> getInSet() {
        return inSet;
    }

    public void setInSet(Set<Local> inSet) {
        this.inSet = inSet;
    }

    public Set<Local> getOutSet() {
        return outSet;
    }

    public void setOutSet(Set<Local> outSet) {
        this.outSet = outSet;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
