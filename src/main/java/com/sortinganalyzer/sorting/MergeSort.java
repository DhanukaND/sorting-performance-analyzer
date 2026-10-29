package com.sortinganalyzer.sorting;

public class MergeSort implements Sorter {

    public void sort(int[] values) {
        if (values == null || values.length < 2) return;
        int[] helper = new int[values.length];
        mergeSort(values, helper, 0, values.length - 1);
    }

    @Override
    public void sort(double[] values) {
        if (values == null || values.length < 2) return;
        double[] helper = new double[values.length];
        mergeSort(values, helper, 0, values.length - 1);
    }

    private void mergeSort(int[] values, int[] helper, int left, int right) {
        if (left >= right) return;
        int middle = left + (right - left) / 2;
        mergeSort(values, helper, left, middle);
        mergeSort(values, helper, middle + 1, right);
        merge(values, helper, left, middle, right);
    }

    private void mergeSort(double[] values, double[] helper, int left, int right) {
        if (left >= right) return;
        int middle = left + (right - left) / 2;
        mergeSort(values, helper, left, middle);
        mergeSort(values, helper, middle + 1, right);
        merge(values, helper, left, middle, right);
    }

    private void merge(int[] values, int[] helper, int left, int middle, int right) {
        System.arraycopy(values, left, helper, left, right - left + 1);
        int i = left;
        int j = middle + 1;
        int k = left;
        while (i <= middle && j <= right) {
            if (helper[i] <= helper[j]) {
                values[k++] = helper[i++];
            } else {
                values[k++] = helper[j++];
            }
        }
        while (i <= middle) {
            values[k++] = helper[i++];
        }
        while (j <= right) {
            values[k++] = helper[j++];
        }
    }

    private void merge(double[] values, double[] helper, int left, int middle, int right) {
        System.arraycopy(values, left, helper, left, right - left + 1);
        int i = left;
        int j = middle + 1;
        int k = left;
        while (i <= middle && j <= right) {
            if (helper[i] <= helper[j]) {
                values[k++] = helper[i++];
            } else {
                values[k++] = helper[j++];
            }
        }
        while (i <= middle) {
            values[k++] = helper[i++];
        }
        while (j <= right) {
            values[k++] = helper[j++];
        }
    }
}
