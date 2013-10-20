from random import gauss, randint, uniform

def point_around(x, y, sigma=1):
    return [gauss(x, sigma), gauss(y, sigma)]

def n_points_around(x, y, n=10, sigma=1):
    return [point_around(x, y, sigma) for i in range(n)]

def random_around(x, y):
    return n_points_around(x, y, randint(5, 30), uniform(0.001, 0.1))

def random_box(a, b, c, d):
    n = int((c-a)*(d-b) * 1000 * gauss(1, 0.2))
    points = []
    for i in range(n):
        points += random_around(uniform(a,c), uniform(b, d))
    return points

def random_magn():
    return randint(1, 3)
