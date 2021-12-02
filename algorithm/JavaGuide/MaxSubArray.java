/**
 * @author flowscolors
 * @date 2021-10-03 19:37
 */
public class MaxSubArray {
    public static int solution(int[] nums) {
        int max = Integer.MIN_VALUE;
        for(int i =0;i<nums.length;i++){
            int sum = 0;
            for(int j = i;j<nums.length;j++){
                sum = sum + nums[j];
                max = Math.max(max,sum);
            }
        }
        return max;
    }

    public static void main(String[] args) {
        int[] input = new int[]{-2,1,-3,4,-1,2,1,-5,4};
        System.out.println(solution(input));
    }
}
