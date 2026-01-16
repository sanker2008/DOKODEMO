package com.dokodemo.ui.screens.settings;

import com.dokodemo.core.CoreManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<CoreManager> coreManagerProvider;

  public SettingsViewModel_Factory(Provider<CoreManager> coreManagerProvider) {
    this.coreManagerProvider = coreManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(coreManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<CoreManager> coreManagerProvider) {
    return new SettingsViewModel_Factory(coreManagerProvider);
  }

  public static SettingsViewModel newInstance(CoreManager coreManager) {
    return new SettingsViewModel(coreManager);
  }
}
