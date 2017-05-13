import gym
from py4j.java_gateway import JavaGateway, CallbackServerParameters


class CartPoleScore(object):
    def __init__(self):
        self.done = False
        self.observation = env.reset()
        self.score = 0.0
        self.steps = 0

    def createNew(self):
        return CartPoleScore()

    def getMaxThreads(self):
        return 1

    def flushBetween(self):
        return True

    def generateInput(self):
        j_inputs = None
        if not self.done:
            env.render()
            j_inputs = gateway.new_array(gateway.jvm.float, 4)
            for x in range(0, 4):
                j_inputs[x] = self.observation[x]

        return j_inputs

    def acceptOutput(self, output):
        action = 0 if output[0] > output[1] else 1
        self.observation, reward, self.done, _ = env.step(action)
        self.score += reward
        self.steps += 1

    def getScore(self):
        return self.score

    def isWinner(self):
        return self.score >= 500.0

    class Java:
        implements = ['plu.teamtwo.rtm.neat.ScoringFunction']


if __name__ == '__main__':
    gateway = JavaGateway(callback_server_parameters=CallbackServerParameters())

    gateway.entry_point.init(gateway.jvm.plu.teamtwo.rtm.neat.Encoding.DIRECT_ENCODING, 4, 2)
    controller = gateway.entry_point.getController()
    controller.createFirstGeneration()

    env = gym.make('CartPole-v1')

    for _ in range(0, 500):
        found_winner = controller.assesGeneration(CartPoleScore())
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
