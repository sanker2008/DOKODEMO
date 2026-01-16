package com.dokodemo.data.repository;

import com.dokodemo.data.dao.ServerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ServerRepository_Factory implements Factory<ServerRepository> {
  private final Provider<ServerDao> serverDaoProvider;

  public ServerRepository_Factory(Provider<ServerDao> serverDaoProvider) {
    this.serverDaoProvider = serverDaoProvider;
  }

  @Override
  public ServerRepository get() {
    return newInstance(serverDaoProvider.get());
  }

  public static ServerRepository_Factory create(Provider<ServerDao> serverDaoProvider) {
    return new ServerRepository_Factory(serverDaoProvider);
  }

  public static ServerRepository newInstance(ServerDao serverDao) {
    return new ServerRepository(serverDao);
  }
}
