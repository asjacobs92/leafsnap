package edu.maryland.leafsnap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * LeafletUrl object used by ORMLite to create the LeafletUrl table in the Application Database.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/> ORMLite </a>
 */
@DatabaseTable
public class LeafletUrl implements Serializable {

    private static final long serialVersionUID = 8369290996737561306L;

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private int order;
    @DatabaseField
    private String rawURL;
    @DatabaseField
    private String type;
    @DatabaseField
    private String dataSource;
    @DatabaseField
    private String thumbnailLocation;
    @DatabaseField
    private String hiResImageLocation;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Species associatedSpecies;
    @DatabaseField(foreign = true)
    private Species revExampleImageLeaf;
    @DatabaseField(foreign = true)
    private Species revExampleImageFlower;
    @DatabaseField(foreign = true)
    private Species revExampleImageFruit;
    @DatabaseField(foreign = true)
    private CollectedLeaf revOriginalImageURL;
    @DatabaseField(foreign = true)
    private CollectedLeaf revSegmentedImageURL;

    public LeafletUrl() {
    }

    public LeafletUrl(String rawURL) {
        setRawURL(rawURL);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getRawURL() {
        return rawURL;
    }

    public void setRawURL(String rawURL) {
        this.rawURL = rawURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getThumbnailLocation() {
        return thumbnailLocation;
    }

    public void setThumbnailLocation(String thumbnailLocation) {
        this.thumbnailLocation = thumbnailLocation;
    }

    public String getHiResImageLocation() {
        return hiResImageLocation;
    }

    public void setHiResImageLocation(String hiResImageLocation) {
        this.hiResImageLocation = hiResImageLocation;
    }

    public Species getAssociatedSpecies() {
        return associatedSpecies;
    }

    public void setAssociatedSpecies(Species associatedSpecies) {
        this.associatedSpecies = associatedSpecies;
    }

    public Species getRevExampleImageLeaf() {
        return revExampleImageLeaf;
    }

    public void setRevExampleImageLeaf(Species revExampleImageLeaf) {
        this.revExampleImageLeaf = revExampleImageLeaf;
    }

    public Species getRevExampleImageFlower() {
        return revExampleImageFlower;
    }

    public void setRevExampleImageFlower(Species revExampleImageFlower) {
        this.revExampleImageFlower = revExampleImageFlower;
    }

    public Species getRevExampleImageFruit() {
        return revExampleImageFruit;
    }

    public void setRevExampleImageFruit(Species revExampleImageFruit) {
        this.revExampleImageFruit = revExampleImageFruit;
    }

    public CollectedLeaf getRevOriginalImageURL() {
        return revOriginalImageURL;
    }

    public void setRevOriginalImageURL(CollectedLeaf revOriginalImageURL) {
        this.revOriginalImageURL = revOriginalImageURL;
    }

    public CollectedLeaf getRevSegmentedImageURL() {
        return revSegmentedImageURL;
    }

    public void setRevSegmentedImageURL(CollectedLeaf revSegmentedImageURL) {
        this.revSegmentedImageURL = revSegmentedImageURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
