
import sys
import numpy as np
import matplotlib.pyplot as plt


print str(sys.argv)
IMAGE_FILE = sys.argv[1]
outF = sys.argv[2]


# Make sure that caffe is on the python path:
caffe_root = '../'  # this file is expected to be in {caffe_root}/examples
import sys
sys.path.insert(0, caffe_root + 'python')

import caffe

# Set the right path to your model definition file, pretrained model weights,
# and the image you would like to classify.
MODEL_FILE = '../models/bvlc_reference_caffenet/deploy.prototxt'
PRETRAINED = '../models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel'


with open('../data/ilsvrc12/synset_words.txt') as f:
    content = f.readlines()



net = caffe.Classifier(MODEL_FILE, PRETRAINED,
                       mean=np.load(caffe_root + 'python/caffe/imagenet/ilsvrc_2012_mean.npy').mean(1).mean(1),
                       channel_swap=(2,1,0),
                       raw_scale=255,
                       image_dims=(256, 256))

input_image = caffe.io.load_image(IMAGE_FILE)

prediction = net.predict([input_image])  # predict takes any number of images, and formats them for the Caffe net automatically
pclass = prediction[0].argmax()
print 'predicted class:', pclass
print 'predicted class name:', content[pclass]

# crabs
score = prediction[120] + prediction[121] + prediction[122] + prediction[123] + prediction[126]

print "SCORE",score

f1 = open(outF, 'w')
f1.write(str(score))
f1.write('\n')
f1.close()




