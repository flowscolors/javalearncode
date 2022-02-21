

## 查找
长度为n的数组中查找一个数字，如果逐一扫描，在需要O(n)的时间，如果数组是排序的，则可以使用二分查找。

```text
    public static  int search (int[] nums,int target){
        int left = 0;
        int right = nums.length-1;
        while(left <= right){
            int mid = (left + right )/2;
            if(nums[mid] == target){
                return mid;
            }
            if(nums[mid] < target){
                left = mid + 1;
            }else{
                right = mid -1;
            }
        }
        return -1 ;
    }
```

常用解题方法：
1.在排序数组中二分查找。一旦看到排序数组，则立马联想二分查找。
2.在数值范围内二分查找。对于不知道问题的解，但是直到解的范围，并且可以逐渐逼近的题，可以使用二分查找的思路。 
    1） 确定解的范围。 2）确定中间值非解时往左跳还是往右跳。  比如求某个数的非负平方根，则值一定在0到m。每次中间值的平方与m比较，判断左跳还是右跳。
    
## 排序
排序是非常基础的算法，因为数组排序后才能使用二分查找，故很多数据都需要先排序再进行存储。
面试中最常遇到的是计数排序、快速排序、归并排序、堆排序。

计数排序是一种线性时间的排序，适用于数组范围远小于长度的情况。首先要按照数组范围构建哈希表（key值有序），然后遍历数组去填满hash表，最后根据hash表输出排序数组。
但是获得数组范围是一件很麻烦的事，大多数情况下我们并不知道，于是只能拿整数上下限来生成hash表了。当然也可以先遍历一遍数组来获取最大、最小值。
```text
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
```

快速排序，基本思想是分治法。在数组中随机取一个基准值，通过遍历数组对其进行分区，分成[比基准小的数][基准][比基准大的数],我们并不知道子数组中的元素具体顺序，只知道比基准的大小。
通过不断选取新的分区标准将数组划分，从而可以使不可控的序列逐渐变小直至消失，此时数组已经排好序。因此快排的函数中会调用3个函数，分别是quicksort取进行下一轮排序，一个是partition，在单次排序中分区，把数组置为上面分区的形态。partition还会调用swap函数用来交换数组下标。
关键在于partition函数，把数组划分成按基准值的三部分，并返回基准值所在位置。该方法可以说是经典双指针问题了，如果该方法可以实现，则TopK问题也依照解决。


归并排序，由合并两个有序数组演变而来。每次合并2个半长的有序数组，直到有序数组的长度为1，相当于把排序拆成了logN次的合并有序数组。而合并有序数组则基于双指针只要进行两次遍历，并比较每次位移的值，大的放入新数组即可。