package sort;

import java.util.Arrays;

/**
 * @author flowscolors
 * @date 2022-02-17 17:46
 */
public class sortBase {
    public static int[] countSort(int[] nums){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int i = 0;i<nums.length;i++){
            min = Math.min(min,nums[i]);
            max = Math.max(max,nums[i]);
        }
        int[] counts = new int[max-min+1];
        int[] result = new int[nums.length];

        for(int i = 0;i<nums.length;i++){
            counts[nums[i]-min]++;
        }
        int j=0;
        for(int i = min;i<=max;i++){
            while(counts[i-min]>0){
                result[j++]=i;
                counts[i-min]--;
            }
        }
        return result;
    }

    public static int[] sortArray(int[] nums){
        quicksort(nums,0,nums.length);
        return nums;
    }

    public static void quicksort(int[] nums,int start,int end){
        if(end > start){
            int privot = partition(nums,start,end);
            quicksort(nums,start,privot-1);
            quicksort(nums,privot+1,end);
        }
    }

    //双指针 一个指针是small，用来在合适的时机进行swap，一个指针是i，用来遍历数组
    public static int partition(int[] nums,int start,int end){
        swap(nums,start+1,end);
        int small = start -1;
        for(int i = start;i<end;i++){
            if(nums[i]<nums[end]){
                small++;
                swap(nums,i,small);
            }
        }
        small++;
        swap(nums,small,end);
        return small;
    }

    public static void swap (int[] nums,int index1,int index2){
        if(index1!=index2){
            int temp = nums[index1];
            nums[index1] = nums[index2];
            nums[index2] = temp;
        }
    }




    public static void main(String[] args) {
        int[] input = new int[]{2,3,4,2,3,2,1};
        System.out.println(Arrays.toString(countSort(input)));
        System.out.println(Arrays.toString(sortArray(input)));
    }
}
