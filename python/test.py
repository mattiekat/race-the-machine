import gym

if __name__ == '__main__':
    env = gym.make('CarRacing-v0')
    for i_episode in range(10000):
        observation = env.reset()
        done = False
        while not done:
            env.render()
            action = env.action_space.sample()
            observation, reward, done, info = env.step(action)