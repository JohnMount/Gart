
import sys
import numpy as np
import matplotlib.pyplot as plt
from skimage.restoration import denoise_tv_chambolle


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
# try to not let local dots control score- so denoise them out a bit before scoring
image_noise = [ sum(sum(sum(abs(img-denoise_tv_chambolle(img, weight=0.2, multichannel=True)))))/(img.shape[0]*img.shape[1]*img.shape[2]) for img in input_images ]

prediction = net.predict(input_images)  # predict takes any number of images, and formats them for the Caffe net automatically

f1 = open(outF, 'w')

for i in range(0,NINPUTS):
    # crabs (files are numbered from 1 by grep and 0 in arrays using  119:n01978287 Dungeness crab, Cancer magister
    score = prediction[i][118]/max(1,sum(prediction[i]))
    if(image_noise[i]>=0.05):
        score = 0.0
    f1.write(str(score))
    f1.write('\n')
    
f1.close()




