import requests

def obtener_noticias(keyword, api_key):
    url = f'https://newsapi.org/v2/everything?q={keyword}&sortby=publishedat&language=es&apikey={api_key}'
    respuesta = requests.get(url)
    datos = respuesta.json()
    
    noticias = []
    if datos.get('status') == 'ok' and 'articles' in datos:
        for art in datos['articles'][:6]:
            noticias.append({
                'titulo': art.get('title', 'sin titulo'),
                'fuente': art.get('source', {}).get('name', 'desconocida'),
                # nota: cambiamos 'publishedat' por 'publishedAt'
                'fecha': art.get('publishedAt', '0000-00-00')[:10],
                'enlace': art.get('url', '#')
            })
    return noticias