# Bota 🗺️

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.eltonkola/bota)](https://search.maven.org/artifact/io.github.eltonkola/bota)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Bota is a lightweight, interactive world map component for Compose Multiplatform applications. Built with KMM (Kotlin Multiplatform Mobile), it provides a beautiful, responsive, and customizable world map that works seamlessly across Android, iOS, and Desktop platforms.

## ✨ Features

- 🌍 **Interactive World Map** - Pan and zoom functionality built-in
- 🎯 **Country Selection** - Easily detect and highlight country selections
- 🎨 **Fully Customizable** - Customize colors, sizes, and interactions
- 📱 **Kotlin Multiplatform** - Works on Android, iOS, and Desktop
- ⚡ **Compose Multiplatform** - Built with Compose for seamless UI integration
- 🚀 **Lightweight** - No heavy dependencies, just pure Compose
- 🔍 **High Performance** - Optimized for smooth rendering and interactions

## 🚀 Quick Start

### Installation

Add Bota to your `commonMain` dependencies:

```kotlin
commonMain {
    dependencies {
        implementation("io.github.eltonkola:bota:0.0.1")
    }
}
```

### Basic Usage

```kotlin
import com.eltonkola.bota.WorldMap
import com.eltonkola.bota.WorldMapCountries
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorldMapExample() {
    var selectedCountryIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Select Countries", style = MaterialTheme.typography.headlineMedium)
        
        WorldMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(8.dp),
            highlightedCountryIds = selectedCountryIds,
            onCountryClick = { country ->
                // Toggle country selection
                selectedCountryIds = if (country.id in selectedCountryIds) {
                    selectedCountryIds - country.id
                } else {
                    selectedCountryIds + country.id
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display selected countries
        Text("Selected Countries (${selectedCountryIds.size}):")
        LazyColumn {
            items(selectedCountryIds.toList().sorted()) { countryId ->
                Text(
                    text = "• ${WorldMapCountries.data.find { it.id == countryId }?.name ?: "Unknown"}",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
```

## 🎨 Customization

### Styling the Map

Customize the appearance of the world map with different colors and styles:

```kotlin
import androidx.compose.ui.graphics.Color

WorldMap(
    modifier = Modifier
        .fillMaxWidth()
        .height(400.dp),
    highlightedCountryIds = selectedCountryIds,
    onCountryClick = { /* ... */ },
    defaultColor = Color(0xFFECECEC),     // Default country color
    highlightColor = Color(0xFFC8A2C8),   // Highlighted country color
    strokeColor = Color.Black             // Border color
)
```

### Getting Country Information

Access country data through the `WorldMapCountries` object:

```kotlin
// Get all countries
val allCountries = WorldMapCountries.data

// Find a country by ID
val country = WorldMapCountries.data.find { it.id == "us" } // United States

// Get all country IDs and names
val countryList = WorldMapCountries.data.map { it.id to it.name }
```

## 🌐 Platform Support

Bota supports the following platforms through Compose Multiplatform:
- Android
- iOS (via Kotlin/Native)
- Desktop (JVM)
- Web (experimental)


## 📄 License

```
Copyright 2024 Elton Kola

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/eltonkola">Elton Kola</a></sub>
</div>