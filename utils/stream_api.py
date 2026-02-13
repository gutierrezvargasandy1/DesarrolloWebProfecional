import requests

def obtener_videos(keyword, api_key):
    url = f'https://www.googleapis.com/youtube/v3/search?part=snippet&q={keyword}+tutorial&type=video&key={api_key}&maxresults=4'
    respuesta = requests.get(url)
    datos = respuesta.json()
    
    videos = []
    if 'items' in datos:
        for item in datos['items']:
            snippet = item.get('snippet', {})
            video_id = item.get('id', {}).get('videoId', '')
            
            videos.append({
                'titulo': snippet.get('title', 'sin titulo'),
                'canal': snippet.get('channelTitle', 'canal desconocido'),
                'enlace': f"https://www.youtube.com/watch?v={video_id}",
                'miniatura': snippet.get('thumbnails', {}).get('medium', {}).get('url', '')
            })
    return videos