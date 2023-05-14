from Party_Class import MessangerParty
from chat_interface import Interface
from methods import return_time_stamp, reading_file
import multiprocessing
import time

FILE_NAME = "Transmitted_Data.txt"


def run_reading_file():
    reading_file(FILE_NAME)


lock = multiprocessing.Lock()

if __name__ == '__main__':
    fileReader = multiprocessing.Process(target=run_reading_file)

    name = input("What is your username?\n")

    sender = MessangerParty(1, name)

    f = open(FILE_NAME, "a")

    f.write("[" + return_time_stamp() + "]: " + sender.name + " has joined the chat!\n")
    Interface.Users = Interface.Users + 1
    print(Interface.Users)
    f.close()
    f = open(FILE_NAME, "r")
    print(f.readline())
    f.seek(0, 0)
    f.close()

    message = ""
    fileReader.start()
    while message != "!quit":
        message = input()
        if message != "!quit":
            lock.acquire()
            f = open(FILE_NAME, "a")
            f.write("[" + return_time_stamp() + "]: " + sender.name + "--> " + message + '\n')
            f.close()
            lock.release()
            # f = open(FILE_NAME, "r")
            # print(f.readlines()[-1])
            # f.close()

    f = open(FILE_NAME, "a")
    f.write("[" + return_time_stamp() + "]: " + sender.name + " has left the chat!\n")
    f.close()
    # f = open(FILE_NAME, "r")
    # print(f.readline())
    # f.close()
    fileReader.terminate()
    Interface.Users = Interface.Users - 1



