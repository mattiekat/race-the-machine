from py4j.java_gateway import JavaGateway, CallbackServerParameters


class PyListener(object):
    def __init__(self):
        print("Initialized PyListener")

    def notify(self, msg, x):
        print(msg)
        return '{}: "{}" was received by python.'.format(x, msg)

    class Java:
        implements = ["plu.teamtwo.rtm.experiments.PythonInterfaceTest$Listener"]


if __name__ == '__main__':
    gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())
    gateway.entry_point.registerListener(PyListener())
    gateway.entry_point.notifyAllListeners("Hello World")
    gateway.entry_point.registerListener(PyListener())
    gateway.entry_point.notifyAllListeners("I am alive!")
    gateway.shutdown()