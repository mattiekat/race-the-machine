package plu.teamtwo.rtm.neat;

import java.io.*;
import java.security.InvalidParameterException;

import static plu.teamtwo.rtm.neat.GAController.readFromStream;
import static plu.teamtwo.rtm.neat.GAController.writeToStream;

public class Archiver {
    /**
     * Save a GAController to a JSON archive. This should be used before creating the next generation to prevent a
     * loss of information as the generation is overwritten.
     *
     * @param controller Controller to save to a file.
     * @param path       Location to save the data to.
     * @return True if it was successfully saved, false otherwise.
     */
    public static boolean saveToFile(GAController controller, String path) {
        if(controller == null || path == null) return false;
        File dir = new File(path);
        if(!dir.exists() || !dir.isDirectory()) {
            System.err.println("The directory '" + path + "' does not exist or is not a directory.");
            return false;
        }
        File file = new File(String.format("%sG%5d.json", dir.getPath() + File.pathSeparator, controller.getGenerationNum()));
        try {
            writeToStream(controller, new FileOutputStream(file, false));
        } catch(IOException e) {
            System.err.println("Could not save NEAT Controller: " + e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Read a GAController from a JSON file. This will create a new GAController with the information of the
     * most recent generation in the JSON file. This expects to receive a directory with the generations saved into it.
     * <p>
     * Note: auto save will need to be re-enabled if it is desired in the new instance.
     *
     * @param path Directory to read from.
     * @return A GAController initialized to the latest generation in the JSON archive.
     */
    public static GAController readFromFile(String path) throws IOException {
        //verify we were given a directory.
        if(path == null) return null;
        File file = new File(path);
        if(!file.exists())
            throw new InvalidParameterException("Directory does not exist.");
        if(!file.isDirectory())
            throw new InvalidParameterException("Path is not to a directory.");
        File[] files = file.listFiles( //filter out non-files and files which are not a generation
                (File dir, String name) -> dir.isFile() && name.matches("G[0-9]{5}\\.json")
        );

        if(files == null || files.length <= 0)
            throw new FileNotFoundException("Could not find a valid generation file.");

        //find the file with the greatest number (it is most recent generation)
        file = files[0];
        for(int x = 1; x < files.length; ++x) {
            if(files[x].getName().compareTo(file.getName()) > 0)
                file = files[x];
        }
        return readFromStream(new FileInputStream(file));
    }
}
