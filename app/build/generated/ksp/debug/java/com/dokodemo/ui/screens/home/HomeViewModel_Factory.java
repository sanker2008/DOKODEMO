package com.dokodemo.ui.screens.home;

import android.content.Context;
import com.dokodemo.data.repository.ServerRepository;
import com.dokodemo.service.VpnController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<VpnController> vpnControllerProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  public HomeViewModel_Factory(Provider<Context> contextProvider,
      Provider<VpnController> vpnControllerProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.vpnControllerProvider = vpnControllerProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(contextProvider.get(), vpnControllerProvider.get(), serverRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<Context> contextProvider,
      Provider<VpnController> vpnControllerProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    return new HomeViewModel_Factory(contextProvider, vpnControllerProvider, serverRepositoryProvider);
  }

  public static HomeViewModel newInstance(Context context, VpnController vpnController,
      ServerRepository serverRepository) {
    return new HomeViewModel(context, vpnController, serverRepository);
  }
}
