import java.util.Stack;

import java.util.Stack;

/**
 * @author flowscolors
 * @date 2021-10-03 19:46
 */

//方向很重要 你是把字符存到栈里还是把字符串存到栈里 对后面的解法会有很大的不同，因为字符无法记录长度，而字符串可以
//上面的二分查找也是同理，由于数组无法指定位置进行切分，故把位置放到参数里进行解决
public class ReverseParentheses {
    public static String solution(String s){
        Stack<String> stack = new Stack<>();
        StringBuffer sb = new StringBuffer();
        for(int i = 0 ; i < s.length() ;i++){
            if(s.charAt(i)=='('){
                //实际栈存的不是输入的s的数据，而是sb的数据
                stack.push(sb.toString());
                sb.setLength(0);
            }else if(s.charAt(i)==')'){
                sb.reverse();
                sb.insert(0,stack.pop());
            }else{
                sb.append(s.charAt(i));
            }
        }

        return sb.toString();
    }

    public static String solution2(String s){
        int[] pair = new int[s.length()];
        Stack<Integer> stack = new Stack<>();
        for(int i = 0; i< s.length();i++){
            if(s.charAt(i)=='('){
                stack.push(i);
            }else if(s.charAt(i)==')'){
                int j = stack.pop();
                pair[i] = j;
                pair[j] = i;
            }
        }

        StringBuffer sb = new StringBuffer();
        int index = 0;
        int step = 1;
        while(index <s.length()){
            if(s.charAt(index)=='(' || s.charAt(index)==')'){
                index = pair[index];
                step = -step;
            }else {
                sb.append(s.charAt(index));
            }
            index+=step;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(solution("(ed(et(oc))el)"));
        System.out.println(solution2("(ed(et(oc))el)"));
    }
}
