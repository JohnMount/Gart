
from typing import Iterable, Tuple
import numpy as np

from PIL import Image

from quaternion import Quaternions
from op_map import formula_to_op_tree, OpDescr


class QOP_tree:
    def __init__(
            self,
            *,
            op: OpDescr,
            q: Quaternions,
            method,
            args: Iterable["QOP_tree"],
    ):
        self.op = op
        self.q = q
        self.method = method
        self.args = tuple(args)
    
    def eval_tree(
            self,
            *,
            x, y, z,
    ) -> Quaternions:
        assert self.args is not None
        if len(self.args) < 1:
            if self.op.depends_on_coords:
                self.method(x, y, z)
            else:
                self.method()
        else:
            q_args = [ai.eval_tree(x=x, y=y, z=z) for ai in self.args]
            self.method(*q_args)
            for ai in self.args:
                ai.q.clear()
        return self.q


def r_dispatch(nd, data_shape: Tuple) -> QOP_tree:
    """Build an evaluation tree from a representation tree"""
    q = Quaternions(data_shape)
    if isinstance(nd, OpDescr):
        return QOP_tree(op=nd,
                    q=q,
                    method=getattr(q, nd.call_name),
                    args=[],
            )
    assert isinstance(nd[0], OpDescr)
    return QOP_tree(op=nd[0],
                q=q,
                method=getattr(q, nd[0].call_name),
                args=[r_dispatch(nd[i], data_shape=data_shape) for i in range(1, len(nd))],
        )



def render_gart(
    formula_str: str,
    *,
    img_height: int = 225,
    img_width: int = 400,
    aa_scale: int = 2,
    buggy_crunch:bool = True,
):
    width= aa_scale * img_width
    height = aa_scale * img_height
    data_shape = (width, height)
    op_tree = formula_to_op_tree(formula_str)
    res = r_dispatch(op_tree, data_shape=data_shape)
    # build data tensors
    min_port = np.min([width, height])
    x_vec = np.linspace(start=-0.5 * (width/min_port), stop=0.5 * (width/min_port), num=width)
    y_vec = np.linspace(start=-0.5 * (height/min_port), stop=0.5 * (height/min_port), num=height)
    x = np.zeros(data_shape)
    y = np.zeros(data_shape)
    z = np.zeros(data_shape)
    for xi in range(width):
        for yi in range(height):
                x[xi][yi] = x_vec[xi]
                y[xi][yi] = y_vec[yi]
    del x_vec
    del y_vec
    res_q = res.eval_tree(x=x, y=y, z=z)
    del res
    # define image shape
    image_shape = (width, height, 3)
    # copy result into image
    img_tensor = np.zeros(image_shape)
    for xi in range(width):
        for yi in range(height):
            img_tensor[xi][yi][0] = res_q.i[xi][yi]
            img_tensor[xi][yi][1] = res_q.j[xi][yi]
            img_tensor[xi][yi][2] = res_q.k[xi][yi]
    del res_q
    # move to 0 to 255 integers
    if buggy_crunch:
        out_of_range = (img_tensor>30.0) | (img_tensor<-30.0)
        img_tensor[out_of_range] = 0
    else:
        img_tensor = np.maximum(-30, img_tensor)
        img_tensor = np.minimum(30, img_tensor)
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
