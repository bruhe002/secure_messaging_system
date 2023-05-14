import time
import multiprocessing as mp
import os


lock = mp.Lock()


def return_time_stamp():
    seconds = time.time()
    return time.ctime(seconds)


def reading_file(file_name: str):
    lock.acquire()
    file = open(file_name, 'r')
    message = file.readlines()[-1]
    file.seek(0, 0)
    lock.release()
    while True:
        if os.path.getsize('Transmitted_Data.txt') != 0:
            file.seek(0, 0)
            if message != file.readlines()[-1]:
                file.seek(0, 0)
                message = file.readlines()[-1]
                print(message)
                file.seek(0, 0)

