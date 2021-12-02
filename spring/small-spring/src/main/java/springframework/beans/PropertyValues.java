package springframework.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flowscolors
 * @date 2021-11-08 13:26
 */
public class PropertyValues {
    //这里如何使用hashmap还是araaylist 多个同样propertyName是否会直接返回
    //参照org.springframework:spring-beans 的 MutablePropertyValues 也是一个ArrayList
    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public void addPropertyValue(PropertyValue propertyValue){
        this.propertyValueList.add(propertyValue);
    }

    public PropertyValue getPropertyValue(String propertyName){
        for(PropertyValue propertyValue:this.propertyValueList){
            if(propertyValue.getName().equals(propertyName)){
                return propertyValue;
            }
        }
        return  null;
    }

    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[0]);
    }
}
