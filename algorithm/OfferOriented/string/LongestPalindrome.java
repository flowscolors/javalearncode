package string;

/**
 * @author flowscolors
 * @date 2022-02-21 13:54
 */
public class LongestPalindrome {

    public static String solution(String input){
        String maxString = "" ;
        for(int i =0;i<input.length();i++){
            for(int j =i;j<input.length();j++){
                if(isPalindrome(input.substring(i,j)) && input.substring(i,j).length() > maxString.length()){
                    maxString = input.substring(i,j);
                }
            }
        }
        return maxString;
    }

    public static Boolean isPalindrome(String input){
        int start = 0;
        int end = input.length()-1;
        for(int i = 0;i< input.length()/2;i++){
            if(input.charAt(start+i)!=input.charAt(end-i)){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String string1 = "banana";
        String string2 = "cbbd";
        System.out.println(solution(string1));
        System.out.println(solution(string2));
        System.out.println(isPalindrome("abcefba"));
    }
}
