import gym
import numpy
from gym import spaces
from py4j.java_gateway import JavaGateway, CallbackServerParameters

ENVIROMENT = 'CarRacing-v0'


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

    def realTimeProcessing(self):
        return True

    def generateInput(self):
        j_inputs = None
        if not self.done:
            env.render()
            j_inputs = gateway.new_array(gateway.jvm.float, INPUTS)
            # for x in range(0, 12):
            #     for y in range(0, 12):
            #         for z in range(0, 3):
            #             j_inputs[x*36 + y*3 + z] = float(self.observation[x*8 + 4][y*8 + 4][z])

        return j_inputs

    def acceptOutput(self, output):
        #action = [(float(output[0]) * 2.0) - 1.0, float(output[1]), float(output[2])]
        action = env.action_space.sample()
        # self.observation, reward, self.done, _ = env.step(action)
        env.step(action)
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
    #INPUT_SIZE = env.observation_space.shape[0]
    #DISCRETE = isinstance(env.action_space, spaces.Discrete)

    DISCRETE = False

    INPUTS = 432
    INPUT_SIZE = gateway.new_array(gateway.jvm.int, 3)
    INPUT_SIZE[0] = INPUT_SIZE[1] = 12
    INPUT_SIZE[2] = 3

    HIDDEN_SIZE = gateway.new_array(gateway.jvm.int, 2)
    HIDDEN_SIZE[0] = HIDDEN_SIZE[1] = 12

    OUTPUT_SIZE = gateway.new_array(gateway.jvm.int, 1)
    OUTPUT_SIZE[0] = 3

    WINNING_SCORE = 1000.0 if env.spec.reward_threshold is None else env.spec.reward_threshold

    #print("Inputs: {}, Outputs: {}, Discrete: {}, Winning Score: {}".format(INPUT_SIZE, OUTPUT_SIZE, DISCRETE, WINNING_SCORE))

    gateway.entry_point.init(gateway.jvm.plu.teamtwo.rtm.genome.graph.MultilayerSubstrateEncodingBuilder()
                             .inputs(INPUT_SIZE).outputs(OUTPUT_SIZE).addLayer(HIDDEN_SIZE))

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
