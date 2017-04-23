package plu.teamtwo.rtm.neat;

/**
 * Created by hannah on 4/20/17.
 */
public interface Genome {

    public abstract void getANN();

    public abstract void cross();

    public abstract void mutation();

    public abstract void serialization();
}
