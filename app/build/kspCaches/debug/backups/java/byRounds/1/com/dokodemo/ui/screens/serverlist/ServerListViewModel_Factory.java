package com.dokodemo.ui.screens.serverlist;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ServerListViewModel_Factory implements Factory<ServerListViewModel> {
  @Override
  public ServerListViewModel get() {
    return newInstance();
  }

  public static ServerListViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ServerListViewModel newInstance() {
    return new ServerListViewModel();
  }

  private static final class InstanceHolder {
    private static final ServerListViewModel_Factory INSTANCE = new ServerListViewModel_Factory();
  }
}
