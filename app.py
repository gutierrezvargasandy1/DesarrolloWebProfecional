from flask import Flask, render_template, request
from utils.news_api import obtener_noticias
from utils.stream_api import obtener_videos

app = Flask(__name__)

news_key = '56ddbc2d8e51477f85bc585eb9cb0693'
yt_key = 'AIzaSyARAam8FzM6vA7KW2Jqw2yKcGjpvU0J4NE'

@app.route('/', methods=['GET', 'POST'])
def index():
    datos_noticias = []
    datos_videos = []
    tendencia = "esperando busqueda"
    clase_tendencia = "secondary"
    keyword = ""

    if request.method == 'POST':
        keyword = request.form.get('keyword')
        datos_noticias = obtener_noticias(keyword, news_key)
        datos_videos = obtener_videos(keyword, yt_key)
        
        total = len(datos_noticias) + len(datos_videos)
        if total >= 8:
            tendencia = "actividad alta"
            clase_tendencia = "danger"
        elif total >= 4:
            tendencia = "actividad media"
            clase_tendencia = "warning"
        else:
            tendencia = "actividad baja"
            clase_tendencia = "info"

    return render_template('index.html', 
                           noticias=datos_noticias, 
                           videos=datos_videos, 
                           tendencia=tendencia,
                           clase=clase_tendencia,
                           busqueda=keyword)

if __name__ == '__main__':
    app.run(debug=True)