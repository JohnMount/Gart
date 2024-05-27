
import numpy as np
from PIL import Image
from IPython.display import display

from quaternion import Quaternions
from op_map import formula_to_op_tree, OpDescr, QOP_tree, r_dispatch


def render_gart(
    formula_str: str,
    *,
    img_height: int = 225,
    img_width: int = 400,
    aa_scale: int = 1,
):
    depth = 1
    width= aa_scale * img_width
    height = aa_scale * img_height
    data_shape = (width, height, 1)
    op_tree = formula_to_op_tree(formula_str)
    res = r_dispatch(op_tree, data_shape=data_shape)
    # build data tensors
    min_port = np.min([width, height])
    x_vec = np.linspace(start=-0.5 * (width/min_port), stop=0.5 * (width/min_port), num=width)
    y_vec = np.linspace(start=-0.5 * (height/min_port), stop=0.5 * (height/min_port), num=height)
    z_vec = np.zeros(1)
    x = np.zeros(data_shape)
    y = np.zeros(data_shape)
    z = np.zeros(data_shape)
    for xi in range(width):
        for yi in range(height):
            for zi in range(depth):
                x[xi][yi][zi] = x_vec[xi]
                y[xi][yi][zi] = y_vec[yi]
                z[xi][yi][zi] = z_vec[zi]
    res_q = res.eval_tree(x=x, y=y, z=z)
    # define image shape
    image_shape = (width, height, 3)
    # copy result into image
    img_tensor = np.zeros(image_shape)
    for xi in range(width):
        for yi in range(height):
            img_tensor[xi][yi][0] = res_q.i[xi][yi][0]
            img_tensor[xi][yi][1] = res_q.j[xi][yi][0]
            img_tensor[xi][yi][2] = res_q.k[xi][yi][0]
    # move to 0 to 255 integers
    img_tensor = np.maximum(-100, img_tensor)
    img_tensor = np.minimum(100, img_tensor)
    img_tensor = np.round(256 / (1 + np.exp(-img_tensor)))
    img_tensor = np.maximum(img_tensor, 0)
    img_tensor = np.minimum(img_tensor, 255)
    img = Image.new("RGB", size=(width, height))
    pixels = []
    for yi in range(height):
        for xi in range(width):
            pixels.append((int(img_tensor[xi][yi][0]), int(img_tensor[xi][yi][1]), int(img_tensor[xi][yi][2])))
    img.putdata(pixels)
    if aa_scale > 1:
        img = img.resize((img_width, img_height))
    return img
