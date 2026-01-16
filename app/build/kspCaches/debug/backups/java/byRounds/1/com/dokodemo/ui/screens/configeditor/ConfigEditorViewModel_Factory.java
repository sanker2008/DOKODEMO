package com.dokodemo.ui.screens.configeditor;

import com.dokodemo.data.repository.ServerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ConfigEditorViewModel_Factory implements Factory<ConfigEditorViewModel> {
  private final Provider<ServerRepository> serverRepositoryProvider;

  public ConfigEditorViewModel_Factory(Provider<ServerRepository> serverRepositoryProvider) {
    this.serverRepositoryProvider = serverRepositoryProvider;
  }

  @Override
  public ConfigEditorViewModel get() {
    return newInstance(serverRepositoryProvider.get());
  }

  public static ConfigEditorViewModel_Factory create(
      Provider<ServerRepository> serverRepositoryProvider) {
    return new ConfigEditorViewModel_Factory(serverRepositoryProvider);
  }

  public static ConfigEditorViewModel newInstance(ServerRepository serverRepository) {
    return new ConfigEditorViewModel(serverRepository);
  }
}
