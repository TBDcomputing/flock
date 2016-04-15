#!/usr/bin/env python

import cmd
import glob
import json
import os
import os.path
import readline
import rlcompleter
import socket
import subprocess
import sys
import threading

SPARKDIR = "/opt/spark"
HOST = "localhost"
PORT = 8900
ADDR = (HOST, PORT)
BUFSIZE = 4096

START_MASTER = SPARKDIR + "/sbin/start-master.sh"
START_SLAVE = SPARKDIR + "/sbin/start-slave.sh"
STOP_MASTER = SPARKDIR + "/sbin/stop-master.sh"
STOP_SLAVE = SPARKDIR + "/sbin/stop-slave.sh"
SUBMIT = SPARKDIR + "/bin/spark-submit"
SLAVE_LIST = SPARKDIR + "/conf/slaves.template"
MASTER_URL = "spark://{0}:7077"

terminate = False
master = None


def listener():
    flock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    flock.bind((ADDR))
    flock.setblocking(0)

    def listen():
        try:
            msg, sender = flock.recvfrom(BUFSIZE)
            if len(msg) > 0:
                processmsg(msg, sender)
        except:
            pass

    while not terminate:
        listen()


def processmsg(msg, sender):
    print msg

    if msg == "election done":
        # ask flock for master
        pass
    elif msg == "master ip":
        # set master
        # if master, start up master, ask for node list
        # start slave
        pass
    elif msg == "node list":
        # add slaves
        pass
    elif msg == "node update":
        # add slaves
        pass


def submittask(script, argv):
    subprocess.Popen(
        [SUBMIT, "--master", MASTER_URL.format(master), script] + argv, shell=False)


def addslaves(slaves, overwrite):
    addrs = set()
    if not overwrite:
        addrs.update(filter(lambda line: not line.startswith("#"),
                            open(SLAVE_LIST, "r").readlines()))
    else:
        addrs.add("localhost\n")

    addrs.update(map(lambda addr: addr + "\n", slaves))
    open(SLAVE_LIST, "w").writelines(addrs)


def master():
    subprocess.Popen([START_MASTER], shell=False)


def slave():
    subprocess.Popen([START_SLAVE, MASTER_URL.format(master)], shell=False)


def kill():
    subprocess.Popen([STOP_MASTER], shell=False)
    subprocess.Popen([STOP_SLAVE], shell=False)


class CLI(cmd.Cmd):

    def __init__(self):
        cmd.Cmd.__init__(self)
        self.prompt = "\033[92mmiddleware> \033[0m"
        if "libedit" in readline.__doc__:
            readline.parse_and_bind("bind ^I rl_complete")
        else:
            readline.parse_and_bind("tab: complete")
        readline.set_completer(self.complete)

    def emptyline(self):
        pass

    def do_submit(self, s):
        argv = s.split()
        script, argv = argv[0], argv[1:]

        if not os.path.isfile(script):
            print "error:", script
            return

        submittask(script, argv)

    def complete_submit(self, text, line, begin, end):
        before_arg = line.rfind(" ", 0, begin)
        if before_arg == -1:
            return

        fixed = line[before_arg + 1:begin]
        arg = line[before_arg + 1:end]
        pattern = arg + '*'

        completions = []
        for path in glob.glob(pattern):
            if path and os.path.isdir(path) and path[-1] != os.sep:
                path += os.sep
            completions.append(path.replace(fixed, "", 1))
        return completions

    def do_election(self, s):
        print s

    def do_exit(self, s):
        global terminate, listenerthread
        kill()
        terminate = True
        listenerthread.join()
        return True


def checkfiles():
    ok = True
    for file in [START_MASTER, START_SLAVE, STOP_MASTER, STOP_SLAVE, SUBMIT, SLAVE_LIST]:
        if not os.path.isfile(file):
            print "error:", file, "not found"
            ok = False

    if not ok:
        sys.exit(1)


if __name__ == "__main__":
    checkfiles()

    listenerthread = threading.Thread(target=listener)
    listenerthread.start()

    try:
        CLI().cmdloop()
    except KeyboardInterrupt:
        kill()
        terminate = True
        listenerthread.join()
