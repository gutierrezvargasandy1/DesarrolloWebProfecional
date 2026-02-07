from flask import Flask, render_template, request
import requests

app = Flask(__name__)

def get_location(place_name):
    """
    Función que consulta la API de Nominatim (OpenStreetMap)
    para obtener las coordenadas de un lugar.
    
    Args:
        place_name (str): Nombre del lugar a buscar
        
    Returns:
        dict: Diccionario con lat, lon y display_name o None si no se encuentra
    """
    url = "https://nominatim.openstreetmap.org/search"
    
    params = {
        'q': place_name,
        'format': 'json',
        'limit': 1
    }
    
    headers = {
        'User-Agent': 'FlaskGeoApp/1.0 (Educational Project)'
    }
    
    try:
        response = requests.get(url, params=params, headers=headers)
        data = response.json()
        
        if data and len(data) > 0:
            result = data[0]
            return {
                'latitude': float(result['lat']),
                'longitude': float(result['lon']),
                'display_name': result['display_name']
            }
        else:
            return None
            
    except Exception as e:
        print(f"Error al consultar la API: {e}")
        return None


def search_pois(city, poi_type, limit=50):
    """
    Busca puntos de interés (POIs) en una ciudad específica usando Overpass API.
    
    Args:
        city (str): Nombre de la ciudad
        poi_type (str): Tipo de POI
        limit (int): Número máximo de resultados
        
    Returns:
        list: Lista de POIs encontrados
    """
    overpass_url = "https://overpass-api.de/api/interpreter"
    
    # Obtener coordenadas de la ciudad
    city_data = get_location(city)
    if not city_data:
        return [], None
    
    lat = city_data['latitude']
    lon = city_data['longitude']
    
    # Mapeo de tipos de POI a etiquetas de OpenStreetMap
    poi_tags = {
        'gasolinera': 'amenity=fuel',
        'hospital': 'amenity=hospital',
        'farmacia': 'amenity=pharmacy',
        'restaurante': 'amenity=restaurant',
        'banco': 'amenity=bank',
        'escuela': 'amenity=school',
        'supermercado': 'shop=supermarket',
        'hotel': 'tourism=hotel',
        'ayuntamiento': 'amenity=townhall',
        'policia': 'amenity=police',
        'bomberos': 'amenity=fire_station',
        'parque': 'leisure=park'
    }
    
    tag = poi_tags.get(poi_type, 'amenity=fuel')
    
    # Query de Overpass para buscar POIs en un radio de 10km
    overpass_query = f"""
    [out:json][timeout:25];
    (
      node[{tag}](around:10000,{lat},{lon});
      way[{tag}](around:10000,{lat},{lon});
    );
    out center {limit};
    """
    
    try:
        response = requests.post(overpass_url, data={'data': overpass_query}, timeout=30)
        data = response.json()
        
        pois = []
        for element in data.get('elements', []):
            # Obtener coordenadas
            if 'lat' in element and 'lon' in element:
                poi_lat = element['lat']
                poi_lon = element['lon']
            elif 'center' in element:
                poi_lat = element['center']['lat']
                poi_lon = element['center']['lon']
            else:
                continue
            
            # Obtener información del POI
            tags = element.get('tags', {})
            name = tags.get('name', 'Sin nombre')
            address = tags.get('addr:street', '')
            
            pois.append({
                'name': name,
                'latitude': poi_lat,
                'longitude': poi_lon,
                'address': address,
                'type': poi_type
            })
        
        return pois, city_data
    
    except Exception as e:
        print(f"Error al buscar POIs: {e}")
        return [], city_data


@app.route('/', methods=['GET'])
def index():
    """
    Página principal con formulario de búsqueda.
    """
    return render_template('index.html')


@app.route('/search', methods=['POST'])
def search():
    """
    Procesa la búsqueda de POIs y muestra el mapa.
    """
    city = request.form.get('city', '').strip()
    poi_type = request.form.get('poi_type', 'gasolinera')
    
    if not city:
        return render_template('index.html', error='Por favor ingresa una ciudad o país')
    
    # Buscar POIs
    pois, city_data = search_pois(city, poi_type)
    
    if not city_data:
        return render_template(
            'index.html',
            error=f'No se pudo encontrar la ubicación: {city}'
        )
    
    if not pois:
        return render_template(
            'index.html',
            error=f'No se encontraron {poi_type}s en {city}. Intenta ampliar la búsqueda o elige otra categoría.'
        )
    
    # Renderizar mapa con POIs
    return render_template(
        'map.html',
        city_name=city,
        place_name=city_data['display_name'],
        latitude=city_data['latitude'],
        longitude=city_data['longitude'],
        pois=pois,
        poi_type=poi_type,
        total_pois=len(pois)
    )


if __name__ == '__main__':
    app.run(debug=True)
