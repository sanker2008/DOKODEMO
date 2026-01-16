package com.dokodemo.ui.screens.splittunneling;

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
public final class SplitTunnelingViewModel_Factory implements Factory<SplitTunnelingViewModel> {
  @Override
  public SplitTunnelingViewModel get() {
    return newInstance();
  }

  public static SplitTunnelingViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SplitTunnelingViewModel newInstance() {
    return new SplitTunnelingViewModel();
  }

  private static final class InstanceHolder {
    private static final SplitTunnelingViewModel_Factory INSTANCE = new SplitTunnelingViewModel_Factory();
  }
}
