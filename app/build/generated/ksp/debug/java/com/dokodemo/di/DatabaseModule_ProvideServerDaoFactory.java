package com.dokodemo.di;

import com.dokodemo.data.AppDatabase;
import com.dokodemo.data.dao.ServerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideServerDaoFactory implements Factory<ServerDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideServerDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ServerDao get() {
    return provideServerDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideServerDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideServerDaoFactory(databaseProvider);
  }

  public static ServerDao provideServerDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideServerDao(database));
  }
}
