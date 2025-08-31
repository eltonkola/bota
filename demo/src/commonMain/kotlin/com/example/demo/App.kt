package com.example.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eltonkola.bota.WorldMap
import com.eltonkola.bota.WorldMapCountries

@Composable
fun App() {
    MaterialTheme {

        var selectedCountryIds by remember { mutableStateOf<Set<String>>(emptySet()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Interactive World Map",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // The WorldMap Composable
            WorldMap(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                highlightedCountryIds = selectedCountryIds,
                onCountryClick = { country ->
                    // This is the callback logic
                    val newSet = selectedCountryIds.toMutableSet()
                    if (country.id in newSet) {
                        newSet.remove(country.id) // Deselect if already selected
                    } else {
                        newSet.add(country.id) // Select if not selected
                    }
                    selectedCountryIds = newSet
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selected Countries (${selectedCountryIds.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            // A scrollable list to show the names of selected countries
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(selectedCountryIds.toList().sorted()) { countryId ->
                    Text(
                        text = "• ${WorldMapCountries.data.find { it.id == countryId }?.name ?: "Unknown"}",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
//        Bota()
    }
}
