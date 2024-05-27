"""Genetic art system. Copyright (C) 1995-20024 John Mount (jmount@mzlabs.com)"""

from typing import Iterable, Tuple

import numpy as np


np.seterr(divide="ignore", invalid="ignore")


class Quaternions:
    def clear(self):
        self.e = None
        self.i = None
        self.j = None
        self.k = None

    def __init__(self, shape: Tuple[int, ...]) -> None:
        self.shape = tuple(shape)
        self.clear()

    def new_instance(self) -> "Quaternions":
        return Quaternions(self.shape)

    def normsq(self) -> np.array:
        return self.e * self.e + self.i * self.i + self.j * self.j + self.k * self.k

    def getE(self) -> np.array:
        return self.e

    def getI(self) -> np.array:
        return self.i

    def getJ(self) -> np.array:
        return self.j

    def getK(self) -> np.array:
        return self.k

    # basic quaternion ops that can have this equal to inputs

    # sets this fields to a
    def set(self, a: "Quaternions") -> None:
        self.e = a.e.copy()
        self.i = a.i.copy()
        self.j = a.j.copy()
        self.k = a.k.copy()

    def qplus(self, a: "Quaternions", b: "Quaternions") -> None:
        self.e = a.e + b.e
        self.i = a.i + b.i
        self.j = a.j + b.j
        self.k = a.k + b.k

    def qneg(self, a: "Quaternions") -> None:
        self.e = -a.e
        self.i = -a.i
        self.j = -a.j
        self.k = -a.k

    def qsub(self, a: "Quaternions", b: "Quaternions") -> None:
        self.e = a.e - b.e
        self.i = a.i - b.i
        self.j = a.j - b.j
        self.k = a.k - b.k

    def qinv(self, a: "Quaternions") -> None:
        d = np.nan_to_num(1 / a.normsq(), nan=0, posinf=0, neginf=0)
        # arbitrary behavior for dividing by 0 or near 0
        self.e = a.e * d
        self.i = -a.i * d
        self.j = -a.j * d
        self.k = -a.k * d

    def qconj(self, a: "Quaternions") -> None:
        self.e = a.e
        self.i = -a.i
        self.j = -a.j
        self.k = -a.k

    def qmult(self, a: "Quaternions", b: "Quaternions") -> None:
        self.e = a.e * b.e - a.i * b.i - a.j * b.j - a.k * b.k
        self.i = a.e * b.i + a.i * b.e + a.j * b.k - a.k * b.j
        self.j = a.e * b.j - a.i * b.k + a.j * b.e + a.k * b.i
        self.k = a.e * b.k + a.i * b.j - a.j * b.i + a.k * b.e

    def qdiv(self, a: "Quaternions", b: "Quaternions") -> None:
        t1 = self.new_instance()
        t1.qinv(b)
        self.qmult(a, t1)

    # the unit quaternions under multiplication form an important group
    # ( isomorphic to SU(2) (2x2 complex matrices with determinant 1) )
    # normalizing quaternions injects us into the group
    def qnorm(self, a: "Quaternions") -> None:
        d = np.nan_to_num(np.sqrt(1 / a.normsq()), nan=0, posinf=0, neginf=0)
        self.e = a.e * d
        self.i = a.i * d
        self.j = a.j * d
        self.k = a.k * d

    # the projective version of the unit quaternions (identifying a with -a)
    # is another important group isomophic to PSU(2) qnormp injects into this
    def qnormp(self, a: "Quaternions") -> None:
        self.qnorm(a)
        chk = self.e < 0
        self.e[chk] *= -1
        self.i[chk] *= -1
        self.j[chk] *= -1
        self.k[chk] *= -1
        chk = (self.e <= 0) & (self.i < 0)
        self.e[chk] *= -1
        self.i[chk] *= -1
        self.j[chk] *= -1
        self.k[chk] *= -1
        chk = (self.e <= 0) & (self.i <= 0) & (self.j < 0)
        self.e[chk] *= -1
        self.i[chk] *= -1
        self.j[chk] *= -1
        self.k[chk] *= -1
        chk = (self.e <= 0) & (self.i <= 0) & (self.j <= 0) & (self.k < 0)
        self.e[chk] *= -1
        self.i[chk] *= -1
        self.j[chk] *= -1
        self.k[chk] *= -1

    # a coordinate independent rounding to the integer
    # subring of the quaternions
    def qfloor(self, a: "Quaternions") -> None:
        self.e = np.floor(a.e)
        self.i = np.floor(a.i)
        self.j = np.floor(a.j)
        self.k = np.floor(a.k)

    # constant yielding ops

    def qc1(self) -> None:
        self.e = np.zeros(self.shape) + 1.0
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    def qc2(self) -> None:
        self.e = np.zeros(self.shape)
        self.i = np.zeros(self.shape) + 1.0
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    def qc3(self) -> None:
        self.e = np.zeros(self.shape)
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape) + 1.0
        self.k = np.zeros(self.shape)

    def qc4(self) -> None:
        self.e = np.zeros(self.shape)
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape) + 1.0

    def qc5(self) -> None:
        self.e = np.zeros(self.shape) + 1.61803398875  # golden ratio
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    # variable entry points

    def qstd(self, x, y, z) -> None:
        self.e = np.zeros(self.shape)
        self.i = np.zeros(self.shape) + x
        self.j = np.zeros(self.shape) + y
        self.k = np.zeros(self.shape) + z

    def qcx(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + x
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    def qcy(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + y
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    # extras to help close tree and make eqns reactive
    def qcx1(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + x
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape) + 1.0 + z

    def qcy1(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + y
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape) + 1.0 + z

    def qcx2(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + x
        self.i = np.zeros(self.shape) + 1.0 + z
        self.j = np.zeros(self.shape)
        self.k = np.zeros(self.shape)

    def qcy2(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + y
        self.i = np.zeros(self.shape)
        self.j = np.zeros(self.shape) + 1.0 + z
        self.k = np.zeros(self.shape)

    def qcxy(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + x
        self.i = np.zeros(self.shape) + y
        self.j = np.zeros(self.shape) + z
        self.k = np.zeros(self.shape) + z

    def qcxy2(self, x, y, z) -> None:
        self.e = np.zeros(self.shape) + x
        self.i = np.zeros(self.shape) + y
        self.j = np.zeros(self.shape) + x + z
        self.k = np.zeros(self.shape) + y + z

    # coordinate independent functions
    # not interesting from the Quaternions point of view- but since we
    # derive colors from the coordinates independently these functions should
    # have some visual value.

    def qisin(self, a: "Quaternions") -> None:
        self.e = np.sin(a.e)
        self.i = np.sin(a.i)
        self.j = np.sin(a.j)
        self.k = np.sin(a.k)

    def qilog(self, a: "Quaternions") -> None:
        self.e = np.log(np.maximum(a.e, 0.000001))
        self.i = np.log(np.maximum(a.i, 0.000001))
        self.j = np.log(np.maximum(a.j, 0.000001))
        self.k = np.log(np.maximum(a.k, 0.000001))

    def qiexp(self, a: "Quaternions") -> None:
        self.e = np.exp(np.minimum(np.maximum(a.e, -30.0), 30.0))
        self.i = np.exp(np.minimum(np.maximum(a.i, -30.0), 30.0))
        self.j = np.exp(np.minimum(np.maximum(a.j, -30.0), 30.0))
        self.k = np.exp(np.minimum(np.maximum(a.k, -30.0), 30.0))

    def qimin(self, a: "Quaternions", b: "Quaternions") -> None:
        self.e = np.minimum(a.e, b.e)
        self.i = np.minimum(a.i, b.i)
        self.j = np.minimum(a.j, b.j)
        self.k = np.minimum(a.k, b.k)

    def qimax(self, a: "Quaternions", b: "Quaternions") -> None:
        self.e = np.maximum(a.e, b.e)
        self.i = np.maximum(a.i, b.i)
        self.j = np.maximum(a.j, b.j)
        self.k = np.maximum(a.k, b.k)

    # shift coordinates around (not a Quaternion automorphism)

    def qrl(self, a: "Quaternions") -> None:
        t = a.e
        self.e = a.i
        self.i = a.j
        self.j = a.k
        self.k = t

    def qrr(self, a: "Quaternions") -> None:
        t = a.k
        self.k = a.j
        self.j = a.i
        self.i = a.e
        self.e = t

    # more exotic ops (can not have this equal to an argument)

    # all R-algrebra automorphisms of H (the quaternions) are of the form
    # qaut1 by corollary of a theorem of Cayley's
    def qaut1(self, a: "Quaternions", b: "Quaternions") -> None:
        t2 = self.new_instance()
        t2.qdiv(b, a)
        self.qmult(a, t2)

    # not an automorphism- but close
    def qaut2(self, a: "Quaternions", b: "Quaternions") -> None:
        t3 = self.new_instance()
        t3.qconj(b)
        self.qaut1(a, t3)

    # exponential map -
    #    using first few terms of power series as approximation
    def qexp(self, ai: "Quaternions") -> None:
        p = self.new_instance()
        a = self.new_instance()
        t3 = self.new_instance()
        a.set(ai)
        # don't touch big arguments
        while True:
            chk = a.normsq() > 900.0
            if np.sum(chk) < 1:
                break
            a.e[chk] /= 10.0
            a.i[chk] /= 10.0
            a.j[chk] /= 10.0
            a.k[chk] /= 10.0
        self.set(a)
        p.set(a)
        self.e += 1.0  # zero power term
        for ti in range(2, 10):
            d = 1.0 / ti
            t3.qmult(p, a)
            p.e = t3.e * d
            p.i = t3.i * d
            p.j = t3.j * d
            p.k = t3.k * d
            t3.set(self)
            self.qplus(t3, p)

    # there is a polar representation of quaternions: for every a: "Quaternions"
    # there is an imaginvar Quaternions u s.t. a = |a| exp(u).
    # finding u looks hard.
    # we can use the fact the u.u = -|u|^2 to break exp(u) into two real
    # series: sum_{k=0^{\infty (-|u|^2)^k / (2 k)! +
    # u (sum_{k=0^{\infty (-|u|^2)^k / ( 2 k +1)!
    # but this looks like a mess

    # by Hamilton's theorem for every ortogonal mappinger f: Im H->Im H there
    # is a unit a: "Quaternions" s.t. f(b) = a b bar(a) or f(b) = - a b bar(a)
    # qorth1 and qorth2 implement these maps
    def qorth1(self, a: "Quaternions", b: "Quaternions") -> None:
        t1 = self.new_instance()
        t2 = self.new_instance()
        t3 = self.new_instance()
        t1.qnorm(a)
        t2.qconj(t1)
        t3.qmult(b, t2)
        self.qmult(t1, t3)

    def qorth2(self, a: "Quaternions", b: "Quaternions") -> None:
        self.qorth1(a, b)
        self.qneg(self)

    # mod (or remainder) over the quaternions in analogy to traditional mod
    def qmod(self, a: "Quaternions", b: "Quaternions") -> None:
        self.qdiv(a, b)
        self.qfloor(self)
        t1 = self.new_instance()
        t1.qmult(self, b)
        self.qsub(a, t1)
