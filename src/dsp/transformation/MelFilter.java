package dsp.transformation;

import main.Constants;

/**
 * 
 * @author igorletso
 * @author niktrk
 * 
 */
public class MelFilter implements Transformation {

	private static final int MEL_MIN_FREQUENCY = 0;// 0-350
	private static final int MEL_MAX_FREQUENCY = 4000;// 3500-4000

	private int numberOfTriangularFilters;
	private double[] centerOfFrequency;

	public MelFilter(int numberOfTriangularFilters) {
		super();
		this.numberOfTriangularFilters = numberOfTriangularFilters;
		calculateCentersOfFrequency();
	}

	private void calculateCentersOfFrequency() {
		double delta = (fromHzToMel(MEL_MAX_FREQUENCY) - fromHzToMel(MEL_MIN_FREQUENCY)) / (numberOfTriangularFilters + 1);
		centerOfFrequency = new double[numberOfTriangularFilters + 2];
		centerOfFrequency[0] = MEL_MIN_FREQUENCY;
		for (int i = 1; i <= numberOfTriangularFilters; i++) {
			centerOfFrequency[i] = fromMelToHz(delta * i + fromHzToMel(MEL_MIN_FREQUENCY));
		}
		centerOfFrequency[numberOfTriangularFilters + 1] = MEL_MAX_FREQUENCY;
	}

	private static double fromMelToHz(double mels) {
		return 700 * (Math.pow(10, mels / 2595) - 1);
	}

	private static double fromHzToMel(double hzs) {
		return 2595 * Math.log10(1 + hzs / 700);
	}

	private double getTriangleFilterCoefficient(int triangleIndex, int frequencyIndex) {
		double frequency = frequencyIndex * Constants.FREQUENCY_STEP;
		double previousCenterOfFrequency = centerOfFrequency[triangleIndex - 1];
		double currentCenterOfFrequency = centerOfFrequency[triangleIndex];
		double nextCenterOfFrequency = centerOfFrequency[triangleIndex + 1];
		if (previousCenterOfFrequency <= frequency && frequency < currentCenterOfFrequency) {

			return (frequency - previousCenterOfFrequency) / (currentCenterOfFrequency - previousCenterOfFrequency);

		} else if (currentCenterOfFrequency <= frequency && frequency < nextCenterOfFrequency) {

			return (frequency - nextCenterOfFrequency) / (currentCenterOfFrequency - nextCenterOfFrequency);

		} else {
			// frequency < previousCenterOfFrequency OR
			// frequency >= nextCenterOfFrequency
			return 0d;
		}
	}

	@Override
	public double[] transform(double[] input) {
		double[] result = new double[numberOfTriangularFilters];

		for (int i = 1; i <= numberOfTriangularFilters; i++) {
			double melCoefficient = 0d;
			for (int j = 0; j < input.length / 2; j++) {
				melCoefficient += input[j] * getTriangleFilterCoefficient(i, j);
			}
			if (melCoefficient < 1e-9) {
				result[i - 1] = 0d;
			} else {
				result[i - 1] = Math.log(melCoefficient);
			}
		}

		return result;
	}
}
