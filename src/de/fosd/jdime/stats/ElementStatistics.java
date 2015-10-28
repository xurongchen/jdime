package de.fosd.jdime.stats;

import java.io.PrintStream;

public class ElementStatistics {

    private int total;
    private int numAdded;
    private int numMerged;
    private int numMatched;
    private int numDeleted;
    private int numChanged;
    private int numOccurInConflict;

    public ElementStatistics() {
        this.total = 0;
        this.numAdded = 0;
        this.numMerged = 0;
        this.numMatched = 0;
        this.numDeleted = 0;
        this.numChanged = 0;
        this.numOccurInConflict = 0;
    }

    public void add(ElementStatistics other) {
        total += other.total;
        numAdded += other.numAdded;
        numMerged += other.numMerged;
        numMatched += other.numMatched;
        numDeleted += other.numDeleted;
        numChanged += other.numChanged;
        numOccurInConflict += other.numOccurInConflict;
    }

    /**
     * Increments the total by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementTotal(int by) {
        total += by;
    }

    /**
     * Increments the added count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumAdded(int by) {
        numAdded += by;
    }

    /**
     * Increments the merged count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumMerged(int by) {
        numMerged += by;
    }

    /**
     * Increments the matched count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumMatched(int by) {
        numMatched += by;
    }

    /**
     * Increments the deleted count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumDeleted(int by) {
        numDeleted += by;
    }

    /**
     * Increments the changed count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumChanged(int by) {
        numChanged += by;
    }

    /**
     * Increments the occur in conflict count by the given amount.
     *
     * @param by the amount to add
     */
    public void incrementNumOccurInConflic(int by) {
        numOccurInConflict += by;
    }

    /**
     * Increments the total by 1.
     */
    public void incrementTotal() {
        incrementTotal(1);
    }

    /**
     * Increments the added count by 1.
     */
    public void incrementNumAdded() {
        incrementNumAdded(1);
    }

    /**
     * Increments the merged count by 1.
     */
    public void incrementNumMerged() {
        incrementNumMerged(1);
    }

    /**
     * Increments the matched count by 1.
     */
    public void incrementNumMatched() {
        incrementNumMatched(1);
    }

    /**
     * Increments the deleted count by 1.
     */
    public void incrementNumDeleted() {
        incrementNumDeleted(1);
    }

    /**
     * Increments the changed count by 1.
     */
    public void incrementNumChanged() {
        incrementNumChanged(1);
    }

    /**
     * Increments the occur in conflict count by 1.
     */
    public void incrementNumOccurInConflic() {
        incrementNumOccurInConflic(1);
    }

    /**
     * Returns the total.
     *
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total to the new value.
     *
     * @param total
     *         the new total
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Returns the added counter.
     *
     * @return the added counter
     */
    public int getNumAdded() {
        return numAdded;
    }

    /**
     * Sets the added counter to the new value.
     *
     * @param numAdded
     *         the new added counter
     */
    public void setNumAdded(int numAdded) {
        this.numAdded = numAdded;
    }

    /**
     * Returns the merged counter.
     *
     * @return the merged counter
     */
    public int getNumMerged() {
        return numMerged;
    }

    /**
     * Sets the merged counter to the new value.
     *
     * @param numMerged
     *         the new merged counter
     */
    public void setNumMerged(int numMerged) {
        this.numMerged = numMerged;
    }

    /**
     * Returns the matched counter.
     *
     * @return the matched counter
     */
    public int getNumMatched() {
        return numMatched;
    }

    /**
     * Sets the matched counter to the new value.
     *
     * @param numMatched
     *         the new matched counter
     */
    public void setNumMatched(int numMatched) {
        this.numMatched = numMatched;
    }

    /**
     * Returns the deleted counter.
     *
     * @return the deleted counter
     */
    public int getNumDeleted() {
        return numDeleted;
    }

    /**
     * Sets the deleted counter to the new value.
     *
     * @param numDeleted
     *         the new deleted counter
     */
    public void setNumDeleted(int numDeleted) {
        this.numDeleted = numDeleted;
    }

    /**
     * Returns the changed counter.
     *
     * @return the changed counter
     */
    public int getNumChanged() {
        return numChanged;
    }

    /**
     * Sets the changed counter to the new value.
     *
     * @param numChanged
     *         the new changed counter
     */
    public void setNumChanged(int numChanged) {
        this.numChanged = numChanged;
    }

    /**
     * Returns the occur in conflict counter.
     *
     * @return the occur in conflict counter
     */
    public int getNumOccurInConflict() {
        return numOccurInConflict;
    }

    /**
     * Sets the occur in conflict counter to the new value.
     *
     * @param numOccurInConflict
     *         the new occur in conflict counter
     */
    public void setNumOccurInConflict(int numOccurInConflict) {
        this.numOccurInConflict = numOccurInConflict;
    }

    public void print(PrintStream os, String indent) {
        os.print(indent); os.print("Total:      "); os.println(total);
        os.print(indent); os.print("Added:      "); os.println(numAdded);
        os.print(indent); os.print("Merged:     "); os.println(numMerged);
        os.print(indent); os.print("Matched:    "); os.println(numMatched);
        os.print(indent); os.print("Deleted:    "); os.println(numDeleted);
        os.print(indent); os.print("Changed:    "); os.println(numChanged);
        os.print(indent); os.print("InConflict: "); os.println(numOccurInConflict);
    }
}
