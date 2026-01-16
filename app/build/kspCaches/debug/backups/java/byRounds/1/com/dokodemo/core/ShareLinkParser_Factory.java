package com.dokodemo.core;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ShareLinkParser_Factory implements Factory<ShareLinkParser> {
  @Override
  public ShareLinkParser get() {
    return newInstance();
  }

  public static ShareLinkParser_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ShareLinkParser newInstance() {
    return new ShareLinkParser();
  }

  private static final class InstanceHolder {
    private static final ShareLinkParser_Factory INSTANCE = new ShareLinkParser_Factory();
  }
}
