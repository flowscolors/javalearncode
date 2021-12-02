package jvm.rtda.heap.methodarea;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flowscolors
 * @date 2021-11-12 14:55
 */
public class MethodDescriptor {

    //方法描述 包括一个list作为入参，一个返回值类型
    public List<String> parameterTypes = new ArrayList<>();
    public String returnType;

    public void addParameterType(String type){
        this.parameterTypes.add(type);
    }
}
