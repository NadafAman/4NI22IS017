package com.example.demo.utill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class SlidingWindow {

    private final int maxSize;
    private final LinkedHashSet<Integer> window = new LinkedHashSet<>();

    public SlidingWindow(int size) {
        this.maxSize = size;
    }

    public List<Integer> getCurrentState() {
        return new ArrayList<>(window);
    }

    public void addNumbers(List<Integer> nums) {
        for (Integer num : nums) {
            if (!window.contains(num)) {
                if (window.size() == maxSize) {
                    Iterator<Integer> it = window.iterator();
                    it.next();
                    it.remove();
                }
                window.add(num);
            }
        }
    }

    public double calculateAverage() {
        return window.isEmpty() ? 0.0 :
                window.stream().mapToInt(i -> i).average().orElse(0.0);
    }
}
