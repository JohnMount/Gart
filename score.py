
import sys
import numpy as np
import matplotlib.pyplot as plt


#print str(sys.argv)
NINPUTS = (len(sys.argv)-2)
IMAGE_FILES = sys.argv[1:(NINPUTS+1)]
outF = sys.argv[NINPUTS+1]


# Make sure that caffe is on the python path:
caffe_root = '../'  # this file is expected to be in {caffe_root}/examples
sys.path.insert(0, caffe_root + 'python')

import caffe

# Set the right path to your model definition file, pretrained model weights,
# and the image you would like to classify.
MODEL_FILE = '../models/bvlc_reference_caffenet/deploy.prototxt'
PRETRAINED = '../models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel'

net = caffe.Classifier(MODEL_FILE, PRETRAINED,
                       mean=np.load(caffe_root + 'python/caffe/imagenet/ilsvrc_2012_mean.npy').mean(1).mean(1),
                       channel_swap=(2,1,0),
                       raw_scale=255,
                       image_dims=(256, 256))

input_images = [ caffe.io.load_image(IF) for IF in IMAGE_FILES ]

prediction = net.predict(input_images)  # predict takes any number of images, and formats them for the Caffe net automatically

f1 = open(outF, 'w')

for i in range(0,NINPUTS):
    # crabs
    score = prediction[i][120] + prediction[i][121] + prediction[i][122] + prediction[i][123] + prediction[i][126]
    f1.write(str(score))
    f1.write('\n')
    
f1.close()




