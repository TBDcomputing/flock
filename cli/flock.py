#!/usr/bin/env python

import cmd
import json
import readline
import rlcompleter
import socket
import sys
import threading
import time

HOST = "localhost"
PORT = 8900
BUFSIZE = 2048

listenerthread = None
sock = None
terminate = False
master = None


def listener():
    global sock
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    try:
        sock.connect((HOST, PORT))
    except:
        global terminate
        terminate = True
        print "error: cannot connect to Flock."

    def listen():
        try:
            msg = sock.recv(BUFSIZE)
            if msg:
                process_message(msg)
        except:
            pass

    sock.settimeout(1)

    while not terminate:
        listen()
    sock.close()


def process_message(jsonmsg):
    msg = json.loads(jsonmsg)
    print msg
    # TODO: handle arrays like from nodelist
    for k, v in msg.items():
        print k, v


def send_message(mtype, arg=None):
    msg = {"type": mtype}
    if (mtype == "has_image" or mtype == "run_image") and arg:
        msg["image"] = arg

    jsonmsg = json.dumps(msg)
    # added new line since it uses readLine() in Java
    sock.sendall(jsonmsg + '\n')


class CLI(cmd.Cmd):

    def __init__(self):
        cmd.Cmd.__init__(self)
        self.prompt = "\033[92mflock> \033[0m"
        if "libedit" in readline.__doc__:
            readline.parse_and_bind("bind ^I rl_complete")
        else:
            readline.parse_and_bind("tab: complete")
        readline.set_completer(self.complete)

    def emptyline(self):
        pass

    def help_exit(self):
        print "self-explanatory"

    def help_has_image(self):
        print "Queries Flock for nodes that have the specified image."
        print "has_image <image>"

    def help_run_image(self):
        print "Asks Flock to spin up containers with the specified image."
        print "run_image <image>"

    def help_start_election(self):
        print "Starts an election on Flock to determine the leader of the cluster."

    def help_query_leader(self):
        print "Queries Flock for the current leader of the cluster."

    def help_nodelist(self):
        print "Asks Flock for a complete list of nodes in the cluster."

    def do_exit(self, s):
        global terminate, listenerthread
        terminate = True
        listenerthread.join()
        return True

    def do_has_image(self, image):
        if not image:
            print "error: has_image <image>"
        else:
            send_message(mtype="has_image", arg=image)

    def do_run_image(self, image):
        if not image:
            print "error: run_image <image>"
        else:
            send_message(mtype="run_image", arg=image)

    def do_start_election(self, s):
        send_message(mtype="start_election")

    def do_query_leader(self, s):
        send_message(mtype="leader")

    def do_nodelist(self, s):
        send_message(mtype="nodelist")


if __name__ == "__main__":
    listenerthread = threading.Thread(target=listener)
    listenerthread.start()

    time.sleep(0.2)
    if terminate:
        sys.exit(0)

    try:
        CLI().cmdloop()
    except KeyboardInterrupt:
        terminate = True
        listenerthread.join()
