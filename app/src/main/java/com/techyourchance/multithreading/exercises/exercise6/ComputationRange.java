package com.techyourchance.multithreading.exercises.exercise6;

class ComputationRange {
    private long start;
    private long end;

    public ComputationRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
