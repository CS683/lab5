package edu.bu.projectportal.datalayer

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.room.Room

class ProjectPortalProvider: ContentProvider() {
    // Defines a handle to the Room database
    private lateinit var appDatabase: ProjectPortalDatabase
    private var projectDao: ProjectDao? = null

    override fun onCreate(): Boolean {

        // Creates a new database object.
        appDatabase = context?.let {
            Room.databaseBuilder(
                it,
                ProjectPortalDatabase::class.java, DBNAME
            ).fallbackToDestructiveMigration().build()
        }!!

        // Gets a Data Access Object to perform the database operations
        projectDao = appDatabase.projectDao()

        return true
    }

    override fun getType(uri: Uri): String? {
        return when (sURIMatcher.match(uri)) {
            PROJECT -> "vnd.android.cursor.dir/$PROVIDER_NAME.$TABLE_NAME"
            PROJECTID -> "vnd.android.cursor.item/$PROVIDER_NAME.$TABLE_NAME"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }



    fun constructProject(contentValue:ContentValues): Project {
        val authors = Converters().fromStringList(PROJECT_AUTHORS)
        val keywords = Converters().fromStringSet(PROJECT_KEYWORDS)
        return Project(contentValue.getAsInteger(PROJECT_ID),
            contentValue.getAsString(PROJECT_TITLE),
            contentValue.getAsString(PROJECT_DESC),
            contentValue.getAsString(authors).split(", ").map { it.trim() },
            contentValue.getAsString(PROJECT_LINK),
            contentValue.getAsString(keywords).split(", ").map { it.trim() }.toSet(),
            contentValue.getAsInteger("isFavorite") == 1
        )

    }

    // Implements the provider's insert method
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Insert code here to determine which DAO to use when inserting data, handle error conditions, etc.
        // match Uri using sURIMatcher defined in the contract
        // match Uri using sURIMatcher defined in the contract
        val uriType: Int = sURIMatcher.match(uri)
        var id: Long = 0

        when (uriType) {
            PROJECT -> {
                // call corresponding db insert method
                values?.let {
                    id = projectDao!!.addProject(constructProject(values))
                }
            }
            else -> throw IllegalArgumentException("unknown URI: $uri")
        }
        if (id >= 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return Uri.parse(
            TABLE_NAME.toString() + "/" + id
        )

    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        when (sURIMatcher.match(uri)) {
            PROJECT -> {
                // Return all projects
                val cursor = MatrixCursor(arrayOf(/* column names */))
                val projects = projectDao?.getAllProjects()?.value
                projects?.forEach { project ->
                    // Populate cursor with project data
                }
                return cursor
            }
            PROJECTID -> {
                // Return a single project by ID
                val id = ContentUris.parseId(uri)
                val cursor = MatrixCursor(arrayOf(/* column names */))
                projectDao?.searchProjectById(id)?.value?.let { project ->
                    // Populate cursor with project data
                }
                return cursor
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when (sURIMatcher.match(uri)) {
            PROJECT -> {
                // Delete projects based on selection criteria
                // This requires implementing a method in your DAO that can handle this operation
            }
            PROJECTID -> {
                val id = ContentUris.parseId(uri)
                val project = projectDao?.searchProjectById(id)?.value ?: return 0
                projectDao?.delProject(project)
                return 1 // Assuming one row is deleted
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }.also {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return 1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        when (sURIMatcher.match(uri)) {
            PROJECT -> {
                // Update projects based on selection criteria
                // This requires implementing a method in your DAO that can handle this operation
            }
            PROJECTID -> {
                val project = values?.let { constructProject(it) }
                if (project != null) {
                    projectDao?.editProject(project)
                }
                return 1 // Assuming one row is updated
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }.also {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return 1
    }

    companion object{
        val DBNAME = "projectportal-db"
        var PROVIDER_NAME = "edu.bu.projectportal.ProjectProvider"
        val URL = "content://$PROVIDER_NAME"
        val TABLE_NAME = "projects"
        val PROJECT_ID = "id"
        val PROJECT_TITLE = "title"
        val PROJECT_DESC = "desc"
        val PROJECT_AUTHORS = listOf<String>()
        val PROJECT_LINK = "www.google.com"
        val PROJECT_KEYWORDS = setOf<String>()
        val PROJECT_ISFAVORITE = false

        val projectUri = Uri.parse(URL + "/" + TABLE_NAME)

        const val PROJECT = 1
        const val PROJECTID = 2
        val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH).apply{
            this.addURI(PROVIDER_NAME, TABLE_NAME, PROJECT)
            this.addURI(PROVIDER_NAME, TABLE_NAME + "/#", PROJECTID)

        }
    }
}

