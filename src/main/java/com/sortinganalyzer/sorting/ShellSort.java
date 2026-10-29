package com.sortinganalyzer.sorting;

public class ShellSort implements Sorter {

    public void sort(int[] values) {
        if (values == null || values.length < 2) return;
        int gap = values.length / 2;
        while (gap > 0) {
            for (int i = gap; i < values.length; i++) {
                int key = values[i];
                int j = i - gap;
                while (j >= 0 && values[j] > key) {
                    values[j + gap] = values[j];
                    j -= gap;
                }
                values[j + gap] = key;
            }
            gap /= 2;
        }
    }

    @Override
    public void sort(double[] values) {
        if (values == null || values.length < 2) return;
        int gap = values.length / 2;
        while (gap > 0) {
            for (int i = gap; i < values.length; i++) {
                double key = values[i];
                int j = i - gap;
                while (j >= 0 && values[j] > key) {
                    values[j + gap] = values[j];
                    j -= gap;
                }
                values[j + gap] = key;
            }
            gap /= 2;
        }
    }
}
