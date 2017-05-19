package plu.teamtwo.rtm.neural;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Represents a substrate network which has only a few outputs at definable coordinates.
 */
public class SubstrateNetwork implements NeuralNetwork {
    /// Defines the dimensions of each layer, e.g. d[0] = [2, 3] would define an input of 2 by 3 (output is final layer)
    ///  this also defines the mapping of the input arrays to the first substrate and so on.
    private final int[][] layers;

    /// Defines weights between substrates (e.g. weights[0] defines a weight matrix between input and next substrate).
    ///  Stored as weights[layer][output][input]
    private final float[][][] weights;

    /// Product of each layer's dimensions
    private final transient int[] layerSizes;

    /// Activation function used to process inputs with before calculating.
    private final ActivationFunction inputFunction;
    /// Activation function to use for output nodes.
    private final ActivationFunction outputFunction;
    /// Activation function to use for hidden nodes on the substrate.
    private final ActivationFunction hiddenFunction;

    private final boolean useGPU;


    /**
     * Create a new substrate network with the information provided by a builder.
     */
    SubstrateNetwork(SubstrateNetworkBuilder builder) {
        layers = builder.layers;
        weights = builder.weights;

        inputFunction  = builder.inputFunction;
        outputFunction = builder.outputFunction;
        hiddenFunction = builder.hiddenFunction;
        useGPU = builder.useGPU;

        if(layers.length < 2)
            throw new InvalidParameterException("Must at minimum have an input and output layer.");
        if(weights.length != layers.length - 1)
            throw new InvalidParameterException("Invalid number of weights for the layers.");

        layerSizes = calculateLayerSizes(layers);

        for(int layer = 0; layer < weights.length; ++layer) {
            if(weights[layer].length != layerSizes[layer + 1])
                throw new InvalidParameterException("Weights must have exactly 1 value for every input and output combination between layers.");
            for(int out = 0; out < weights[layer].length; ++out)
                if(weights[layer][out].length != layerSizes[layer])
                    throw new InvalidParameterException("Weights must have exactly 1 value for every input and output combination between layers.");
        }
    }


    /**
     * Calculate the size of all layers. This calculates the product of all the dimensions maximum value in a layer.
     * The results of this allow for calculating the needed length of a single-dimension to rpresent the whole
     * multi-dimensional space.
     * <p>
     * Think of the dimensions less as physical dimensions, and instead as the dimensions of a square or a cube, since
     * the layer-space is finite.
     *
     * @param layers The dimensions of each layer.
     * @return The maximum size of each layer given the dimensions for it.
     */
    public static int[] calculateLayerSizes(int[][] layers) {
        int[] layerSizes = new int[layers.length];
        for(int i = 0; i < layers.length; ++i) {
            //find product of all dimensions
            int size = 1;

            if(layers[i].length < 1)
                throw new InvalidParameterException("A layer must have at least 1 dimension");

            for(int d : layers[i]) {
                if(d <= 0) throw new InvalidParameterException("Invalid dimension size");
                size *= d;
            }
            layerSizes[i] = size;
        }

        return layerSizes;
    }


    /**
     * Calculate the outputs of the neural network given the inputs. Note individual implementations are responsible for
     * mapping the inputs/outputs to n-dimensional space if necessary.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Output of the network.
     */
    @Override
    public float[] calculate(float... inputs) {
        if(inputs.length != layerSizes[0])
            throw new InvalidParameterException("Invalid number of inputs.");

//        if(useGPU)
//            return gpuCalculate(inputs);
        return cpuCalculate(inputs);
    }


    /**
     * Steps values through the neural network by processing from the final nodes to the initial nodes. This could be
     * used with real-time applications where direct input-output pairing are not so important as temporal
     * comprehension. Note individual implementations are responsible for mapping the inputs/outputs to n-dimensional
     * space if necessary.
     * <p>
     * Flush should not be called between calls to step.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Array of values from the output neurons.
     */
    @Override
    public float[] step(float... inputs) {
        return calculate(inputs); //for now just call calculate since we don't store values within the network
    }


    /**
     * Empty the network of all stored values. This should be called between tests.
     */
    @Override
    public void flush() {

    }


    /**
     * Determines if the neural network has recurrent cycles in it.
     *
     * @return True if it is a recurrent neural network.
     */
    @Override
    public boolean isRecurrent() {
        return false;
    }


    /**
     * Get the number of inputs the network expects to receive.
     *
     * @return Number of expected inputs.
     */
    @Override
    public int inputs() {
        return layerSizes[0];
    }


    /**
     * Get the number of outputs the network will produced when calculate or step is called.
     *
     * @return Number of outputs produced by the network.
     */
    @Override
    public int outputs() {
        return layerSizes[layerSizes.length - 1];
    }


