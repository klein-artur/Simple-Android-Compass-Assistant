package org.groundhoglabs.assistants;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Artur Hellmann on 24.06.16.
 *
 * This class represents a moving average.
 */
public class MovingAverageList extends ArrayList<Float> {

    /**
     * the amount of values maximal for this average list.
     */
    private final int max;

    /**
     * initializes the list with a default maximum of 10.
     */
    public MovingAverageList() {
        this(10);
    }

    /**
     * initializes the list
     * @param max the maximum amount of values
     */
    public MovingAverageList(int max) {
        this.max = max;
    }

    /**
     * adds an object to the average list and removes old ones if full.
     * @param object the value to add.
     * @return always true
     */
    @Override
    public boolean add(Float object) {
        if (this.size() >= max) {
            this.remove(0);
        }
        return super.add(object);
    }

    /**
     * Adds an object to the list and removes old ones if full. It gives the moving average as a
     * return value.
     * @param object the value to add
     * @return the average of the list.
     */
    public Float addAndGetAverage(Float object) {
        this.add(object);
        return this.getAverage();
    }

    /**
     * returns the average of the list
     * @return the average
     */
    public Float getAverage() {
        Float sum = 0.0f;
        for (Iterator<Float> it = this.iterator(); it.hasNext();) {
            Float val = it.next();
            sum += val;
        }
        return sum / this.size();
    }
}
