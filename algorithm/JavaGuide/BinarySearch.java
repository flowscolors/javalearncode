/**
 * @author flowscolors
 * @date 2021-10-03 19:36
 */
public class BinarySearch {
    //类似跳台阶，不用递归的就用for或者while循环解决
    public static int solution(int[] array,int key,int left,int right){

        if(left<0 || right>array.length-1 || key<array[left] || key > array[right] || left > right){
            return -1;
        }

        if(array[(left+right)/2]==key){
            return (left+right)/2;
        }else if(array[(left+right)/2]>key){
            return solution(array,key,left,(left+right)/2-1);
        }else {
            return solution(array,key,(left+right)/2+1,right);
        }
    }

    public static int solution2(int[] array,int key){
        int left = 0;
        int right = array.length-1;
        //int mid = (left + right)/2;
        if(left<0 || right>array.length-1 || key<array[left] || key > array[right] || left > right){
            return -1;
        }

        while(left<right){
            if(array[(left+right)/2]==key){
                return (left+right)/2;
            }else if(array[(left+right)/2]>key){
                right = (left+right)/2 -1;
            }else {
                left = (left+right)/2 +1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] input = new int[]{1,4,8,9,12,49};
        int[] input2 = new int[]{6, 12, 33, 87, 90, 97, 108, 561};
        System.out.println(solution(input,8,0,input.length-1));
        System.out.println(solution2(input,8));
        System.out.println(solution(input2,108,0,input2.length-1));
        System.out.println(solution2(input2,108));
    }

}