    private float[] cpuCalculate(float[] inputs) {
        if(inputFunction != ActivationFunction.LINEAR)
            //would do same thing without if statement, except it would take longer
            for(int i = 0; i < inputs.length; ++i)
                inputs[i] = inputFunction.calculate(inputs[i]);

        float[] last = inputs;
        float[] outputs = null;

        //for each layer, calculate the value of the next one given the inputs and weights of the inputs
        // layer is the current input; don't run last layer, it is output
        for(int layer = 0; layer < (layers.length - 1); ++layer) {
            final float[][] layerWeights = weights[layer];
            outputs = new float[layerSizes[layer + 1]];

            //for all the outputs, calculate the value based on all inputs and associated weights
            for(int out = 0; out < layerSizes[layer + 1]; ++out) {
                float sum = 0;

                //dot(input, weights[layer][out])
                for(int in = 0; in < layerSizes[layer]; ++in)
                    sum += last[in] * layerWeights[out][in];

                outputs[out] = (layer == layers.length - 2) ?
                                       outputFunction.calculate(sum) :
                                       hiddenFunction.calculate(sum);
            }

            last = outputs;
        }

        return outputs;
    }


// TODO: switch to setting all the weights up front rather than trying to pass them each time a calculation happens. Basically set the weights stored on the GPU in the constructor.
//    private static final String CL_OUTPUT_CALCULATOR =
//            "kernel void dot_product(global const float* inputs, global const float* weights," +
//                                    "global float* output, int n) {" +
//
//                "int gid = get_global_id(0);" +
//                "float sum = 0.0f;" +
//                "n--;" +
//                "for(;n >= 0;--n) {" +
//                    "sum += inputs[n] * weights[n];" +
//                "}" +
//                "output[gid] = sum;" +
//            "}";
//
//    private float[] gpuCalculate(float[] initialInputs) {
//        // CONSTRUCT INPUT AND OUTPUT ARRAYS //
//        float[] inputArray, outputArray, weightArray;
//        Pointer inputPointer, outputPointer, weightPointer;
//        long maxLayerSize = 0;
//        for(int s : layerSizes)
//            maxLayerSize = maxLayerSize > s ? maxLayerSize : s;
//
//        inputArray = Arrays.copyOf(initialInputs, (int)maxLayerSize);
//        weightArray = new float[(int)maxLayerSize];
//        outputArray = new float[(int)maxLayerSize];
//
//        inputPointer = Pointer.to(inputArray);
//        weightPointer = Pointer.to(weightArray);
//        outputPointer = Pointer.to(outputArray);
//
//        // SETUP OPENCL //
//        cl_context context;
//        cl_command_queue commandQueue;
//        cl_program program;
//        cl_kernel kernel;
//        cl_mem buffA, buffB, buffC;
//        {
//            final int PLATFORM_INDEX = 0;
//            final int DEVICE_INDEX = 0;
//            final long DEVICE_TYPE = CL_DEVICE_TYPE_GPU;
//
//            int numPlatformsArray[] = new int[1];
//            clGetPlatformIDs(0, null, numPlatformsArray);
//            int numPlatforms = numPlatformsArray[0];
//
//            cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
//            clGetPlatformIDs(platforms.length, platforms, null);
//            cl_platform_id platform = platforms[PLATFORM_INDEX];
//
//            cl_context_properties contextProperties = new cl_context_properties();
//            contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
//
//            int numDevicesArray[] = new int[1];
//            clGetDeviceIDs(platform, DEVICE_TYPE, 0, null, numDevicesArray);
//            int numDevices = numDevicesArray[0];
//
//            cl_device_id devices[] = new cl_device_id[numDevices];
//            clGetDeviceIDs(platform, DEVICE_TYPE, numDevices, devices, null);
//            cl_device_id device = devices[DEVICE_INDEX];
//
//            context = clCreateContextFromType(contextProperties, DEVICE_TYPE, null, null, null);
//            commandQueue = clCreateCommandQueueWithProperties(context, device, new cl_queue_properties(), null);
//
//            //TODO: use max transition size for bufA
//            buffA   = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * maxLayerSize * maxLayerSize, null, null);
//            buffB   = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * maxLayerSize, null, null);
//            buffC   = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * maxLayerSize, null, null);
//
//            program = clCreateProgramWithSource(context, 1, new String[]{CL_OUTPUT_CALCULATOR}, null, null);
//            clBuildProgram(program, 0, null, null, null, null);
//            kernel = clCreateKernel(program, "substrateOutputCalculator", null);
//        }
//
//        // CALCULATE OUTPUTS //
//        long global_work_size[] = new long[1];
//        long local_work_size[] = new long[]{1};
//
//        cl_event[] writeEvents = new cl_event[(int)maxLayerSize];
//        cl_event[] computeEvents = new cl_event[1];
//        for(int layer = 0; layer < layers.length - 1; ++layer) {
//            final long inputLayerSize = layerSizes[layer];
//            final long outputLayerSize = layerSizes[layer + 1];
//
//            for(long out = 0; out < layerSizes[layer+1]; ++out) {
//                clEnqueueWriteBuffer(commandQueue, buffA, false, out * inputLayerSize,
//                        Sizeof.cl_float * inputLayerSize, Pointer.to(weights[layer][(int)out]),
//                        1, computeEvents, writeEvents[(int)out]);
//            }
//
//            global_work_size[0] = layerSizes[layer+1];
//
//            clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(buffA));
//            if(layer % 2 == 0) { //swap which one we consider the input and output to prevent making copies
//                clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(buffB));
//                clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(buffC));
//            } else {
//                clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(buffC));
//                clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(buffB));
//            }
//            clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{layerSizes[layer]}));
//
//            clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
//                    local_work_size, 1, new cl_event[]{last}, last);
//        }
//
//        // CLEANUP OPENCL //
//        clReleaseMemObject(buffA);
//        clReleaseMemObject(buffB);
//        clReleaseMemObject(buffC);
//        clReleaseKernel(kernel);
//        clReleaseProgram(program);
//        clReleaseCommandQueue(commandQueue);
//        clReleaseContext(context);
//        return null;
//    }
}
