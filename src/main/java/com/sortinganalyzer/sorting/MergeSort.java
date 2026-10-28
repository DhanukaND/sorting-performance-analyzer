package com.sortinganalyzer.sorting;

public final class MergeSort implements Sorter {

    @Override
    public void sort(double[] values) {
        if (values.length < 2) {
            return;
        }
        double[] buffer = new double[values.length];
        mergeSort(values, buffer, 0, values.length - 1);
    }

    private void mergeSort(double[] values, double[] buffer, int left, int right) {
        if (left >= right) {
            return;
        }
        int middle = left + (right - left) / 2;
        mergeSort(values, buffer, left, middle);
        mergeSort(values, buffer, middle + 1, right);
        merge(values, buffer, left, middle, right);
    }

    private void merge(double[] values, double[] buffer, int left, int middle, int right) {
        int first = left;
        int second = middle + 1;
        int target = left;

        while (first <= middle && second <= right) {
            buffer[target++] = values[first] <= values[second] ? values[first++] : values[second++];
        }
        while (first <= middle) {
            buffer[target++] = values[first++];
        }
        while (second <= right) {
            buffer[target++] = values[second++];
        }
        for (int i = left; i <= right; i++) {
            values[i] = buffer[i];
        }
    }
}
