from py4j.java_gateway import JavaGateway, CallbackServerParameters


class PyListener(object):
    count = 0

    def __init__(self):
        self.x = PyListener.count
        PyListener.count += 1
        print("Initialized PyListener {}".format(self.x))

    def notify(self, msg):
        print(msg)
        return '{}: "{}" was received by python.'.format(self.x, msg)

    def duplicate(self):
        return PyListener()

    class Java:
        implements = ["plu.teamtwo.rtm.experiments.PythonInterfaceTest$Listener"]


if __name__ == '__main__':
    gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())
    gateway.entry_point.registerListener(PyListener())
    gateway.entry_point.notifyAllListeners("Hello World")
    gateway.entry_point.registerListener(PyListener())
    gateway.entry_point.duplicateAll()
    gateway.entry_point.notifyAllListeners("I am alive!")
    gateway.shutdown()


# class Generator(object):
#     def __init__(self):
#         print("Initialized Generator")
#
#     def getVal(self):
#         #return gateway.jvm.java.lang.Float(0.5)
#         #return gateway.jvm.java.lang.Float(0.5).floatValue()
#         #return numpy.float32(0.5)
#         return 0.5
#
#     class Java:
#         implements = ["plu.teamtwo.rtm.experiments.PythonInterfaceTest$RealGen"]
#
#
# if __name__ == '__main__':
#     gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())
#     gateway.entry_point.setGenerator(Generator())
#     gateway.entry_point.printVal()
#     gateway.shutdown()
