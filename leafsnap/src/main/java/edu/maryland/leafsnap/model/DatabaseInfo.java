package edu.maryland.leafsnap.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * DatabaseInfo object used by ORMLite to create the DatabaseInfo table in the Application Database.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/> ORMLite </a>
 */
@DatabaseTable
public class DatabaseInfo implements Serializable {

    private static final long serialVersionUID = 775689121372891278L;

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private float version;
    @DatabaseField
    private boolean fullyLoaded;
    @DatabaseField(version = true, dataType = DataType.DATE_STRING)
    private Date lastUpdate;

    public DatabaseInfo() {

    }

    public DatabaseInfo(float version, boolean fullyLoaded) {
        setVersion(version);
        setFullyLoaded(fullyLoaded);
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
