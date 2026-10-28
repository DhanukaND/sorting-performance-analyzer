package com.sortinganalyzer.sorting;

public final class ShellSort implements Sorter {

    @Override
    public void sort(double[] values) {
        for (int gap = values.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < values.length; i++) {
                double value = values[i];
                int position = i;
                while (position >= gap && values[position - gap] > value) {
                    values[position] = values[position - gap];
                    position -= gap;
                }
                values[position] = value;
            }
        }
    }
}
