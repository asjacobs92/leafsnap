
#Leafsnap

This is the repository for the Android version of the Leafnap App. This app is still unpublished.
Leafsnap is an iOS app developed by University of Maryland, in association with Columbia Unvieristy and the Smithsonian Instituion.

##Project Structure
The project main files are located under *leafsnap/leafsnap/src/main/* and are divided in three main folders:
* assets/
This folder contains all the images for every species in the database of the app (which in the SVN are compressed in a zip file, but must be unzipped for the app to run properly) and the pre-populated SQLite Database that is shipped with the app. It is very important that the database and the images files match, otherwise unexpected Exception will occur at runtime, and may cause the app to crash. Since this files can no longer be altered, only accessed, when the app is running, every database update made within the app will store the images for the new species on a different location within the External Storage of the Android device. In case anyone might want to change the change the pre-populated database and the species list, it must be either done manually or make a script to do it. I initially did it by downloading the images from the Leafsnap database and creating a database within the app and then exporting it so I could ship it pre-populated. Be warned that this process takes a long time, and all images used for this app were scaled down by a factor of three, snce the original images were way too big for any device.

* res/
The resources folder stores the files for every UI component of the app. Android development uses XML files to represent interfaces, but there is a visual editor that helps build the interfaces. However animation and shapes must be done by coding. The structure of this folder is as follows:
  * anim/
    * This folder contains the XML files for animations within the app, such as the flip card transition used to switch between a species images and description.
  * drawable/ (for various resolutions)
    * This folder has all images and icons used in the app, for various different resolutions, as Android devices present several different screen sizes and more importantly, different pixel density. There are also some shapes that can be defined in XML and later used in layouts.
  * layout/
    * The layout folder contains XML files for every UI in the app, for every Activity and Fragment. All UI components, such as labels and buttons are given Ids in these files and can be then accessed and altered in Java code from the classes. Therefore, every item in this folder has either an Activity class, Fragment class or Adapter class related to it.
  * menu/
    * This folder contains only a couple of UI files for the Action Bar menu of some activities.
  * raw/
    * This folder contains a database configuration file. This app uses Object Relational Mapping Lite (ORM Lite) that provides some lightweight functionality for persisting Java objects to SQL databases. This is very similar to what Hibernate does, however, in a much lighter way, as Android devices can’t deal with all the overhead attached to doing this. Part of avoiding this complexity is the file in this folder *ormlite_config.xml* that gives a quick representation of the database to be persisted. Every time the model for the database are altered, this file must be generated again. This will be explained on a later section.
  * values/
    * This folder contains XML files for values used in the app, such as color Hex codes, strings and dimensions. This is an especially interesting Android feature, since the strings files can be used to easily translate the app to other languages using I18n, in case it is ever pored to other countries.

* java/edu/maryland/leafsnap/
This is folder contains all the source code for the app. The code is divided in several packages, as shown below.
  * activity
    * This package contains all the Activities of the app, each one with a corresponding XML file in the resources folder. Basically, each activity is a different “window” in an Android app, which defines the context for app. For more information on Activities, please check this [link] (http://developer.android.com/guide/components/activities.html). This package also contains the entry point of the code, which is the MainActivity.java class.
  * adapter
    * This package contains the Adapters used in Activities and Fragments. In Android, adapters are used to inflate (i.e. draw) the same layout several times, only changing the values for each item (e.g. each species in the species list). Every list adapter must follow the ViewHolder pattern for performance purposes. More information on Adapters and the ViewHolder pattern can be found [here] (http://www.vogella.com/tutorials/AndroidListView/article.html).
  * api
    * This package basically contains the code translate from the iOS version of the app, used to access the Leafsnap database and execute operation such as user creation, submitting an image and etc. All the files’ names and functions were kept the same (as much as possible, at least) as the iOS version it’d be easily recognizable. 
  * data
    * This package is very important for the database configuration. Here are kept two files: DatabaseHelper.java and DatabaseConfigUtil.java. The former is an Android class used to persist the database, using DAOs for every model class. It also opens the pre-populated database shipped with app, so be careful in making changes. The latter is actually a Java application, not related to the app. This application must be run every time the model files for the database are changed, so it can create the aforementioned file ormlite_config.xml used to improve database performance.
  * fragment 
    * The fragment package contains all the Fragments of the app. A Fragment in an Android app is basically a, well, a fragment of an Activity. It is mainly the visual aspect of an Activity. An Activity may have several Fragments within it (such as the Main Activity, that contains a different Fragment for each tab in the Action Bar), but each Fragment must be associated with only activity at a time, since the context of the Fragment is given by the Activity. More information of Fragments can be found [here] ( http://developer.android.com/guide/components/fragments.html).
  * model
    * This package contains the model classes for each table in the database. That is, models for Species, CollectedLeaf, DatabaseInfo, LeafletUrl and RankedSpecies. Once again, each time these files are changed, the DatabaseConfigUtil Java application must be run so these changes are affected in the app.
  * util
    * Finally, this package some random utility files, such as MediaUtils.java, that has functions used to access External Storage and Assets files, such as all the images for the Species, and the SessionManager.java, used to store the user information.


Some useful tools used during development:

* http://romannurik.github.io/AndroidAssetStudio/
* http://androidweekly.net/toolbox




