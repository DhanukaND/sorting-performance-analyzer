package com.sortinganalyzer.sorting;

public class QuickSort {

    public void sort(int[] values) {
        if (values == null || values.length < 2) return;
        quickSort(values, 0, values.length - 1);
    }

    public void sort(double[] values) {
        if (values == null || values.length < 2) return;
        quickSort(values, 0, values.length - 1);
    }

    private void quickSort(int[] values, int low, int high) {
        if (low >= high) return;
        int pivot = values[high];
        int left = low;
        int right = high - 1;
        while (left <= right) {
            while (left <= right && values[left] < pivot) left++;
            while (left <= right && values[right] > pivot) right--;
            if (left <= right) {
                swap(values, left, right);
                left++;
                right--;
            }
        }
        swap(values, left, high);
        quickSort(values, low, left - 1);
        quickSort(values, left + 1, high);
    }

    private void quickSort(double[] values, int low, int high) {
        if (low >= high) return;
        double pivot = values[high];
        int left = low;
        int right = high - 1;
        while (left <= right) {
            while (left <= right && values[left] < pivot) left++;
            while (left <= right && values[right] > pivot) right--;
            if (left <= right) {
                swap(values, left, right);
                left++;
                right--;
            }
        }
        swap(values, left, high);
        quickSort(values, low, left - 1);
        quickSort(values, left + 1, high);
    }

    private void swap(int[] values, int left, int right) {
        int temp = values[left];
        values[left] = values[right];
        values[right] = temp;
    }

    private void swap(double[] values, int left, int right) {
        double temp = values[left];
        values[left] = values[right];
        values[right] = temp;
    }
}