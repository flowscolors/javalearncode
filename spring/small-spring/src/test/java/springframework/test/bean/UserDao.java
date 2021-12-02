package springframework.test.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-11-08 14:34
 */
public class UserDao {

    private static Map<String, String> hashMap = new HashMap<String, String>();

    static {
        hashMap.put("10001", "joker");
        hashMap.put("10002", "king");
        hashMap.put("10003", "zero");
    }

    public void initDataMethod(){
        System.out.println("执行：init-method");
        hashMap.put("10004", "kk");
        hashMap.put("10005", "aa");
        hashMap.put("10006", "pp");
        System.out.println("Map 内容: " + hashMap.toString());
    }

    public void destroyDataMethod(){
        System.out.println("执行：destroy-method");
        hashMap.clear();
        System.out.println("Map 内容: " + hashMap.toString());
    }


    public String queryUserName(String uId) {
        return hashMap.get(uId);
    }
}
