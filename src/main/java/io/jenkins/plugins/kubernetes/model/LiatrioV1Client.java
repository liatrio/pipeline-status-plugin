package io.jenkins.plugins.kubernetes.model;

import java.util.function.Supplier;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.BaseClient;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

public class LiatrioV1Client extends BaseClient implements Client {
  public static final String LIATRIO_GROUP = "sdm.liatr.io";
  public static final String LIATRIO_VERSION = "v1alpha1";
  public static final String KIND = "Build";
  public static final String NAME_SINGULAR = KIND.toLowerCase();
  public static final String NAME_PLURAL = NAME_SINGULAR+"s";
  public static final String CRD_NAME = NAME_PLURAL+"."+LIATRIO_GROUP;

  static {
    KubernetesDeserializer.registerCustomKind(LIATRIO_GROUP + "/"+LIATRIO_VERSION, KIND, LiatrioV1Build.class);
    //KubernetesDeserializer.registerCustomKind(LIATRIO_GROUP + "/"+LIATRIO_VERSION, KIND+"List", LiatrioV1BuildList.class);
  }

  private MixedOperation<LiatrioV1Build, LiatrioV1BuildList, LiatrioV1BuildDoneable, Resource<LiatrioV1Build, LiatrioV1BuildDoneable>> builds;

  public LiatrioV1Client(KubernetesClient client) {
    builds = client.customResources(getBuildCRD(client), LiatrioV1Build.class, LiatrioV1BuildList.class, LiatrioV1BuildDoneable.class);
  }

  public MixedOperation<LiatrioV1Build, LiatrioV1BuildList, LiatrioV1BuildDoneable, Resource<LiatrioV1Build, LiatrioV1BuildDoneable>> builds() {
    return builds;
  }

  private static Supplier<CustomResourceDefinition> createBuildCRD(KubernetesClient client) {
    CustomResourceDefinition crd = 
            new CustomResourceDefinitionBuilder()
                .withApiVersion("apiextensions.k8s.io/v1beta1")
                .withNewMetadata()
                  .withName(CRD_NAME)
                .endMetadata()
                .withNewSpec()
                  .withGroup(LIATRIO_GROUP)
                  .withVersion(LIATRIO_VERSION)
                  .withScope("Namespaced")
                  .withNewNames()
                    .withKind(KIND)
                    .withSingular(NAME_SINGULAR)
                    .withPlural(NAME_PLURAL)
                  .endNames()
                .endSpec()
                .build();

    return () -> {
      client.customResourceDefinitions().create(crd);
      return crd;
    };
  }

  private static CustomResourceDefinition getBuildCRD(KubernetesClient client) {
    CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
    return crds.getItems()
               .stream()
               .filter(crd -> crd.getMetadata() != null)
               .filter(crd -> crd.getMetadata().getName().equals(CRD_NAME))
               .findFirst()
               .orElseGet(createBuildCRD(client));
  }
}
