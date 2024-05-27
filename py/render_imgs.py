

import os
from multiprocessing import cpu_count, Pool

from IPython.display import display

from render_gart import render_gart
from farchive import farchive


img_dir = 'imgs'


def mk_img_job(i):
    formula_str = farchive[i]
    img = render_gart(
        formula_str,
        img_height=1440,
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
    i = 0
    for i in range(len(farchive)):
        formula_str = farchive[i]
        if formula_str not in f_set:
            f_set.add(formula_str)
            tasks.append(i)
    nproc = 2
    if nproc > 1:
        with Pool(processes=nproc) as pool:
            pool.map(mk_img_job, tasks)
    else:
        for task in tasks:
            mk_img_job(task)
    print("done")
