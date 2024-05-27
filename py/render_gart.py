
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


x_y_z_cache = dict()

def build_x_y_z(data_shape: Tuple[int, int, ...]):
    try:
        return x_y_z_cache[data_shape]
    except KeyError:
        pass
    # build data tensors
    width = data_shape[0]
    height = data_shape[1]
    min_port = np.min([width, height])
    x_vec = np.linspace(start=-0.5 * (width/min_port), stop=0.5 * (width/min_port), num=width)
    y_vec = np.linspace(start=-0.5 * (height/min_port), stop=0.5 * (height/min_port), num=height)
    # fuzzcoords
    eps = 0.1 / max(width, height)
    rng = np.random.default_rng(2024)
    x_vec = x_vec + rng.uniform(-eps, eps, x_vec.shape[0])
    y_vec = y_vec + rng.uniform(-eps, eps, y_vec.shape[0])
    # convert shape
    x = np.zeros(data_shape)
    y = np.zeros(data_shape)
    z = np.zeros(data_shape)
    for xi in range(width):
        for yi in range(height):
                x[xi][yi] = x_vec[xi]
                y[xi][yi] = y_vec[yi]
    # cache and return
    if len(x_y_z_cache) > 5:
        x_y_z_cache.clear()
    x_y_z_cache[data_shape] = (x, y, z)
    return x, y, z


def crunch_range(vec, *, buggy_crunch:bool = True):
    # move to 0 to 255 integers
    if buggy_crunch:
        out_of_range = (vec>30.0) | (vec<-30.0)
        vec[out_of_range] = 0
    vec = np.maximum(-30, vec)
    vec = np.minimum(30, vec)
    vec = np.round(256 / (1 + np.exp(-vec)))
    if buggy_crunch:
        vec[out_of_range] = 0
    vec = np.maximum(vec, 0)
    vec = np.minimum(vec, 255)
    return vec


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
    x, y, z = build_x_y_z(data_shape)
    res_q = res.eval_tree(x=x, y=y, z=z)
    del res
    # copy result into image
    i_values = crunch_range(res_q.i)
    j_values = crunch_range(res_q.j)
    k_values = crunch_range(res_q.k)
    del res_q
    img = Image.new("RGB", size=(width, height))
    pixels = [(0,0,0)] * (width * height)
    idx = 0
    for yi in range(height):
        for xi in range(width):
            pixels[idx] = (int(i_values[xi][yi]), int(j_values[xi][yi]), int(k_values[xi][yi]))
            idx = idx + 1
    assert idx == len(pixels)
    img.putdata(pixels)
    if aa_scale > 1:
        img = img.resize((img_width, img_height))
    return img
