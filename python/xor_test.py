import random
import math
import numpy
from py4j.java_gateway import JavaGateway, CallbackServerParameters


class XORScorePy(object):
    inputs = [
        [1.0, 0.0, 0.0],
        [1.0, 0.0, 1.0],
        [1.0, 1.0, 0.0],
        [1.0, 1.0, 1.0]
    ]

    def __init__(self):
        self.error = 0.0
        self.correct = 0
        self.last = 0
        self.order = [0, 1, 2, 3]
        random.shuffle(self.order)
        self.expected = False

    def createNew(self):
        return XORScorePy()

    def getMaxThreads(self):
        return 1

    def flushBetween(self):
        return True

    def realTimeProcessing(self):
        return False

    def generateInput(self):
        j_inputs = None
        if self.last < 4:
            # update internal information
            inputs = XORScorePy.inputs[self.order[self.last]]
            self.expected = (int(inputs[1]) ^ int(inputs[2])) == 1
            self.last += 1

            # convert to java array
            j_inputs = gateway.new_array(gateway.jvm.float, 3)
            for x in range(0, 3):
                j_inputs[x] = inputs[x]

        return j_inputs

    def acceptOutput(self, output):
        self.error += math.fabs((1.0 if self.expected else 0.0) - output[0])
        if (output[0] >= 0.5) == self.expected:
            self.correct += 1

    def getScore(self):
        return (4.0 - self.error) ** 2.0

    def isWinner(self):
        return self.correct == 4

    class Java:
        implements = ['plu.teamtwo.rtm.neat.ScoringFunction']


if __name__ == '__main__':
    gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())

    gateway.entry_point.init(gateway.jvm.plu.teamtwo.rtm.neat.Encoding.DIRECT_ENCODING, 3, 1)
    controller = gateway.entry_point.getController()
    controller.createFirstGeneration()

    for _ in range(0, 500):
        found_winner = controller.assesGeneration(XORScorePy())
        best = controller.getBestIndividual()
        print('Gen {:d}: {:.2f}, {:.1f}'
              .format(controller.getGenerationNum(), controller.getFitness(), best.getFitness()))
        # print('generation')
        if found_winner:
            gson = gateway.jvm.com.google.gson.GsonBuilder().setPrettyPrinting().create()
            print(gson.toJson(best))
            break

        controller.nextGeneration()

    gateway.shutdown()
