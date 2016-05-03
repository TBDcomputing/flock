#!/usr/bin/env python

import cmd
import json
import readline
import rlcompleter
import socket
import sys
import threading
import time

FLOCK_JAR = "path/to/jar"

HOST = "localhost"
PORT = 8900
BUFSIZE = 10240

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
    resp = json.loads(jsonmsg)

    if resp["type"] == "nodelist":
        print "\nNodes(%d):" % (len(resp["nodes"]))
        print "--------------------"
        for m in resp["nodes"]:
            print m["address"]
    elif resp["type"] == "leader_ip":
        print "\nLeader: ssh -p 8895 root@%s" % (resp["leader_ip"])
    elif resp["type"] == "has_image":
        if resp["nodes"]:
            print "\nNodes(%d) with image:" % (len(resp["nodes"]))
            print "--------------------"
            for m in resp["nodes"]:
                print m["address"]
        else:
            print "\nNo nodes found."
    elif resp["type"] == "run_image":
        resp["nodes"] = [c for c in resp["nodes"] if c]
        if resp["nodes"]:
            print "\nNodes(%d) running image:" % (len(resp["nodes"]))
            print "--------------------"
            for m in resp["nodes"]:
                m = json.loads(m)
                print "ssh -p 8895 root@%s" % (m["ip"][1:])
        else:
            print "\nNo nodes ran this image."


def send_message(mtype, arg=None):
    msg = {"type": mtype}
    if mtype in ["has_image", "run_image", "stop_image"] and arg:
        msg["image"] = arg
    elif mtype in ["start_election"] and arg:
        msg["config"] = arg

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

    def help_stop_image(self):
        print "Asks Flock to shut down containers with the specified image."
        print "stop_image <image>"

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

    def do_stop_image(self, image):
        if not image:
            print "error: stop_image <image>"
        else:
            send_message(mtype="stop_image", arg=image)

    def do_start_election(self, alpha_config_str):
        if not alpha_config_str:
            send_message(mtype="start_election")
        else:
            alpha_config = alpha_config_str.split()
            send_message(mtype="start_election", arg=alpha_config)

    def do_query_leader(self, s):
        send_message(mtype="leader")

    def do_nodelist(self, s):
        send_message(mtype="nodelist")


if __name__ == "__main__":
    """
    import subprocess
    if subprocess.call(["java", "-jar", FLOCK_JAR]):
        sys.exit(1)
    print "Flock is starting up ..."
    time.sleep(2)
    """

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
