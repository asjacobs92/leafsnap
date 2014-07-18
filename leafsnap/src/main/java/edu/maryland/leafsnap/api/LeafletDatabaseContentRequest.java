package edu.maryland.leafsnap.api;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.DatabaseInfo;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.Species;

/**
 * @author Arthur Jacobs
 */
public class LeafletDatabaseContentRequest {

    private Context mContext;
    private DatabaseHelper mDbHelper;

    public LeafletDatabaseContentRequest(Context context) {
        setContext(context);
    }

    public void fetchWholeDatabaseFromServer() {
        RequestParams params = new RequestParams();
        params.put("detailed", "1");
        params.put("fmt", "json");
        LeafletAsyncRestClient.get("/species/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    parseResult(response);
                    LeafletImageManager imageManager = new LeafletImageManager(getContext());
                    imageManager.consolidateDatabase();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseResult(JSONObject result) throws JSONException, SQLException {
        parseDBInfo(result);
        parseSpecies(result);
    }

    private void parseDBInfo(JSONObject result) throws JSONException, SQLException {
        String dbVersion = result.getString("DB_VERSION");
        DatabaseInfo dbInfo = new DatabaseInfo(Float.parseFloat(dbVersion), true);
        dbInfo.setLastUpdate(new Date());

        getDbHelper().getDatabaseInfoDao().create(dbInfo);
    }

    private void parseSpecies(JSONObject result) throws JSONException, SQLException {
        JSONArray allJSONSpecies = result.getJSONArray("species");
        for (int i = 0; i < allJSONSpecies.length(); i++) {
            JSONObject oneJSONSpecies = allJSONSpecies.getJSONObject(i);
            Species oneSpecies = new Species();

            oneSpecies.setPopularName(oneJSONSpecies.getString("popname"));
            oneSpecies.setScientificName(oneJSONSpecies.getString("sciname"));
            oneSpecies.setDescription(oneJSONSpecies.getString("description"));

            oneSpecies = parseSpeciesData(oneJSONSpecies, oneSpecies);

            JSONObject exampleImages = oneJSONSpecies.getJSONObject("examples");
            oneSpecies = parseSpeciesExampleImages(oneSpecies, exampleImages);

            JSONArray datasets = oneJSONSpecies.getJSONArray("datasets");
            oneSpecies = parseSpeciesDatasets(oneSpecies, datasets);

            getDbHelper().getSpeciesDao().create(oneSpecies);

            JSONArray images = oneJSONSpecies.getJSONArray("images");
            parseSpeciesImages(oneSpecies, images);
        }
    }

    private void parseSpeciesImages(Species species, JSONArray images) throws JSONException, SQLException {
        int orderCount = 0;

        for (int i = 0; i < images.length(); i++) {
            JSONObject oneImage = images.getJSONObject(i);

            LeafletUrl leafletUrl = new LeafletUrl(oneImage.getString("url"));
            leafletUrl.setDataSource(oneImage.getString("datasource"));
            leafletUrl.setType(oneImage.getString("type"));
            leafletUrl.setOrder(orderCount);
            leafletUrl.setThumbnailLocation("Server");
            leafletUrl.setHiResImageLocation("Server");
            leafletUrl.setAssociatedSpecies(species);

            orderCount++;
            getDbHelper().getLeafletUrlDao().create(leafletUrl);
        }
    }

    private Species parseSpeciesExampleImages(Species species, JSONObject exampleImages) throws JSONException,
            SQLException {
        for (int i = 0; i < exampleImages.names().length(); i++) {
            String example = exampleImages.names().getString(i);

            LeafletUrl exampleUrl = new LeafletUrl(exampleImages.getString(example));
            exampleUrl.setDataSource("exampleImage");
            exampleUrl.setType(example);
            exampleUrl.setThumbnailLocation("Server");
            exampleUrl.setHiResImageLocation("Server");
            getDbHelper().getLeafletUrlDao().create(exampleUrl);

            if (example.compareTo("leaf") == 0) {
                species.setExampleImageLeaf(exampleUrl);
            }

            if (example.compareTo("flower") == 0) {
                species.setExampleImageFlower(exampleUrl);
            }

            if (example.compareTo("fruit") == 0) {
                species.setExampleImageFruit(exampleUrl);
            }
        }

        return species;
    }

    private Species parseSpeciesDatasets(Species species, JSONArray datasets) throws JSONException {
        for (int i = 0; i < datasets.length(); i++) {
            String dataset = datasets.getString(i);
            if (dataset.compareTo("rock_creek_park") == 0) {
                species.setDataset(species.getDataset() | 1);
            }

            if (dataset.compareTo("central_park") == 0) {
                species.setDataset(species.getDataset() | 2);
            }

            if (dataset.compareTo("northeast") == 0) {
                species.setDataset(species.getDataset() | 4);
            }
        }

        return species;
    }

    private Species parseSpeciesData(JSONObject oneJSONSpecies, Species oneSpecies) {
        JSONObject speciesData = null;

        // Must keep these try/catches separated since some JSON Objects have incomplete data.
        // This way, if only one of these is missing, the others will still be added.
        try {
            speciesData = oneJSONSpecies.getJSONObject("speciesdata");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        parseSpeciesHabitat(oneSpecies, speciesData);
        parseSpeciesBloom(oneSpecies, speciesData);
        parseSpeciesGrowth(oneSpecies, speciesData);
        parseSpeciesLongevity(oneSpecies, speciesData);
        parseSpeciesPresence(oneSpecies, speciesData);

        return oneSpecies;
    }

    private Species parseSpeciesPresence(Species oneSpecies, JSONObject speciesData) {
        try {
            oneSpecies.setPresence(speciesData.getString("Presence in US States"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSpecies;
    }

    private Species parseSpeciesLongevity(Species oneSpecies, JSONObject speciesData) {
        try {
            oneSpecies.setLongevity(speciesData.getString("Longevity"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSpecies;
    }

    private Species parseSpeciesGrowth(Species oneSpecies, JSONObject speciesData) {
        try {
            oneSpecies.setGrowth(speciesData.getString("Growth Habit"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSpecies;
    }

    private Species parseSpeciesBloom(Species oneSpecies, JSONObject speciesData) {
        try {
            oneSpecies.setBloom(speciesData.getString("Bloom Time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSpecies;
    }

    private Species parseSpeciesHabitat(Species oneSpecies, JSONObject speciesData) {
        try {
            oneSpecies.setHabitat(speciesData.getString("Habitat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSpecies;
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        }
        return mDbHelper;
    }
}
