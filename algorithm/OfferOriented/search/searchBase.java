package search;

/**
 * @author flowscolors
 * @date 2022-02-17 17:27
 */
public class searchBase {

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


    public static void main(String[] args) {
        int[] input = new int[]{1,2,3,4,5};
        System.out.println(search(input,5));
    }

}
