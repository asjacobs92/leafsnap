package edu.maryland.leafsnap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.DatabaseInfo;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.model.Species;

/**
 * Database helper, extending {@link OrmLiteSqliteOpenHelper}, to create and upgrade the SQLite Database of the
 * application. The Tables are created based on the Classes in {@link edu.maryland.leafsnap.model}.
 *
 * @author Arthur Jacobs
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "DATABASE";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Leaflet-Master";

    private static final String ASSET_DB_PATH = "databases";
    //private static final String ASSET_SPECIES_PATH = "species";

    private Context mContext = null;

    private String mAssetPath = null;

    private String mDatabasePath = null;

    private boolean mIsInitializing = false;

    private SQLiteDatabase mDatabase = null;

    private Dao<Species, Integer> speciesDao = null;

    private Dao<LeafletUrl, Integer> leafletUrlDao = null;

    private Dao<DatabaseInfo, Integer> databaseInfoDao = null;

    private Dao<RankedSpecies, Integer> rankedSpeciesDao = null;

    private Dao<CollectedLeaf, Integer> collectedLeafDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        setContext(context);

        setAssetPath(ASSET_DB_PATH + "/" + DATABASE_NAME + ".sqlite");
        setDatabasePath(context.getApplicationInfo().dataDir + "/databases");
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Species.class);
            TableUtils.createTable(connectionSource, LeafletUrl.class);
            TableUtils.createTable(connectionSource, DatabaseInfo.class);
            TableUtils.createTable(connectionSource, RankedSpecies.class);
            TableUtils.createTable(connectionSource, CollectedLeaf.class);
            Log.d("DATABASE", "DATABASE CREATED SUCCESFULLY.");
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Species.class, true);
            TableUtils.dropTable(connectionSource, LeafletUrl.class, true);
            TableUtils.dropTable(connectionSource, DatabaseInfo.class, true);
            TableUtils.dropTable(connectionSource, RankedSpecies.class, true);
            TableUtils.dropTable(connectionSource, CollectedLeaf.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVersion + " to new "
                    + newVersion, e);
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (getDatabase() != null && getDatabase().isOpen() && !getDatabase().isReadOnly()) {
            return getDatabase();  // The database is already open for business
        }

        if (isInitializing()) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        try {
            setIsInitializing(true);
            db = createOrOpenDatabase(false);

            onOpen(db);
            success = true;
            return db;
        } finally {
            setIsInitializing(false);
            if (success) {
                if (getDatabase() != null) {
                    getDatabase().close();
                }
                setDatabase(db);
            } else {
                if (db != null) db.close();
            }
        }
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (getDatabase() != null && getDatabase().isOpen()) {
            return getDatabase();  // The database is already open for business
        }

        if (isInitializing()) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Couldn't open " + DATABASE_NAME + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            setIsInitializing(true);
            String path = getContext().getDatabasePath(DATABASE_NAME).getPath();
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

            onOpen(db);
            Log.w(TAG, "Opened " + DATABASE_NAME + " in read-only mode");
            setDatabase(db);
            return getDatabase();
        } finally {
            setIsInitializing(false);
            if (db != null && db != getDatabase()) db.close();
        }
    }

    /**
     * Close any open database object.
     */
    @Override
    public synchronized void close() {
        if (isInitializing()) throw new IllegalStateException("Closed during initialization");

        if (getDatabase() != null && getDatabase().isOpen()) {
            getDatabase().close();
            setDatabase(null);
        }
    }

    private SQLiteDatabase createOrOpenDatabase(boolean force) {
        // test for the existence of the db file first and don't attempt open
        // to prevent the error trace in log on API 14+
        SQLiteDatabase db = null;
        File file = new File(getDatabasePath() + "/" + DATABASE_NAME);
        if (file.exists()) {
            db = returnDatabase();
        }

        if (db == null) {
            // database does not exist, copy it from assets and return it
            copyDatabaseFromAssets();
            //copyAssetsToExternalStorage(ASSET_SPECIES_PATH);
            db = returnDatabase();
        }

        return db;
    }

    private SQLiteDatabase returnDatabase() {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(getDatabasePath() + "/" + DATABASE_NAME + ".sqlite", null, SQLiteDatabase.OPEN_READWRITE);
            Log.i(TAG, "successfully opened database " + DATABASE_NAME);
            return db;
        } catch (SQLiteException e) {
            Log.w(TAG, "could not open database " + DATABASE_NAME + " - " + e.getMessage());
            return null;
        }
    }

    private void copyDatabaseFromAssets() {
        Log.w(TAG, "copying database from assets...");

        InputStream is = null;
        try {
            is = getContext().getAssets().open(getAssetPath());
            File f = new File(getDatabasePath() + "/");
            if (!f.exists()) {
                f.mkdir();
            }

            writeFileToDisk(is, new FileOutputStream(getDatabasePath() + "/" + DATABASE_NAME + ".sqlite"));

            Log.w(TAG, "database copy complete");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private void copyAssetsToExternalStorage(String path) {
        AssetManager assetManager = getContext().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                InputStream is = getContext().getAssets().open(path);
                File outFile = new File(getContext().getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), path);
                OutputStream os = new FileOutputStream(outFile);
                writeFileToDisk(is, os);
            } else {
                File dir = new File(getContext().getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), path);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyAssetsToExternalStorage(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("TAG", "I/O Exception", ex);
        }
    }*/

    private void writeFileToDisk(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }

    public Dao<Species, Integer> getSpeciesDao() throws SQLException {
        if (speciesDao == null) {
            speciesDao = getDao(Species.class);
        }
        return speciesDao;
    }

    public Dao<LeafletUrl, Integer> getLeafletUrlDao() throws SQLException {
        if (leafletUrlDao == null) {
            leafletUrlDao = getDao(LeafletUrl.class);
        }
        return leafletUrlDao;
    }

    public Dao<DatabaseInfo, Integer> getDatabaseInfoDao() throws SQLException {
        if (databaseInfoDao == null) {
            databaseInfoDao = getDao(DatabaseInfo.class);
        }
        return databaseInfoDao;
    }

    public Dao<RankedSpecies, Integer> getRankedSpeciesDao() throws SQLException {
        if (rankedSpeciesDao == null) {
            rankedSpeciesDao = getDao(RankedSpecies.class);
        }
        return rankedSpeciesDao;
    }

    public Dao<CollectedLeaf, Integer> getCollectedLeafDao() throws SQLException {
        if (collectedLeafDao == null) {
            collectedLeafDao = getDao(CollectedLeaf.class);
        }
        return collectedLeafDao;
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private String getDatabasePath() {
        return mDatabasePath;
    }

    private void setDatabasePath(String mDatabasePath) {
        this.mDatabasePath = mDatabasePath;
    }

    private String getAssetPath() {
        return mAssetPath;
    }

    private void setAssetPath(String mAssetPath) {
        this.mAssetPath = mAssetPath;
    }

    private SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    private void setDatabase(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    private boolean isInitializing() {
        return mIsInitializing;
    }

    private void setIsInitializing(boolean mIsInitializing) {
        this.mIsInitializing = mIsInitializing;
    }
}
