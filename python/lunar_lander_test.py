import gym
import numpy
from py4j.java_gateway import JavaGateway, CallbackServerParameters

INPUT_SIZE = 8
OUTPUT_SIZE = 2
WINNING_SCORE = 200.0


class LunarLanderScore(object):
    def __init__(self):
        self.done = False
        self.observation = env.reset()
        self.score = 0.0
        self.steps = 0

    def createNew(self):
        return LunarLanderScore()

    def getMaxThreads(self):
        return 1

    def flushBetween(self):
        return True

    def generateInput(self):
        j_inputs = None
        if not self.done:
            env.render()
            j_inputs = gateway.new_array(gateway.jvm.float, INPUT_SIZE)
            for x in range(0, INPUT_SIZE):
                j_inputs[x] = self.observation[x]

        return j_inputs

    def acceptOutput(self, output):
        action = numpy.empty(2, dtype=numpy.float64)
        for i in range(0, OUTPUT_SIZE):
            val = numpy.float64(output[i])
            action[i] = (val * 2.0) - 1.0

        self.observation, reward, self.done, _ = env.step(action)
        self.score += float(reward)
        self.steps += 1

    def getScore(self):
        return self.score

    def isWinner(self):
        return self.score >= WINNING_SCORE

    class Java:
        implements = ['plu.teamtwo.rtm.neat.ScoringFunction']


if __name__ == '__main__':
    gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())

    gateway.entry_point.init(gateway.jvm.plu.teamtwo.rtm.genome.graph.GraphEncodingBuilder().inputs(INPUT_SIZE).outputs(OUTPUT_SIZE))
    controller = gateway.entry_point.getController()
    controller.createFirstGeneration()

    env = gym.make('LunarLanderContinuous-v2')

    for _ in range(0, 500):
        found_winner = controller.assesGeneration(LunarLanderScore())
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
