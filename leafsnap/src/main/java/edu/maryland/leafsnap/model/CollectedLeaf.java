package edu.maryland.leafsnap.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * CollectedLeaf object used by ORMLite to create the CollectedLeaf table in the Application Database.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/> ORMLite </a>
 */
@DatabaseTable
public class CollectedLeaf implements Serializable {

    private static final long serialVersionUID = 3262946905098294189L;
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private long leafID;
    @DatabaseField
    private String altitude;
    @DatabaseField
    private String latitude;
    @DatabaseField
    private String longitude;
    @DatabaseField
    private boolean uploaded;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date collectedDate;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date lastModified;
    @DatabaseField
    private SyncStatus syncStatus;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Species selectedSpeciesRel;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LeafletUrl originalImageURL;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LeafletUrl segmentedImageURL;

    public CollectedLeaf() {

    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public long getLeafID() {
        return leafID;
    }

    public void setLeafID(long leafID) {
        this.leafID = leafID;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Date getCollectedDate() {
        return collectedDate;
    }

    public void setCollectedDate(Date collectedDate) {
        this.collectedDate = collectedDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Species getSelectedSpeciesRel() {
        return selectedSpeciesRel;
    }

    public void setSelectedSpeciesRel(Species selectedSpeciesRel) {
        this.selectedSpeciesRel = selectedSpeciesRel;
    }

    public LeafletUrl getOriginalImageURL() {
        return originalImageURL;
    }

    public void setOriginalImageURL(LeafletUrl originalImageURL) {
        this.originalImageURL = originalImageURL;
    }

    public LeafletUrl getSegmentedImageURL() {
        return segmentedImageURL;
    }

    public void setSegmentedImageURL(LeafletUrl segmentedImageURL) {
        this.segmentedImageURL = segmentedImageURL;
    }

    public enum SyncStatus {
        SERVER_IS_NEWER, SAME, PHONE_IS_NEWER;
    }
}
