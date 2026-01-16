package com.dokodemo.service;

import android.content.Context;
import com.dokodemo.core.CoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class VpnController_Factory implements Factory<VpnController> {
  private final Provider<Context> contextProvider;

  private final Provider<CoreManager> coreManagerProvider;

  public VpnController_Factory(Provider<Context> contextProvider,
      Provider<CoreManager> coreManagerProvider) {
    this.contextProvider = contextProvider;
    this.coreManagerProvider = coreManagerProvider;
  }

  @Override
  public VpnController get() {
    return newInstance(contextProvider.get(), coreManagerProvider.get());
  }

  public static VpnController_Factory create(Provider<Context> contextProvider,
      Provider<CoreManager> coreManagerProvider) {
    return new VpnController_Factory(contextProvider, coreManagerProvider);
  }

  public static VpnController newInstance(Context context, CoreManager coreManager) {
    return new VpnController(context, coreManager);
  }
}
