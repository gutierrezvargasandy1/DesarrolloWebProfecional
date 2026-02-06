from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def index():
    return render_template(
        'index.html',
        breadcrumb=["Inicio"]
    )

@app.route('/sistema-ambiental')
def sistema():
    return render_template(
        'sistema.html',
        breadcrumb=["Inicio", "Sistema de Gestión Ambiental"]
    )

@app.route('/futuro')
def futuro():
    return render_template(
        'futuro.html',
        breadcrumb=["Inicio", "Futuro del Planeta"]
    )

@app.route('/tres-r')
def tres_r():
    return render_template(
        'tres_r.html',
        breadcrumb=["Inicio", "Las 3 R"]
    )

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)