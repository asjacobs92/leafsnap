package edu.maryland.leafsnap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * RankedSpecies object used by ORMLite to create the RankedSpecies table in the Application Database.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/> ORMLite </a>
 */
@DatabaseTable
public class RankedSpecies implements Serializable {

    private static final long serialVersionUID = -2835076667336270314L;

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private int rank;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Species species;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private CollectedLeaf associatedCollection;

    public RankedSpecies() {
    }

    public RankedSpecies(int rank, Species species) {
        setRank(rank);
        setSpecies(species);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public CollectedLeaf getAssociatedCollection() {
        return associatedCollection;
    }

    public void setAssociatedCollection(CollectedLeaf associatedCollection) {
        this.associatedCollection = associatedCollection;
    }

}
