from py4j.java_gateway import JavaGateway

if __name__ == '__main__':
    gateway = JavaGateway()
    jvm = gateway.jvm
    pkg = jvm.plu.teamtwo.rtm
    stack = gateway.entry_point.getStack()
    stack.push("test")
    stack.push("hello world")
    print(stack.pop())
    print(stack.pop())
    gateway.shutdown()