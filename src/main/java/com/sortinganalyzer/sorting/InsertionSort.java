package com.sortinganalyzer.sorting;

public final class InsertionSort implements Sorter {

    @Override
    public void sort(double[] values) {
        for (int i = 1; i < values.length; i++) {
            double value = values[i];
            int position = i - 1;
            while (position >= 0 && values[position] > value) {
                values[position + 1] = values[position];
                position--;
            }
            values[position + 1] = value;
        }
    }
}
