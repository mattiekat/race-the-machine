import gym
import numpy
from gym import spaces
from py4j.java_gateway import JavaGateway, CallbackServerParameters

# BipedalWalker-v2
# BipedalWalkerHardcore-v2
# CartPole-v1
# LunarLander-v2
# LunarLanderContinuous-v2
# MountainCar-v0
# MountainCarContinuous-v0
# Pendulum-v0

ENVIROMENT = 'BipedalWalkerHardcore-v2'


class ScoreFunction(object):
    def __init__(self):
        self.done = False
        self.observation = env.reset()
        self.score = 0.0
        self.steps = 0

    def createNew(self):
        return ScoreFunction()

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
        if DISCRETE:
            action = None
            max = -100.0
            for i in range(0, OUTPUT_SIZE):
                if numpy.float64(output[i]) > max:
                    max = numpy.float64(output[i])
                    action = i

        else:
            action = numpy.empty(OUTPUT_SIZE, dtype=numpy.float64)
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

    env = gym.make(ENVIROMENT)
    INPUT_SIZE = env.observation_space.shape[0]
    DISCRETE = isinstance(env.action_space, spaces.Discrete)

    OUTPUT_SIZE = None
    if DISCRETE:
        OUTPUT_SIZE = env.action_space.n
    else:
        OUTPUT_SIZE = env.action_space.shape[0]

    WINNING_SCORE = 1000.0 if env.spec.reward_threshold is None else env.spec.reward_threshold

    print("Inputs: {}, Outputs: {}, Discrete: {}, Winning Score: {}".format(INPUT_SIZE, OUTPUT_SIZE, DISCRETE, WINNING_SCORE))

    gateway.entry_point.init(gateway.jvm.plu.teamtwo.rtm.genome.graph.GraphEncodingBuilder().inputs(INPUT_SIZE).outputs(OUTPUT_SIZE))
    controller = gateway.entry_point.getController()
    controller.createFirstGeneration()

    for _ in range(0, 500):
        found_winner = controller.assesGeneration(ScoreFunction())
        best = controller.getBestIndividual()
        print('Gen {:d}: {:.2f}, {:.1f}'
              .format(controller.getGenerationNum(), controller.getFitness(), best.getFitness()))
        # print('generation')
        if found_winner:
            gson = gateway.jvm.com.google.gson.GsonBuilder().setPrettyPrinting().create()
            print(gson.toJson(best))

        controller.nextGeneration()

    gateway.shutdown()
