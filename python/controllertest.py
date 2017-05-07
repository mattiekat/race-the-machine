from py4j.java_gateway import JavaGateway


class XORScore(object):



gateway = JavaGateway()
jvm = gateway.jvm
pkg = jvm.plu.teamtwo.rtm
gateway.entry_point.init(pkg.neat.Encoding.DIRECT_ENCODING, 3, 1)
controller = gateway.getController()

controller.createFirstGeneration()

#for _ in range(100):
#    controller.assesGeneration()