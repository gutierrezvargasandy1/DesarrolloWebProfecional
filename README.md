# 🗺️ Buscador de Lugares con Geolocalización

## 📋 Descripción del Proyecto

Aplicación web desarrollada con Flask que permite buscar **puntos de interés específicos** (gasolineras, hospitales, farmacias, ayuntamientos, etc.) en cualquier ciudad del mundo y visualizarlos todos en un mapa interactivo.

## 🎯 Características Principales

✅ **Filtro de Categorías Visual** - Selecciona entre 12 tipos de lugares  
✅ **Búsqueda Simple** - Solo ingresa la ciudad o país  
✅ **Múltiples Marcadores** - Muestra TODOS los lugares encontrados  
✅ **Lista Interactiva** - Haz clic en la lista para enfocar en el mapa  
✅ **Mapa Interactivo** - Zoom, navegación y popups informativos  
✅ **Contador de Resultados** - Sabrás cuántos lugares se encontraron  
✅ **Radio de 10km** - Búsqueda amplia desde el centro de la ciudad

## 🏷️ Categorías Disponibles

- ⛽ **Gasolineras**
- 🏥 **Hospitales**
- 💊 **Farmacias**
- 🏛️ **Ayuntamientos**
- 🍽️ **Restaurantes**
- 🏦 **Bancos**
- 🏫 **Escuelas**
- 🛒 **Supermercados**
- 🏨 **Hoteles**
- 👮 **Estaciones de Policía**
- 🚒 **Estaciones de Bomberos**
- 🌳 **Parques**

## 🛠️ Tecnologías Utilizadas

- **Backend**: Flask (Python)
- **API**: Nominatim (OpenStreetMap)
- **Frontend**: HTML5, CSS3, JavaScript
- **Mapas**: Leaflet.js
- **HTTP Requests**: biblioteca `requests` de Python

## 📁 Estructura del Proyecto

```
flask_geolocalizacion/
│
├── app.py                      # Aplicación principal Flask
├── templates/
│   ├── index.html              # Página de búsqueda
│   └── map.html                # Página del mapa
├── static/
│   └── css/
│       └── styles.css          # Estilos personalizados
├── requirements.txt            # Dependencias de Python
├── README.md                   # Documentación
└── .gitignore                  # Archivos ignorados por Git
```

## 🚀 Instalación y Configuración

### Requisitos Previos
- Python 3.8 o superior
- pip (gestor de paquetes de Python)

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/TU_USUARIO/flask-geolocalizacion.git
cd flask-geolocalizacion
```

2. **Crear entorno virtual**
```bash
python -m venv venv
```

3. **Activar el entorno virtual**

En Windows:
```bash
venv\Scripts\activate
```

En macOS/Linux:
```bash
source venv/bin/activate
```

4. **Instalar dependencias**
```bash
pip install -r requirements.txt
```

5. **Ejecutar la aplicación**
```bash
python app.py
```

6. **Abrir en el navegador**
```
http://127.0.0.1:5000
```

## 📖 Uso de la Aplicación

1. **Página Principal**: Ingresa el nombre de cualquier lugar del mundo
   - Ejemplos: "Dolores Hidalgo Guanajuato", "Torre Eiffel", "Central Park"

2. **Búsqueda**: Haz clic en "Buscar Ubicación"

3. **Visualización**: El mapa mostrará:
   - Ubicación exacta con marcador rojo
   - Coordenadas (latitud y longitud)
   - Nombre completo del lugar
   - Mapa interactivo con zoom

## 🌐 API Utilizada: Nominatim (OpenStreetMap)

### Características
- **URL Base**: `https://nominatim.openstreetmap.org/search`
- **Método**: GET
- **Formato**: JSON
- **Autenticación**: No requiere API Key
- **Limitaciones**: Uso educativo y no comercial

### Parámetros de la API
| Parámetro | Descripción |
|-----------|-------------|
| `q` | Texto del lugar a buscar |
| `format` | Formato de respuesta (json) |
| `limit` | Número de resultados (1) |

### Headers Requeridos
```python
{
    'User-Agent': 'FlaskGeoApp/1.0 (Educational Project)'
}
```

## 🎨 Características de la Interfaz

### Diseño Responsivo
- ✅ Adaptable a móviles, tablets y escritorio
- ✅ Gradientes modernos y sombras suaves
- ✅ Animaciones CSS

### Mapa Interactivo
- ✅ Zoom con rueda del mouse
- ✅ Marcador personalizado rojo
- ✅ Popup informativo
- ✅ Círculo de área de 500m

## 📸 Capturas de Pantalla

### Página Principal
![Página de búsqueda](screenshots/index.png)

### Mapa de Resultados
![Mapa interactivo](screenshots/map.png)

## 🔧 Funcionalidades Técnicas

### Backend (Flask)
- Manejo de rutas con decoradores
- Renderizado de templates con Jinja2
- Peticiones HTTP a API externa
- Validación de formularios

### Frontend
- HTML5 semántico
- CSS3 con gradientes y animaciones
- JavaScript vanilla para Leaflet.js
- Diseño mobile-first

## 📚 Conceptos Clave Implementados

1. **Framework Flask**
   - Enrutamiento
   - Templates Jinja2
   - Manejo de formularios

2. **Consumo de API REST**
   - Peticiones GET
   - Procesamiento de JSON
   - Manejo de errores

3. **Mapas Interactivos**
   - Leaflet.js
   - Tiles de OpenStreetMap
   - Marcadores y popups

4. **Frontend Responsivo**
   - CSS Grid y Flexbox
   - Media queries
   - Gradientes y animaciones

## 🐛 Manejo de Errores

La aplicación maneja los siguientes casos:
- ❌ Campo de búsqueda vacío
- ❌ Lugar no encontrado
- ❌ Error de conexión con la API
- ❌ Respuesta inválida de la API

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Haz fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la [Licencia MIT](LICENSE).

## 👨‍💻 Autor

**Tu Nombre**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- Email: tu-email@ejemplo.com

## 🙏 Agradecimientos

- OpenStreetMap por proporcionar la API Nominatim
- Leaflet.js por la librería de mapas
- Flask por el framework web
- Comunidad de código abierto

## 📞 Soporte

Si tienes alguna pregunta o problema:
- Abre un [Issue](https://github.com/tu-usuario/flask-geolocalizacion/issues)
- Contacta al autor

---

**Proyecto Educativo** - Desarrollado con 💜 usando Flask y OpenStreetMap
