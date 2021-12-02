import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author flowscolors
 * @date 2021-10-04 16:45
 */
public class CombineBeanDemo {
    public static void main(String[] args) {
        Deployment deployment1 = new DeploymentBuilder().withNewMetadata()
                .withName("nginx").endMetadata().build();
        System.out.println(deployment1.getMetadata().toString());

        Deployment deployment2 = new DeploymentBuilder().withNewMetadata()
                .withNamespace("kube-system").endMetadata().build();
        System.out.println(deployment2.getMetadata().toString());

        Deployment deployment3 = combinebean(deployment1,deployment2);
        System.out.println(deployment3.getMetadata().toString());

    }

    //wait check 代码需要继续调试，namespace没有合并
    public static <T> T combinebean(T sourceBean, T targetBean){
        Class sourceBeanClass = sourceBean.getClass();
        Class targetBeanClass = targetBean.getClass();
        Field[] sourceFields = sourceBeanClass.getDeclaredFields();
        Field[] targetFields = targetBeanClass.getDeclaredFields();
        for(int i=0; i<sourceFields.length; i++){
            Field sourceField = sourceFields[i];
            if(Modifier.isStatic(sourceField.getModifiers())){
                continue;
            }
            Field targetField = targetFields[i];
            if(Modifier.isStatic(targetField.getModifiers())){
                continue;
            }
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            try {
                if( !(sourceField.get(sourceBean) == null) &&  !"serialVersionUID".equals(sourceField.getName())){
                    targetField.set(targetBean, sourceField.get(sourceBean));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return targetBean;
    }
}
