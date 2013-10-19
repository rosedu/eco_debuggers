from flask import Flask, request, Response, json, send_file
from flask.ext.pymongo import PyMongo
from bson.binary import Binary
from bson.objectid import ObjectId

from io import BytesIO

app = Flask(__name__)
mongo = PyMongo(app)


@app.route('/')
def index():
    return 'INDEX, BOSS'


@app.route('/api/v1/report', methods=['POST'])
def report():
    try:
        report = {
            'coords': [float(request.form['long']), float(request.form['lat'])],
            'magn': request.form['magn'],
            'img': Binary(request.files['img'].read())
        }
        # app.logger.debug(report)
        mongo.db.reports.insert(report)

        return Response(status=201)

    except:
        return Response(status=400)


@app.route('/api/v1/trash', methods=['GET'])
def trash():
    try:
        point_ne = [float(request.args['long_sw']), float(request.args['lat_sw'])]
        point_sw = [float(request.args['long_ne']), float(request.args['lat_ne'])]
        query = {'coords': {'$within': {'$box': [point_sw, point_ne]}}}
        cursor = mongo.db.reports.find(query, {'img': 0})
        reports = list(cursor)
        for r in reports:
            r['_id'] = str(r['_id'])
            r['long'] = r['coords'][0]
            r['lat'] = r['coords'][1]
            del r['coords']
        json_data = json.dumps(reports)

        return Response(json_data, 200)

    except:
        return Response(400)


@app.route('/api/v1/image', methods=['GET'])
def image():
    try:
        _id_string = request.args['_id']
        _id = ObjectId(_id_string)
        report = mongo.db.reports.find_one({'_id': _id}, {'img': 1, '_id': 0})
        image = report['img']
        image_file = BytesIO(image)

        return send_file(image_file)

    except:
        return Response(status=400)


@app.route('/api/v1/update', methods=['PUT'])
def update():
    try:
        _id_string = request.form['_id']
        _id = ObjectId(_id_string)

        updated_fields = {}
        try:
            updated_fields['img'] = Binary(request.files['img'].read())
        except:
            pass

        try:
            updated_fields['magn'] = request.form['magn']
        except:
            pass

        if updated_fields:
            mongo.db.reports.update({'_id': _id}, {'$set': updated_fields})

        return Response(status=204)

    except:
        return Response(status=400)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
