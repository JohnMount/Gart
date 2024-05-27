

import os
from multiprocessing import cpu_count, Pool

import numpy as np

from render_gart import render_gart
from farchive import farchive


img_dir = 'imgs'


def mk_img_job(idxs):
    for i in idxs:
        formula_str = farchive[i]
        img = render_gart(
            formula_str,
            img_height=1600,
            img_width=2560,
            aa_scale=2,
        )
        img.save(f"{img_dir}/{i:05d}.jpeg")


if __name__ == "__main__":
    try:
        os.mkdir(img_dir)
    except FileExistsError:
        pass
    tasks = []
    f_set = set()
    for i, formula_str in enumerate(farchive):
        if formula_str not in f_set:
            f_set.add(formula_str)
            tasks.append(i)
    # choose degree of parallelism
    nproc = np.max([1, cpu_count() - 1])
    # break tasks into sub-lists
    if (nproc > 1) and (len(tasks) > 1):
        n_lists = np.min([len(tasks), np.max([nproc, int(len(tasks) / (4 * nproc))])])
        rng = np.random.default_rng()
        rng.shuffle(tasks)
        task_lists = np.array_split(tasks, n_lists)
        task_lists = [tuple(tl) for tl in task_lists if tl.shape[0] > 0]
    else:
        task_lists = [tasks]
    if (nproc > 1) and (len(task_lists) > 1):
        with Pool(processes=nproc) as pool:
            pool.map(mk_img_job, task_lists)
    else:
        for tl in task_lists:
            mk_img_job(tl)
    print("done")
