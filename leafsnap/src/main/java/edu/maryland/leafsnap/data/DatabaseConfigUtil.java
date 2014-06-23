package edu.maryland.leafsnap.data;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Configuration file to be run separately from Android Application. This Class creates a configuration file for
 * ORMLite, so it can create the database faster.
 *
 * @author Arthur Jacobs
 * @see <a href=http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html#Config-Optimization> ORMLite with
 * Android </a>
 */

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt");
    }
}
