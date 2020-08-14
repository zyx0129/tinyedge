# USAGE
# Start the server:
# python app.py
# Submit a request via cURL:
# curl -X POST -F image=@dog.jpg 'http://localhost:5211/predict'

# import the necessary packages
from keras.applications import ResNet50
from keras.preprocessing.image import img_to_array
from keras.applications import imagenet_utils
from PIL import Image
import numpy as np
import flask
import io
import tensorflow as tf
import time

# initialize our Flask application and the Keras model
model = None


def load_model():
    # load the pre-trained Keras model (here we are using a model
    # pre-trained on ImageNet and provided by Keras, but you can
    # substitute in your own networks just as easily)
    global model
    model = ResNet50(weights="imagenet")
    global graph
    graph = tf.get_default_graph()


load_model()
app = flask.Flask(__name__)
app.debug = True


def prepare_image(image, target):
    # if the image mode is not RGB, convert it
    if image.mode != "RGB":
        image = image.convert("RGB")

    # resize the input image and preprocess it
    image = image.resize(target)
    image = img_to_array(image)
    image = np.expand_dims(image, axis=0)
    image = imagenet_utils.preprocess_input(image)

    # return the processed image
    return image


# @app.route("/", methods=["GET"])
# def home():
#     return '''\
#     <form method=\"post\" action=\"/predict\" enctype=\"multipart/form-data\" id=\"file_upload\"> 
#         <input type=\"file\" id=\"test-image-file\" name=\"image\" accept=\"image/jpeg, image/jpg\">
#         <input type=\"submit\" value=\"Submit\" />
#     </form>'''


@app.route("/predict", methods=["POST"])
def predict():
    # initialize the data dictionary that will be returned from the
    # view
    response = {}
    response["code"] = 1
    response["message"] = "failed,request post"
    response["data"] = {}
    time_start = time.time()

    # ensure an image was properly uploaded to our endpoint
    if flask.request.method == "POST":
        if flask.request.files.get("image"):
            # read the image in PIL format
            image = flask.request.files["image"].read()
            image = Image.open(io.BytesIO(image))
            # preprocess the image and prepare it for classification
            image = prepare_image(image, target=(224, 224))
            #image.save("/home/root/pic/1.jpg")
            time_prepare = time.time()

            # classify the input image and then initialize the list
            # of predictions to return to the client
            with graph.as_default():
                preds = model.predict(image)
                time_predict = time.time()
                results = imagenet_utils.decode_predictions(preds)
                #data["predictions"] = []
                response["data"]["predictions"] = []
                # loop over the results and add them to the list of
                # returned predictions
                for (imagenetID, label, prob) in results[0]:
                    r = {"label": label, "probability": float(prob)}
                    #data["predictions"].append(r)
                    response["data"]["predictions"].append(r)
                app.logger.info(response["data"])
                # indicate that the request was a success
                response["code"] = 0
                response["message"] = "success"
                #data["success"] = True
            time_decode = time.time()

    # return the data dictionary as a JSON response
    resp = flask.make_response(flask.jsonify(response))
    return resp
    
    # if (response["code"] == 1):
    #     resp = flask.make_response(flask.jsonify(response))
    #     return resp
    # else:
    #     resp = flask.make_response("It's " + data["predictions"][0][
    #         "label"] + ".\nProbability: " + str(
    #         data["predictions"][0]["probability"]))
    #     resp.headers['time_prepare'] = str(time_prepare - time_start)
    #     resp.headers['time_predict'] = str(time_predict - time_prepare)
    #     resp.headers['time_decode'] = str(time_decode - time_predict)
    #     return resp


# if this is the main thread of execution first load the model
# and then start the server
if __name__ == "__main__":
    print(("* Loading Keras model and Flask starting server..."
           "please wait until server has fully started"))
    load_model()
    app.run(host='0.0.0.0', port=80)
