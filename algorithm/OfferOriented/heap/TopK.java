package heap;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * @author flowscolors
 * @date 2022-02-17 11:38
 */
public class TopK {

    public static int[] solution(int k,int[] nums){
        PriorityQueue heap = new PriorityQueue();
        int[] result = new int[k];
        for(int i = 0;i < nums.length ;i++){
            if(heap.size() < k){
                heap.offer(nums[i]);
            }else {
                if(nums[i] > (Integer) heap.peek()){
                    heap.poll();
                    heap.offer(nums[i]);
                }
            }
        }
        for(int i= 0;i<k;i++){
            result[i] = (int) heap.poll();
        }
        return result;
    }

    public static void main(String[] args) {
        int[] input = new int[]{1,3,5,9,8,7,6,4,2};
        System.out.println(Arrays.toString(solution(5,input)));
    }
}
