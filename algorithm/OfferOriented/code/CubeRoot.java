package code;

/**
 * @author flowscolors
 * @date 2022-02-21 12:50
 */
public class CubeRoot {

    public static double solution(double input){
        if(input < 0){
            return -solution(-input);
        }
        double left = 0;
        double right = input;
        double mid = (left + right)/2;
        while(Math.abs(mid*mid*mid - input)>0.00001){
          mid = (left + right)/2;
            if(Math.abs(mid*mid*mid - input)<0.00001){
                return mid;
            }else if (mid*mid*mid < input){
                left = mid;
            }else {
                right = mid;
            }
        }
        return mid;
    }


    public static void main(String[] args) {
        System.out.println(solution(4913));
    }

}
