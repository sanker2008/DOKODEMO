package com.dokodemo.service;

import com.dokodemo.core.CoreManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DokoDemoVpnService_MembersInjector implements MembersInjector<DokoDemoVpnService> {
  private final Provider<CoreManager> coreManagerProvider;

  public DokoDemoVpnService_MembersInjector(Provider<CoreManager> coreManagerProvider) {
    this.coreManagerProvider = coreManagerProvider;
  }

  public static MembersInjector<DokoDemoVpnService> create(
      Provider<CoreManager> coreManagerProvider) {
    return new DokoDemoVpnService_MembersInjector(coreManagerProvider);
  }

  @Override
  public void injectMembers(DokoDemoVpnService instance) {
    injectCoreManager(instance, coreManagerProvider.get());
  }

  @InjectedFieldSignature("com.dokodemo.service.DokoDemoVpnService.coreManager")
  public static void injectCoreManager(DokoDemoVpnService instance, CoreManager coreManager) {
    instance.coreManager = coreManager;
  }
}
