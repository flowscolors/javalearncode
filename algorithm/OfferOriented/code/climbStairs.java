package code;

/**
 * @author flowscolors
 * @date 2022-02-21 12:28
 */
public class climbStairs {
    public static  int solution (int n){
        if(n == 1){
            return 1;
        }
        if(n == 2){
            return 2;
        }
        return solution(n-1)+solution(n-2);
    }

    public static  int solution2 (int n){
        if(n == 1){
            return 1;
        }
        if(n == 2){
            return 2;
        }
        int a=1,b=2,temp;
        for(int i = 3;i<=n;i++){
            temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }


    public static void main(String[] args) {
        System.out.println(solution(10));
        System.out.println(solution2(10));
    }
}
