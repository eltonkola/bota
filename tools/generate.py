import xml.etree.ElementTree as ET
import argparse
import os

try:
    import pycountry
except ImportError:
    print("Error: 'pycountry' library not found.")
    print("Please install it by running: pip install pycountry")
    exit(1)

# A manual mapping for country names that differ between the SVG and the pycountry library.
MANUAL_NAME_TO_ISO_MAP = {
    # Official Name Mismatches
    "Bolivia": "Bolivia, Plurinational State of",
    "Brunei Darussalam": "Brunei Darussalam",
    "Republic of Congo": "Congo",
    "Democratic Republic of the Congo": "Congo, The Democratic Republic of the",
    "Côte d'Ivoire": "Côte d'Ivoire",
    "Iran": "Iran, Islamic Republic of",
    "Lao PDR": "Lao People's Democratic Republic",
    "Dem. Rep. Korea": "Korea, Democratic People's Republic of",
    "Republic of Korea": "Korea, Republic of",
    "Macedonia": "North Macedonia",
    "Russian Federation": "Russian Federation",
    "Syria": "Syrian Arab Republic",
    "Taiwan": "Taiwan, Province of China",
    "Tanzania": "Tanzania, United Republic of",
    "United States": "United States",
    "Venezuela": "Venezuela, Bolivarian Republic of",
    "Vietnam": "Viet Nam",

    # Name changes or variations
    "Turkey": "Türkiye",
    "Cape Verde": "Cabo Verde",

    # Territories with assigned ISO codes
    "United States Virgin Islands": "VI",
    "Falkland Islands": "Falkland Islands (Malvinas)",
    "Faeroe Islands": "FO",
    "Canary Islands (Spain)": "IC", # Standard code for Canary Islands

    # Grouped Territories
    "Saba (Netherlands)": "BQ", # Bonaire, Sint Eustatius and Saba
    "St. Eustatius (Netherlands)": "BQ",
    "Sint Maarten": "Sint Maarten (Dutch part)",

    # User-assigned / special codes
    "Kosovo": "XK",
    "Western Sahara": "EH"
}

def get_country_iso_code(country_name):
    """Finds the ISO 3166-1 alpha-2 code for a given country name."""
    if len(country_name) == 2 and country_name.isupper():
        return country_name

    if country_name in MANUAL_NAME_TO_ISO_MAP:
        lookup_name = MANUAL_NAME_TO_ISO_MAP[country_name]
        if len(lookup_name) == 2 and lookup_name.isupper():
            return lookup_name

        country = pycountry.countries.get(name=lookup_name)
        return country.alpha_2 if country else None

    country = pycountry.countries.get(name=country_name)
    if country:
        return country.alpha_2

    try:
        results = pycountry.countries.search_fuzzy(country_name)
        if results:
            return results[0].alpha_2
    except LookupError:
        pass

    return None


def generate_kotlin_files(svg_file_path, output_base_name):
    """
    Parses an SVG and generates two Kotlin files: one with path data and one with just country data.
    """
    try:
        ET.register_namespace('', "http://www.w3.org/2000/svg")
        tree = ET.parse(svg_file_path)
        root = tree.getroot()
        namespace = {'svg': 'http://www.w3.org/2000/svg'}

        countries_agg = {}
        paths = root.findall('.//svg:path', namespace)
        print(f"Found {len(paths)} <path> elements. Aggregating paths...")

        for path in paths:
            attrs = path.attrib
            name = attrs.get('name') or attrs.get('class')
            path_id = attrs.get('id')
            path_data = attrs.get('d')

            if name and path_data:
                if name not in countries_agg:
                    countries_agg[name] = {"id": path_id, "paths": []}

                countries_agg[name]["paths"].append(" ".join(path_data.strip().split()))

                if path_id and not countries_agg[name].get("id"):
                    countries_agg[name]["id"] = path_id

        print("Aggregation complete. Resolving country IDs...")

        final_country_list = []
        unmapped_countries = []

        for name, data in sorted(countries_agg.items()):
            stable_id = data.get("id")

            if not stable_id or len(stable_id) != 2:
                iso_code = get_country_iso_code(name)
                if iso_code:
                    stable_id = iso_code
                else:
                    stable_id = name
                    unmapped_countries.append(name)

            final_country_list.append({
                "id": stable_id,
                "name": name,
                "paths": data["paths"]
            })

        # --- Generate WorldMapPaths.kt (heavy data) ---
        paths_kt_path = f"{output_base_name}Paths.kt"
        with open(paths_kt_path, 'w', encoding='utf-8') as kt_file:
            kt_file.write("package com.eltonkola.bota\n\n")
            kt_file.write(f"object WorldMapPaths {{\n")
            kt_file.write("    val data = listOf(\n")
            for i, country in enumerate(final_country_list):
                kt_id = f'"{country["id"]}"'
                kt_name = f'"{country["name"]}"'
                kt_paths_list = ',\n'.join([f'            """{p}"""' for p in country["paths"]])
                kt_paths = f"listOf(\n{kt_paths_list}\n        )"
                kt_file.write(f'        CountryPath(id = {kt_id}, name = {kt_name}, paths = {kt_paths})')
                kt_file.write(",\n" if i < len(final_country_list) - 1 else "\n")
            kt_file.write("    )\n}\n")

        print(f"\n✅ Success! Generated heavy path data -> {paths_kt_path}")

        # --- Generate WorldMapCountries.kt (light data) ---
        countries_kt_path = f"{output_base_name}Countries.kt"
        with open(countries_kt_path, 'w', encoding='utf-8') as kt_file:
            kt_file.write("package com.eltonkola.bota\n\n")
            kt_file.write(f"object WorldMapCountries {{\n")
            kt_file.write("    val data = listOf(\n")
            for i, country in enumerate(final_country_list):
                kt_id = f'"{country["id"]}"'
                kt_name = f'"{country["name"]}"'
                kt_file.write(f'        Country(id = {kt_id}, name = {kt_name})')
                kt_file.write(",\n" if i < len(final_country_list) - 1 else "\n")
            kt_file.write("    )\n}\n")

        print(f"✅ Success! Generated lightweight country list -> {countries_kt_path}")

        if unmapped_countries:
            print("\n⚠️ The following names could not be mapped to an ISO code and used their name as an ID instead:")
            print(", ".join(unmapped_countries))

    except FileNotFoundError:
        print(f"❌ Error: The file '{svg_file_path}' was not found.")
    except Exception as e:
        print(f"❌ An unexpected error occurred: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate Kotlin map data files from an SVG.")
    parser.add_argument("svg_file", help="Path to the input SVG file.")
    parser.add_argument("-o", "--output", default="WorldMap", help="Base name for the output Kotlin files (e.g., 'WorldMap' will create WorldMapPaths.kt and WorldMapCountries.kt).")

    args = parser.parse_args()
    generate_kotlin_files(args.svg_file, args.output)