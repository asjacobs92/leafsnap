package edu.maryland.leafsnap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Species object used by ORMLite to create the Species table in the Application Database.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/> ORMLite </a>
 */
@DatabaseTable
public class Species implements Serializable, Comparable<Species> {

    private static final long serialVersionUID = -7598558514511455528L;

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private int dataset;
    @DatabaseField(unique = true)
    private String popularName;
    @DatabaseField
    private String description;
    @DatabaseField(unique = true)
    private String scientificName;
    @DatabaseField
    private String habitat;
    @DatabaseField
    private String bloom;
    @DatabaseField
    private String growth;
    @DatabaseField
    private String longevity;
    @DatabaseField
    private String presence;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LeafletUrl exampleImageFlower;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LeafletUrl exampleImageLeaf;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LeafletUrl exampleImageFruit;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<RankedSpecies> associatedRankedSpecies;

    public Species() {
        setDataset(0);
    }

    public Species(String popularName, String scientificName) {
        setDataset(0);
        setPopularName(popularName);
        setScientificName(scientificName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getBloom() {
        return bloom;
    }

    public void setBloom(String bloom) {
        this.bloom = bloom;
    }

    public String getGrowth() {
        return growth;
    }

    public void setGrowth(String growth) {
        this.growth = growth;
    }

    public String getLongevity() {
        return longevity;
    }

    public void setLongevity(String longevity) {
        this.longevity = longevity;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public LeafletUrl getExampleImageFlower() {
        return exampleImageFlower;
    }

    public void setExampleImageFlower(LeafletUrl exampleImageFlower) {
        this.exampleImageFlower = exampleImageFlower;
    }

    public LeafletUrl getExampleImageLeaf() {
        return exampleImageLeaf;
    }

    public void setExampleImageLeaf(LeafletUrl exampleImageLeaf) {
        this.exampleImageLeaf = exampleImageLeaf;
    }

    public LeafletUrl getExampleImageFruit() {
        return exampleImageFruit;
    }

    public void setExampleImageFruit(LeafletUrl exampleImageFruit) {
        this.exampleImageFruit = exampleImageFruit;
    }

    public int getDataset() {
        return dataset;
    }

    public void setDataset(int dataset) {
        this.dataset = dataset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommomName() {
        return popularName;
    }

    public void setPopularName(String commomName) {
        this.popularName = commomName;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Species another) {
        if (another != null) {
            return this.getCommomName().compareTo(another.getCommomName());
        }
        return -1;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof Species) {
            sameSame = this.getCommomName().contentEquals(((Species) object).getCommomName());
        }

        return sameSame;
    }

    public ForeignCollection<RankedSpecies> getAssociatedRankedSpecies() {
        return associatedRankedSpecies;
    }

    public void setAssociatedRankedSpecies(ForeignCollection<RankedSpecies> associatedRankedSpecies) {
        this.associatedRankedSpecies = associatedRankedSpecies;
    }
}
