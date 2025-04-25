package com.drm.isail.ui.screens.home

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drm.isail.data.model.LandAssignment
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.UserStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToShipForm: () -> Unit,
    onNavigateToLandForm: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shipAssignments by viewModel.shipAssignments.collectAsState()
    val landAssignments by viewModel.landAssignments.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Home") },
            actions = {
                IconButton(onClick = { viewModel.syncAssignmentsWithFirestore() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                }
            }
        )
        
        when (homeState) {
            is HomeState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Welcome, ${currentUser?.name ?: "User"}!",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Current Status: ${if (currentUser?.currentStatus == UserStatus.ON_SHIP) "On Ship" else "On Land"}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (currentUser?.currentStatus == UserStatus.ON_SHIP) {
                            if (shipAssignments.isEmpty()) {
                                CreateStatusCard(
                                    title = "Create Ship Assignment",
                                    description = "You're currently on a ship but haven't created an assignment. Create one to find potential replacements.",
                                    buttonText = "Create Ship Assignment",
                                    onClick = onNavigateToShipForm
                                )
                            } else {
                                Text(
                                    text = "Your Ship Assignments",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else { // ON_LAND
                            if (landAssignments.isEmpty()) {
                                CreateStatusCard(
                                    title = "Create Land Assignment",
                                    description = "You're currently on land but haven't created an assignment. Create one to find potential ships to join.",
                                    buttonText = "Create Land Assignment",
                                    onClick = onNavigateToLandForm
                                )
                            } else {
                                Text(
                                    text = "Your Land Assignments",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    if (currentUser?.currentStatus == UserStatus.ON_SHIP) {
                        items(shipAssignments) { shipAssignment ->
                            ShipAssignmentCard(shipAssignment = shipAssignment)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        if (shipAssignments.isNotEmpty()) {
                            item {
                                Button(
                                    onClick = onNavigateToShipForm,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Create New Ship Assignment")
                                }
                            }
                        }
                    } else { // ON_LAND
                        items(landAssignments) { landAssignment ->
                            LandAssignmentCard(landAssignment = landAssignment)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        if (landAssignments.isNotEmpty()) {
                            item {
                                Button(
                                    onClick = onNavigateToLandForm,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Create New Land Assignment")
                                }
                            }
                        }
                    }
                }
            }
            is HomeState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (homeState as HomeState.Error).message,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.syncAssignmentsWithFirestore() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CreateStatusCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.body1
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun ShipAssignmentCard(shipAssignment: ShipAssignment) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = shipAssignment.shipName ?: "Unknown Ship",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = shipAssignment.rank ?: "Unknown Rank",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = shipAssignment.fleetType ?: "Unknown Fleet",
                    style = MaterialTheme.typography.subtitle1
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Joined On",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = shipAssignment.dateOfOnboard?.let { dateFormat.format(it) } ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = "Expected Sign-off",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = shipAssignment.getExpectedReleaseDate()?.let { dateFormat.format(it) } ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Port of Joining",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = shipAssignment.portOfJoining ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                Column {
                    Text(
                        text = "Visibility",
                        style = MaterialTheme.typography.caption
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (shipAssignment.isPublic) MaterialTheme.colors.primary
                                else MaterialTheme.colors.error
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (shipAssignment.isPublic) "Public" else "Private",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandAssignmentCard(landAssignment: LandAssignment) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Last Vessel: ${landAssignment.lastVessel ?: "Unknown"}",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Fleet Type: ${landAssignment.fleetType ?: "Unknown"}",
                style = MaterialTheme.typography.subtitle1
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Date Home",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = landAssignment.dateHome?.let { dateFormat.format(it) } ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = "Expected Joining",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = landAssignment.expectedJoiningDate?.let { dateFormat.format(it) } ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Company",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = landAssignment.company ?: "Unknown",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                Column {
                    Text(
                        text = "Visibility",
                        style = MaterialTheme.typography.caption
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (landAssignment.isPublic) MaterialTheme.colors.primary
                                else MaterialTheme.colors.error
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (landAssignment.isPublic) "Public" else "Private",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
    }
} 