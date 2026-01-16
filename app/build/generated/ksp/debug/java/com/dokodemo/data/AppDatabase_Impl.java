package com.dokodemo.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.dokodemo.data.dao.ServerDao;
import com.dokodemo.data.dao.ServerDao_Impl;
import com.dokodemo.data.dao.SubscriptionDao;
import com.dokodemo.data.dao.SubscriptionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ServerDao _serverDao;

  private volatile SubscriptionDao _subscriptionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `server_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `port` INTEGER NOT NULL, `uuid` TEXT NOT NULL, `password` TEXT NOT NULL, `protocol` TEXT NOT NULL, `encryption` TEXT NOT NULL, `flow` TEXT NOT NULL, `useTls` INTEGER NOT NULL, `allowInsecure` INTEGER NOT NULL, `serverName` TEXT NOT NULL, `network` TEXT NOT NULL, `wsPath` TEXT NOT NULL, `wsHost` TEXT NOT NULL, `countryCode` TEXT NOT NULL, `countryName` TEXT NOT NULL, `latency` INTEGER, `isSelected` INTEGER NOT NULL, `lastConnected` INTEGER, `subscriptionId` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `subscriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `url` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `lastUpdated` INTEGER, `serverCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1cd3ab01ea2bf65c7c0e24bf4cf09870')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `server_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `subscriptions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsServerProfiles = new HashMap<String, TableInfo.Column>(23);
        _columnsServerProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("port", new TableInfo.Column("port", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("uuid", new TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("password", new TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("protocol", new TableInfo.Column("protocol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("encryption", new TableInfo.Column("encryption", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("flow", new TableInfo.Column("flow", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("useTls", new TableInfo.Column("useTls", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("allowInsecure", new TableInfo.Column("allowInsecure", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("serverName", new TableInfo.Column("serverName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("network", new TableInfo.Column("network", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("wsPath", new TableInfo.Column("wsPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("wsHost", new TableInfo.Column("wsHost", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("countryCode", new TableInfo.Column("countryCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("countryName", new TableInfo.Column("countryName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("latency", new TableInfo.Column("latency", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("isSelected", new TableInfo.Column("isSelected", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("lastConnected", new TableInfo.Column("lastConnected", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("subscriptionId", new TableInfo.Column("subscriptionId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerProfiles.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServerProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesServerProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoServerProfiles = new TableInfo("server_profiles", _columnsServerProfiles, _foreignKeysServerProfiles, _indicesServerProfiles);
        final TableInfo _existingServerProfiles = TableInfo.read(db, "server_profiles");
        if (!_infoServerProfiles.equals(_existingServerProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "server_profiles(com.dokodemo.data.model.ServerProfile).\n"
                  + " Expected:\n" + _infoServerProfiles + "\n"
                  + " Found:\n" + _existingServerProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsSubscriptions = new HashMap<String, TableInfo.Column>(8);
        _columnsSubscriptions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("url", new TableInfo.Column("url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("serverCount", new TableInfo.Column("serverCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubscriptions.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSubscriptions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSubscriptions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSubscriptions = new TableInfo("subscriptions", _columnsSubscriptions, _foreignKeysSubscriptions, _indicesSubscriptions);
        final TableInfo _existingSubscriptions = TableInfo.read(db, "subscriptions");
        if (!_infoSubscriptions.equals(_existingSubscriptions)) {
          return new RoomOpenHelper.ValidationResult(false, "subscriptions(com.dokodemo.data.model.Subscription).\n"
                  + " Expected:\n" + _infoSubscriptions + "\n"
                  + " Found:\n" + _existingSubscriptions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1cd3ab01ea2bf65c7c0e24bf4cf09870", "a0f5436e0bae16fad5c467ce1ed981c3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "server_profiles","subscriptions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `server_profiles`");
      _db.execSQL("DELETE FROM `subscriptions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ServerDao.class, ServerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SubscriptionDao.class, SubscriptionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ServerDao serverDao() {
    if (_serverDao != null) {
      return _serverDao;
    } else {
      synchronized(this) {
        if(_serverDao == null) {
          _serverDao = new ServerDao_Impl(this);
        }
        return _serverDao;
      }
    }
  }

  @Override
  public SubscriptionDao subscriptionDao() {
    if (_subscriptionDao != null) {
      return _subscriptionDao;
    } else {
      synchronized(this) {
        if(_subscriptionDao == null) {
          _subscriptionDao = new SubscriptionDao_Impl(this);
        }
        return _subscriptionDao;
      }
    }
  }
}
