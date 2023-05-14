from Party_Class import MessangerParty
import time

FILE_NAME = "Transmitted_Data.txt"

name = input("What is your user name?\n")

receiver = MessangerParty(1, name)
seconds = time.time()
timeStamp = time.ctime(seconds)
f = open(FILE_NAME, "w")

f.write("[" + timeStamp + "]: " + receiver.name + " has joined the chat!\n")

f.close()
f = open(FILE_NAME, "r")

print(f.read())
