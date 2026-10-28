package com.sortinganalyzer.sorting;

public final class QuickSort implements Sorter {

    @Override
    public void sort(double[] values) {
        quickSort(values, 0, values.length - 1);
    }

    private void quickSort(double[] values, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(values, low, high);
        quickSort(values, low, pivotIndex - 1);
        quickSort(values, pivotIndex + 1, high);
    }

    private int partition(double[] values, int low, int high) {
        double pivot = values[high];
        int smaller = low;
        for (int current = low; current < high; current++) {
            if (values[current] <= pivot) {
                swap(values, smaller++, current);
            }
        }
        swap(values, smaller, high);
        return smaller;
    }

    private void swap(double[] values, int first, int second) {
        double value = values[first];
        values[first] = values[second];
        values[second] = value;
    }
}
