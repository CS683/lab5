package edu.bu.projectportal.datalayer

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
This is the Database Entry point
 */
// define database configuraiton, and the access point to the database
@Database(
    entities = [Project::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ProjectPortalDatabase: RoomDatabase() {

    abstract fun projectDao(): ProjectDao
}