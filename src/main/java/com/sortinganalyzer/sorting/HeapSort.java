package com.sortinganalyzer.sorting;

public class HeapSort implements Sorter {

    public void sort(int[] values) {
        if (values == null || values.length < 2) return;
        int n = values.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(values, n, i);
        }
        for (int i = n - 1; i >= 0; i--) {
            int temp = values[0];
            values[0] = values[i];
            values[i] = temp;
            heapify(values, i, 0);
        }
    }

    @Override
    public void sort(double[] values) {
        if (values == null || values.length < 2) return;
        int n = values.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(values, n, i);
        }
        for (int i = n - 1; i >= 0; i--) {
            double temp = values[0];
            values[0] = values[i];
            values[i] = temp;
            heapify(values, i, 0);
        }
    }

    private void heapify(int[] values, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && values[left] > values[largest]) largest = left;
        if (right < n && values[right] > values[largest]) largest = right;
        if (largest != i) {
            int swap = values[i];
            values[i] = values[largest];
            values[largest] = swap;
            heapify(values, n, largest);
        }
    }

    private void heapify(double[] values, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && values[left] > values[largest]) largest = left;
        if (right < n && values[right] > values[largest]) largest = right;
        if (largest != i) {
            double swap = values[i];
            values[i] = values[largest];
            values[largest] = swap;
            heapify(values, n, largest);
        }
    }
}
