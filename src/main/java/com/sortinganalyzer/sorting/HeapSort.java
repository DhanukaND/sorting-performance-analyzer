package com.sortinganalyzer.sorting;

public final class HeapSort implements Sorter {

    @Override
    public void sort(double[] values) {
        for (int root = values.length / 2 - 1; root >= 0; root--) {
            siftDown(values, root, values.length);
        }
        for (int end = values.length - 1; end > 0; end--) {
            swap(values, 0, end);
            siftDown(values, 0, end);
        }
    }

    private void siftDown(double[] values, int root, int size) {
        while (true) {
            int child = root * 2 + 1;
            if (child >= size) {
                return;
            }
            if (child + 1 < size && values[child + 1] > values[child]) {
                child++;
            }
            if (values[root] >= values[child]) {
                return;
            }
            swap(values, root, child);
            root = child;
        }
    }

    private void swap(double[] values, int first, int second) {
        double value = values[first];
        values[first] = values[second];
        values[second] = value;
    }
}
