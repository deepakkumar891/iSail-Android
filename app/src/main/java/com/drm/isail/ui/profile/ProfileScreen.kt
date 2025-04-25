package com.drm.isail.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drm.isail.data.model.User
import com.drm.isail.ui.components.LoadingIndicator
import com.drm.isail.ui.components.TextInputField
import com.drm.isail.ui.theme.primaryColor
import com.drm.isail.util.UiState

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(key1 = true) {
        viewModel.loadUserProfile()
    }
    
    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingIndicator()
        }
        is UiState.Success -> {
            ProfileContent(
                user = state.data,
                onUpdateProfile = { updatedUser -> viewModel.updateProfile(updatedUser) },
                onSignOut = {
                    viewModel.signOut()
                    onSignOut()
                },
                onImageSelected = { uri -> viewModel.uploadProfileImage(uri) },
                isUpdating = viewModel.isUpdating
            )
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    onUpdateProfile: (User) -> Unit,
    onSignOut: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    isUpdating: Boolean
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var fleetType by remember { mutableStateOf(user.fleetType) }
    var rank by remember { mutableStateOf(user.rank) }
    var isEditing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = isEditing) {
                    imagePickerLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default profile picture",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile image",
                        tint = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // User details
        if (isEditing) {
            TextInputField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person
            )
            
            TextInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = false
            )
            
            TextInputField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )
            
            TextInputField(
                value = fleetType,
                onValueChange = { fleetType = it },
                label = "Fleet Type",
                leadingIcon = Icons.Default.DirectionsBoat
            )
            
            TextInputField(
                value = rank,
                onValueChange = { rank = it },
                label = "Rank",
                leadingIcon = Icons.Default.Star
            )
        } else {
            ProfileInfoItem(label = "Name", value = user.name, icon = Icons.Default.Person)
            ProfileInfoItem(label = "Email", value = user.email, icon = Icons.Default.Email)
            ProfileInfoItem(label = "Phone", value = user.phone, icon = Icons.Default.Phone)
            ProfileInfoItem(label = "Fleet Type", value = user.fleetType, icon = Icons.Default.DirectionsBoat)
            ProfileInfoItem(label = "Rank", value = user.rank, icon = Icons.Default.Star)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Edit/Save button
        Button(
            onClick = {
                if (isEditing) {
                    // Save changes
                    val updatedUser = user.copy(
                        name = name,
                        phone = phone,
                        fleetType = fleetType,
                        rank = rank
                    )
                    onUpdateProfile(updatedUser)
                }
                isEditing = !isEditing
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = if (isEditing) "Save Profile" else "Edit Profile")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sign out button
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sign out icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Sign Out",
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 