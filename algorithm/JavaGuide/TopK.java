import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author flowscolors
 * @date 2021-10-03 19:47
 */
public class TopK {
    public static int solution(int[] array,int topN){
        if(topN<1){
            return -1;
        }
        Arrays.sort(array);
        System.out.println(Arrays.toString(array));
        return array[array.length-topN];
    }

    //大顶堆查最小的前N个值，小顶底查最大的前N个值。因为
    public static int solution2(int[] array,int topN){
        if(topN<1){
            return -1;
        }
        // 使用一个最大堆（大顶堆）
        // Java 的 PriorityQueue 默认是小顶堆，添加 comparator 参数使其变成最大堆
        Queue<Integer> heap = new PriorityQueue<>(topN, (i1, i2) -> Integer.compare(i2, i1));

        for (int e : array) {
            // 当前数字小于堆顶元素才会入堆
            if (heap.isEmpty() || heap.size() < topN || e > heap.peek()) {
                heap.offer(e);
            }
            if (heap.size() > topN) {
                heap.poll(); // 删除堆顶最大元素
            }
        }

        // 将堆中的元素存入数组
        int[] res = new int[topN];
        for(int i = 0; i< topN;i++){
            res[i] = heap.poll();
        }
        System.out.println(Arrays.toString(res));
        return res[0];
    }


    public static int[] solution3(int[] array,int topN){
        int[] result = new int[topN];
        int max = Integer.MAX_VALUE;
        for(int i = 0 ;i<topN;i++){
            int temp = Integer.MIN_VALUE;
            for(int j = 0 ;j< array.length;j++){
                if(array[j]>temp && array[j] < max){
                    temp = array[j];
                }
            }
            result[i] = temp;
            max = Math.min(max,temp);
        }

        return result;
    }


    public static void main(String[] args) {
        int[] input = new int[]{7,5,15,3,17,2,20,24,1,9,12,8};
        System.out.println(solution(input,4));
        System.out.println(solution2(input,4));
        System.out.println(Arrays.toString(solution3(input,4)));
    }
}
