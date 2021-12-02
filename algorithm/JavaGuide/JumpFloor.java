/**
 * @author flowscolors
 * @date 2021-10-03 19:35
 */
public class JumpFloor {
    public static int solution(int number){
        if(number<=0){
            return 0;
        }
        if (number == 1){
            return 1;
        }
        if (number == 2){
            return 2;
        }
        return solution(number-1)+solution(number-2);
    }

    public static int solution2(int number){
        if(number<=0){
            return 0;
        }
        if (number == 1){
            return 1;
        }
        if (number == 2){
            return 2;
        }
        int first = 1,second = 2,third = 3;
        for(int i =3 ;i<=number;i++){
            third = first + second;
            first = second;
            second = third;
        }
        return third;
    }

    public static void main(String[] args) {
        System.out.println(solution2(5));
    }
}
