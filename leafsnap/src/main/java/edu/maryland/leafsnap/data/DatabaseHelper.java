package edu.maryland.leafsnap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

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

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Leaflet-Master";

    private Dao<Species, Integer> speciesDao;
    private Dao<LeafletUrl, Integer> leafletUrlDao;
    private Dao<DatabaseInfo, Integer> databaseInfoDao;
    private Dao<RankedSpecies, Integer> rankedSpeciesDao;
    private Dao<CollectedLeaf, Integer> collectedLeafDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
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

}
