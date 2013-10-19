from flask import Flask, request
from flask.ext.pymongo import PyMongo
from bson.binary import Binary

app = Flask(__name__)
mongo = PyMongo(app)

app.config['IMAGE_FOLDER'] = 'IMAGES'

@app.route('/')
def hello_world():
    return 'Hello World!'

@app.route('/api/v1/report', methods=['POST'])
def report():
    report = {
        'long': request.form['long'],
        'lat': request.form['lat'],
        'magn': request.form['magn'],
        'img': Binary(request.files['img'].read())
    }
    # app.logger.debug(report)

    mongo.db.reports.insert(report)

    return 'ceva'

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
