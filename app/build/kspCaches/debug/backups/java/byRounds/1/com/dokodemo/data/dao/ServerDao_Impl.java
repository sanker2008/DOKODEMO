package com.dokodemo.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.dokodemo.data.model.Converters;
import com.dokodemo.data.model.Protocol;
import com.dokodemo.data.model.ServerProfile;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ServerDao_Impl implements ServerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ServerProfile> __insertionAdapterOfServerProfile;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ServerProfile> __deletionAdapterOfServerProfile;

  private final EntityDeletionOrUpdateAdapter<ServerProfile> __updateAdapterOfServerProfile;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySubscription;

  private final SharedSQLiteStatement __preparedStmtOfClearSelection;

  private final SharedSQLiteStatement __preparedStmtOfSelectServer;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLatency;

  public ServerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfServerProfile = new EntityInsertionAdapter<ServerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `server_profiles` (`id`,`name`,`address`,`port`,`uuid`,`password`,`protocol`,`encryption`,`flow`,`useTls`,`allowInsecure`,`serverName`,`network`,`wsPath`,`wsHost`,`countryCode`,`countryName`,`latency`,`isSelected`,`lastConnected`,`subscriptionId`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getAddress());
        statement.bindLong(4, entity.getPort());
        statement.bindString(5, entity.getUuid());
        statement.bindString(6, entity.getPassword());
        final String _tmp = __converters.fromProtocol(entity.getProtocol());
        statement.bindString(7, _tmp);
        statement.bindString(8, entity.getEncryption());
        statement.bindString(9, entity.getFlow());
        final int _tmp_1 = entity.getUseTls() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getAllowInsecure() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindString(12, entity.getServerName());
        statement.bindString(13, entity.getNetwork());
        statement.bindString(14, entity.getWsPath());
        statement.bindString(15, entity.getWsHost());
        statement.bindString(16, entity.getCountryCode());
        statement.bindString(17, entity.getCountryName());
        if (entity.getLatency() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getLatency());
        }
        final int _tmp_3 = entity.isSelected() ? 1 : 0;
        statement.bindLong(19, _tmp_3);
        if (entity.getLastConnected() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getLastConnected());
        }
        if (entity.getSubscriptionId() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getSubscriptionId());
        }
        statement.bindLong(22, entity.getCreatedAt());
        statement.bindLong(23, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfServerProfile = new EntityDeletionOrUpdateAdapter<ServerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `server_profiles` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerProfile entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfServerProfile = new EntityDeletionOrUpdateAdapter<ServerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `server_profiles` SET `id` = ?,`name` = ?,`address` = ?,`port` = ?,`uuid` = ?,`password` = ?,`protocol` = ?,`encryption` = ?,`flow` = ?,`useTls` = ?,`allowInsecure` = ?,`serverName` = ?,`network` = ?,`wsPath` = ?,`wsHost` = ?,`countryCode` = ?,`countryName` = ?,`latency` = ?,`isSelected` = ?,`lastConnected` = ?,`subscriptionId` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getAddress());
        statement.bindLong(4, entity.getPort());
        statement.bindString(5, entity.getUuid());
        statement.bindString(6, entity.getPassword());
        final String _tmp = __converters.fromProtocol(entity.getProtocol());
        statement.bindString(7, _tmp);
        statement.bindString(8, entity.getEncryption());
        statement.bindString(9, entity.getFlow());
        final int _tmp_1 = entity.getUseTls() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getAllowInsecure() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindString(12, entity.getServerName());
        statement.bindString(13, entity.getNetwork());
        statement.bindString(14, entity.getWsPath());
        statement.bindString(15, entity.getWsHost());
        statement.bindString(16, entity.getCountryCode());
        statement.bindString(17, entity.getCountryName());
        if (entity.getLatency() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getLatency());
        }
        final int _tmp_3 = entity.isSelected() ? 1 : 0;
        statement.bindLong(19, _tmp_3);
        if (entity.getLastConnected() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getLastConnected());
        }
        if (entity.getSubscriptionId() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getSubscriptionId());
        }
        statement.bindLong(22, entity.getCreatedAt());
        statement.bindLong(23, entity.getUpdatedAt());
        statement.bindLong(24, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM server_profiles WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBySubscription = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM server_profiles WHERE subscriptionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearSelection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE server_profiles SET isSelected = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSelectServer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE server_profiles SET isSelected = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLatency = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE server_profiles SET latency = ?, updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ServerProfile server, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfServerProfile.insertAndReturnId(server);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<ServerProfile> servers,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfServerProfile.insert(servers);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ServerProfile server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfServerProfile.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ServerProfile server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfServerProfile.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBySubscription(final long subscriptionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBySubscription.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, subscriptionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteBySubscription.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearSelection(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSelection.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearSelection.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object selectServer(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSelectServer.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSelectServer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLatency(final long id, final Integer latency, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLatency.acquire();
        int _argIndex = 1;
        if (latency == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, latency);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLatency.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ServerProfile>> getAllServers() {
    final String _sql = "SELECT * FROM server_profiles ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"server_profiles"}, new Callable<List<ServerProfile>>() {
      @Override
      @NonNull
      public List<ServerProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ServerProfile> _result = new ArrayList<ServerProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServerProfile _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getServerById(final long id,
      final Continuation<? super ServerProfile> $completion) {
    final String _sql = "SELECT * FROM server_profiles WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ServerProfile>() {
      @Override
      @Nullable
      public ServerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ServerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ServerProfile> getServerByIdFlow(final long id) {
    final String _sql = "SELECT * FROM server_profiles WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"server_profiles"}, new Callable<ServerProfile>() {
      @Override
      @Nullable
      public ServerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ServerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSelectedServer(final Continuation<? super ServerProfile> $completion) {
    final String _sql = "SELECT * FROM server_profiles WHERE isSelected = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ServerProfile>() {
      @Override
      @Nullable
      public ServerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ServerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ServerProfile> getSelectedServerFlow() {
    final String _sql = "SELECT * FROM server_profiles WHERE isSelected = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"server_profiles"}, new Callable<ServerProfile>() {
      @Override
      @Nullable
      public ServerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ServerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ServerProfile>> getServersBySubscription(final long subscriptionId) {
    final String _sql = "SELECT * FROM server_profiles WHERE subscriptionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, subscriptionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"server_profiles"}, new Callable<List<ServerProfile>>() {
      @Override
      @NonNull
      public List<ServerProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ServerProfile> _result = new ArrayList<ServerProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServerProfile _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ServerProfile>> searchServers(final String query) {
    final String _sql = "SELECT * FROM server_profiles WHERE name LIKE '%' || ? || '%' OR address LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"server_profiles"}, new Callable<List<ServerProfile>>() {
      @Override
      @NonNull
      public List<ServerProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfEncryption = CursorUtil.getColumnIndexOrThrow(_cursor, "encryption");
          final int _cursorIndexOfFlow = CursorUtil.getColumnIndexOrThrow(_cursor, "flow");
          final int _cursorIndexOfUseTls = CursorUtil.getColumnIndexOrThrow(_cursor, "useTls");
          final int _cursorIndexOfAllowInsecure = CursorUtil.getColumnIndexOrThrow(_cursor, "allowInsecure");
          final int _cursorIndexOfServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "serverName");
          final int _cursorIndexOfNetwork = CursorUtil.getColumnIndexOrThrow(_cursor, "network");
          final int _cursorIndexOfWsPath = CursorUtil.getColumnIndexOrThrow(_cursor, "wsPath");
          final int _cursorIndexOfWsHost = CursorUtil.getColumnIndexOrThrow(_cursor, "wsHost");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfCountryName = CursorUtil.getColumnIndexOrThrow(_cursor, "countryName");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsSelected = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelected");
          final int _cursorIndexOfLastConnected = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnected");
          final int _cursorIndexOfSubscriptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "subscriptionId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ServerProfile> _result = new ArrayList<ServerProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServerProfile _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final Protocol _tmpProtocol;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfProtocol);
            _tmpProtocol = __converters.toProtocol(_tmp);
            final String _tmpEncryption;
            _tmpEncryption = _cursor.getString(_cursorIndexOfEncryption);
            final String _tmpFlow;
            _tmpFlow = _cursor.getString(_cursorIndexOfFlow);
            final boolean _tmpUseTls;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseTls);
            _tmpUseTls = _tmp_1 != 0;
            final boolean _tmpAllowInsecure;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAllowInsecure);
            _tmpAllowInsecure = _tmp_2 != 0;
            final String _tmpServerName;
            _tmpServerName = _cursor.getString(_cursorIndexOfServerName);
            final String _tmpNetwork;
            _tmpNetwork = _cursor.getString(_cursorIndexOfNetwork);
            final String _tmpWsPath;
            _tmpWsPath = _cursor.getString(_cursorIndexOfWsPath);
            final String _tmpWsHost;
            _tmpWsHost = _cursor.getString(_cursorIndexOfWsHost);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpCountryName;
            _tmpCountryName = _cursor.getString(_cursorIndexOfCountryName);
            final Integer _tmpLatency;
            if (_cursor.isNull(_cursorIndexOfLatency)) {
              _tmpLatency = null;
            } else {
              _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            }
            final boolean _tmpIsSelected;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSelected);
            _tmpIsSelected = _tmp_3 != 0;
            final Long _tmpLastConnected;
            if (_cursor.isNull(_cursorIndexOfLastConnected)) {
              _tmpLastConnected = null;
            } else {
              _tmpLastConnected = _cursor.getLong(_cursorIndexOfLastConnected);
            }
            final Long _tmpSubscriptionId;
            if (_cursor.isNull(_cursorIndexOfSubscriptionId)) {
              _tmpSubscriptionId = null;
            } else {
              _tmpSubscriptionId = _cursor.getLong(_cursorIndexOfSubscriptionId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ServerProfile(_tmpId,_tmpName,_tmpAddress,_tmpPort,_tmpUuid,_tmpPassword,_tmpProtocol,_tmpEncryption,_tmpFlow,_tmpUseTls,_tmpAllowInsecure,_tmpServerName,_tmpNetwork,_tmpWsPath,_tmpWsHost,_tmpCountryCode,_tmpCountryName,_tmpLatency,_tmpIsSelected,_tmpLastConnected,_tmpSubscriptionId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getServerCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM server_profiles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
