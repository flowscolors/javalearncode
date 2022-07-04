import crds.Dummy;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;

public class KubernetesCRDDemo {
    public static void main(String[] args) {
        Pod pod = new Pod();
        System.out.println(Serialization.asYaml(pod));

        CustomResourceDefinition dummyCRD = null;
        final String dummyCRDName = CustomResource.getCRDName(Dummy.class);
        dummyCRD = CustomResourceDefinitionContext.v1CRDFromCustomResourceType(Dummy.class).build();
        //client.apiextensions().v1beta1().customResourceDefinitions().create(dummyCRD);
        System.out.println("Created CRD " + dummyCRD.getMetadata().getName());
        System.out.println(Serialization.asYaml(dummyCRD));
    }
}
