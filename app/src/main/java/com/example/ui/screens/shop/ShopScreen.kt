package com.example.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ALL_TREE_SPECIES
import com.example.data.model.ForestEnvironmentType
import com.example.ui.EvergreenViewModel
import com.example.ui.components.TreeCanvas

@Composable
fun ShopScreen(
    viewModel: EvergreenViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    var selectedShopTab by remember { mutableIntStateOf(0) } // 0: Tree Species, 1: Environments

    val userCoins = user?.coins ?: 0
    val unlockedSpecies = remember(user?.unlockedSpeciesIds) {
        (user?.unlockedSpeciesIds ?: "classic_pine").split(",")
    }
    val unlockedEnvs = remember(user?.unlockedEnvironmentIds) {
        (user?.unlockedEnvironmentIds ?: "meadow").split(",")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Bar with Coins Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tree Nursery & Market",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Unlock botanical wonders with your focused time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFB300).copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$userCoins",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val tabs = listOf("Tree Species", "Environments")
            tabs.forEachIndexed { idx, title ->
                SegmentedButton(
                    selected = selectedShopTab == idx,
                    onClick = { selectedShopTab = idx },
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size)
                ) {
                    Text(title, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedShopTab == 0) {
            // Tree Species Catalog
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ALL_TREE_SPECIES) { species ->
                    val isUnlocked = unlockedSpecies.contains(species.id)
                    val canAfford = userCoins >= species.price

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            TreeCanvas(
                                species = species,
                                growthProgress = 1.0f,
                                size = 95.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = species.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(species.rarity.badgeColor).copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = species.rarity.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(species.rarity.badgeColor),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = species.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (isUnlocked) {
                                val isSelected = selectedSpecies.id == species.id
                                if (isSelected) {
                                    FilledTonalButton(
                                        onClick = { },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Planted", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.setSpecies(species) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("Plant This", fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buySpecies(species) },
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${species.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Environments Catalog
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ForestEnvironmentType.entries) { env ->
                    val isUnlocked = unlockedEnvs.contains(env.id)
                    val isSelected = user?.selectedEnvironmentId == env.id
                    val canAfford = userCoins >= env.price

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(env.groundColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = Color.White)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(env.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    env.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                FilledTonalButton(
                                    onClick = {},
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Active", fontSize = 12.sp)
                                }
                            } else if (isUnlocked) {
                                OutlinedButton(
                                    onClick = { viewModel.selectEnvironment(env) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Apply", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyEnvironment(env) },
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${env.price}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
