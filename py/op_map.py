
from typing import Iterable, Tuple

from dataclasses import dataclass, field
import re

import sexpdata

from quaternion import Quaternions


op_list = (
    ("+", 2, "qplus(a, b)"),
    ("-", 2, "qsub(a, b)"),
    ("*", 2, "qmult(a, b)"),
    ("inv", 1, "qinv(a)"),
    ("/", 2, "qdiv(a, b)"),
    ("conj", 1, "qconj(a)"),
    ("A1", 2, "qaut1(a, b)"),
    ("A2", 2, "qaut2(a, b)"),
    ("exp", 1, "qexp(a)"),
    ("floor", 1, "qfloor(a)"),
    ("mod", 2, "qmod(a, b)"),
    ("normalize", 1, "qnorm(a)"),
    ("normp", 1, "qnormp(a)"),
    ("orth1", 2, "qorth1(a, b)"),
    ("orth2", 2, "qorth2(a, b)"),
    ("1", 0, "qc1()"),
    ("i", 0, "qc2()"),
    ("j", 0, "qc3()"),
    ("k", 0, "qc4()"),
    ("golden", 0, "qc5()"),
    ("x", 0, "qcx(x, y, z)"),
    ("y", 0, "qcy(x, y, z)"),
    ("x_k", 0, "qcx1(x, y, z)"),
    ("y_k", 0, "qcy1(x, y, z)"),
    ("x_iy", 0, "qcxy(x, y, z)"),
    ("x_iy_jx_ky", 0, "qcxy2(x, y, z)"),
    ("isin", 1, "qisin(a)"),
    ("ilog", 1, "qilog(a)"),
    ("iexp", 1, "qiexp(a)"),
    ("imin", 2, "qimin(a, b)"),
    ("imax", 2, "qimax(a, b)"),
    ("rolL", 1, "qrl(a)"),
    ("rolR", 1, "qrr(a)"),
    ("subst", 2, "set(b)"),
)


@dataclass
class OpDescr:
    """class to represent operation"""
    name: str
    degree: int
    call_str: str
    call_name: str = field(init=False)
    depends_on_coords: bool = field(init=False)

    def __post_init__(self):
        self.call_name = self.call_str[:self.call_str.find('(')]
        self.depends_on_coords = '(x, y, z)' in self.call_str


# map names of call to method names and signatures
call_map = {
    op[0].strip().lower(): OpDescr(name=op[0], degree=op[1], call_str=op[2])
    for op in op_list
}


def lookup_symbol(s: str) -> Quaternions:
    s1 = s.strip().lower()
    try:
        return call_map[s1]
    except KeyError:
        pass
    s2 = re.sub(r'[\s]*[+][\s]*', '_', s1)
    return call_map[s2]


def r_walk_tree(tree):
    if isinstance(tree, sexpdata.Symbol):
        return lookup_symbol(tree.title())
    return tuple([r_walk_tree(nd) for nd in tree])


def formula_to_op_tree(f: str):
    tree = sexpdata.loads(f)
    op_tree = r_walk_tree(tree)
    return op_tree


class QOP_tree:
    def __init__(
            self,
            *,
            op: OpDescr,
            q: Quaternions,
            method,
            args: Iterable[Quaternions],
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
        if len(self.args) < 1:
            if self.op.depends_on_coords:
                self.method(x, y, z)
            else:
                self.method()
        else:
            q_args = [ai.eval_tree(x=x, y=y, z=z) for ai in self.args]
            self.method(*q_args)
        return self.q


def r_dispatch(nd, img_shape: Tuple) -> QOP_tree:
    """Build an evaluation tree from a representation tree"""
    q = Quaternions(img_shape)
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
                args=[r_dispatch(nd[i], img_shape=img_shape) for i in range(1, len(nd))],
        )
