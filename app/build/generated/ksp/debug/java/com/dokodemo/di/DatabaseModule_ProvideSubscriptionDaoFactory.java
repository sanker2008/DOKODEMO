package com.dokodemo.di;

import com.dokodemo.data.AppDatabase;
import com.dokodemo.data.dao.SubscriptionDao;
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
public final class DatabaseModule_ProvideSubscriptionDaoFactory implements Factory<SubscriptionDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideSubscriptionDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SubscriptionDao get() {
    return provideSubscriptionDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSubscriptionDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSubscriptionDaoFactory(databaseProvider);
  }

  public static SubscriptionDao provideSubscriptionDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSubscriptionDao(database));
  }
}
