package edu.bu.projectportal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.bu.projectportal.datalayer.Project
import edu.bu.projectportal.ProjectPortalApplication
import edu.bu.projectportal.datalayer.ProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
/*
Create a ViewModel class to keep track of the Project list
Use AndroidViewModel as the super class, we can pass the
application object directly to it.
 */
class ProjectListViewModel(application: Application): AndroidViewModel(application) {
    // pass the projectportalApplication as a parameter
    // make sure to define the application name in the manifest file.

    val projectPortalRepository =
        (application as ProjectPortalApplication).projectPortalRepository


    private val _projectList: LiveData<List<Project>> = projectPortalRepository.getAllProjects()
    private val favoriteProjectList: LiveData<List<Project>> = projectPortalRepository.getFavoriteProjects()

    private val _currentProjects = MutableLiveData<LiveData<List<Project>>>()
    private val currentProjects: LiveData<LiveData<List<Project>>> = _currentProjects

    // Backing field to hold the current list of projects
    private val _currentProjectList = MutableLiveData<List<Project>>()

    // Public getter to expose the current list of projects
    val projectList: LiveData<List<Project>>
        get() = _currentProjectList

    init {
        // Initially show all projects
        _currentProjects.value = _projectList

        // Observe changes in _currentProjects to update _currentProjectList
        _currentProjects.observeForever { liveData ->
            liveData.observeForever { projects ->
                _currentProjectList.value = projects
            }
        }
    }

    fun showOnlyFavorites(show: Boolean) {
        _currentProjects.value = if (show) favoriteProjectList else _projectList
    }


    fun addProject(project: Project) {
        projectPortalRepository.addProject(project)
    }

    fun delProject(project: Project) {
        projectPortalRepository.delProject(project)
    }

}