package com.drm.isail.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.LandAssignment
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.LandAssignmentRepository
import com.drm.isail.data.repository.ShipAssignmentRepository
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shipAssignmentRepository: ShipAssignmentRepository,
    private val landAssignmentRepository: LandAssignmentRepository
) : ViewModel() {
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private val _shipResults = MutableStateFlow<List<ShipAssignment>>(emptyList())
    val shipResults: StateFlow<List<ShipAssignment>> = _shipResults.asStateFlow()
    
    private val _landResults = MutableStateFlow<List<LandAssignment>>(emptyList())
    val landResults: StateFlow<List<LandAssignment>> = _landResults.asStateFlow()
    
    private val _userResults = MutableStateFlow<List<User>>(emptyList())
    val userResults: StateFlow<List<User>> = _userResults.asStateFlow()
    
    fun search(query: String, searchType: SearchType) {
        if (query.length < 2) {
            _searchState.value = SearchState.Error("Search query must be at least 2 characters")
            return
        }
        
        _searchState.value = SearchState.Loading
        
        viewModelScope.launch {
            try {
                when (searchType) {
                    SearchType.ALL -> searchAll(query)
                    SearchType.USERS -> searchUsers(query)
                    SearchType.SHIPS -> searchShips(query)
                    SearchType.LAND -> searchLand(query)
                }
                
                _searchState.value = SearchState.Success
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Search failed")
            }
        }
    }
    
    private suspend fun searchAll(query: String) {
        searchUsers(query)
        searchShips(query)
        searchLand(query)
    }
    
    private suspend fun searchUsers(query: String) {
        val lowerQuery = query.lowercase()
        
        try {
            // First sync with Firebase
            val allUsers = userRepository.getAllUsers()
            
            // Filter users based on query
            val filteredUsers = allUsers.value.filter { user ->
                (user.name?.lowercase()?.contains(lowerQuery) == true ||
                user.surname?.lowercase()?.contains(lowerQuery) == true ||
                user.presentRank?.lowercase()?.contains(lowerQuery) == true ||
                user.fleetWorking?.lowercase()?.contains(lowerQuery) == true ||
                user.company?.lowercase()?.contains(lowerQuery) == true) &&
                user.isProfileVisible
            }
            
            _userResults.value = filteredUsers
        } catch (e: Exception) {
            _userResults.value = emptyList()
        }
    }
    
    private suspend fun searchShips(query: String) {
        val lowerQuery = query.lowercase()
        
        try {
            // First sync with Firebase
            val shipAssignments = shipAssignmentRepository.getAllPublicShipAssignmentsFromFirestore()
            
            // Filter ship assignments based on query
            val filteredShips = shipAssignments.getOrNull()?.filter { shipAssignment ->
                shipAssignment.shipName?.lowercase()?.contains(lowerQuery) == true ||
                shipAssignment.rank?.lowercase()?.contains(lowerQuery) == true ||
                shipAssignment.company?.lowercase()?.contains(lowerQuery) == true ||
                shipAssignment.fleetType?.lowercase()?.contains(lowerQuery) == true ||
                shipAssignment.portOfJoining?.lowercase()?.contains(lowerQuery) == true
            } ?: emptyList()
            
            _shipResults.value = filteredShips
        } catch (e: Exception) {
            _shipResults.value = emptyList()
        }
    }
    
    private suspend fun searchLand(query: String) {
        val lowerQuery = query.lowercase()
        
        try {
            // First sync with Firebase
            val landAssignments = landAssignmentRepository.getAllPublicLandAssignmentsFromFirestore()
            
            // Filter land assignments based on query
            val filteredLand = landAssignments.getOrNull()?.filter { landAssignment ->
                landAssignment.lastVessel?.lowercase()?.contains(lowerQuery) == true ||
                landAssignment.fleetType?.lowercase()?.contains(lowerQuery) == true ||
                landAssignment.company?.lowercase()?.contains(lowerQuery) == true
            } ?: emptyList()
            
            _landResults.value = filteredLand
        } catch (e: Exception) {
            _landResults.value = emptyList()
        }
    }
    
    fun resetSearchState() {
        _searchState.value = SearchState.Idle
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    object Success : SearchState()
    data class Error(val message: String) : SearchState()
}

enum class SearchType {
    ALL,
    USERS,
    SHIPS,
    LAND
} 