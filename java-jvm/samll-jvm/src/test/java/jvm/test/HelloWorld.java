package jvm.test;

/**
 * @author flowscolors
 * @date 2021-11-08 23:48
 */
public class HelloWorld {

    //unit1
/*    public static void main(String[] args) {
        System.out.println("hello world");
    }*/

    //unit4
/*    public static void main(String[] args) {
        System.out.println(circumference(1.6f));
    }

    public static float circumference(float r){
        float pi = 3.14f;
        float area = 2 * pi * r;
        return area;
    }*/

    //unit5
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += i;
        }
        System.out.println(sum);
    }

    //unit7
/*    public static void main(String[] args) {
        long x = fibonacci(10);
        System.out.println(x);
    }

    //斐波那契数列（Fibonacci sequence）
    private static long fibonacci(long n) {
        if (n <= 1) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }*/
}
