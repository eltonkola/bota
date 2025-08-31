package com.example.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eltonkola.bota.Country
import com.eltonkola.bota.WorldMap
import com.eltonkola.bota.WorldMapCountries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        var selectedCountryIds by remember { mutableStateOf<Set<String>>(emptySet()) }
        var searchQuery by remember { mutableStateOf("") }

        // Centralized logic to toggle a country's selection state
        val onToggleCountry: (Country) -> Unit = { country ->
            selectedCountryIds = if (country.id in selectedCountryIds) {
                selectedCountryIds - country.id
            } else {
                selectedCountryIds + country.id
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Interactive World Map",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // The WorldMap Composable, occupying the top portion of the screen
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                WorldMap(
                    modifier = Modifier.fillMaxSize(),
                    highlightedCountryIds = selectedCountryIds,
                    onCountryClick = onToggleCountry
                )
            }

            // Controls: Add/Remove All and Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedCountryIds = WorldMapCountries.data.map { it.id }.toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select All")
                    }
                    Button(
                        onClick = { selectedCountryIds = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deselect All")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Countries") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton({
                            searchQuery = ""
                        }){
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "clear",
                            )
                        }
                    }
                )
            }


            // A Row to hold the two lists side-by-side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(0.6f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Column for Selected Countries ---
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selected (${selectedCountryIds.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val selectedCountries = remember(selectedCountryIds) {
                        WorldMapCountries.data
                            .filter { it.id in selectedCountryIds }
                            .sortedBy { it.name }
                    }
                    LazyColumn {
                        items(selectedCountries, key = { it.id }) { country ->
                            CountryListItem(
                                country = country,
                                isSelected = true,
                                onToggle = { onToggleCountry(country) }
                            )
                        }
                    }
                }

                // --- Column for All Countries with Search Filter ---
                Column(modifier = Modifier.weight(1f)) {

                    val allCountriesFiltered = remember(searchQuery) {
                        if (searchQuery.isBlank()) {
                            WorldMapCountries.data
                        } else {
                            WorldMapCountries.data.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }
                    Text(
                        text = "All Countries (${allCountriesFiltered.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn {
                        items(allCountriesFiltered, key = { it.id }) { country ->
                            CountryListItem(
                                country = country,
                                isSelected = country.id in selectedCountryIds,
                                onToggle = { onToggleCountry(country) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A reusable composable for an item in either country list.
 * It features a checkbox and text, and the entire row is clickable.
 */
@Composable
private fun CountryListItem(
    country: Country,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = country.name, modifier = Modifier.weight(1f))
    }
}