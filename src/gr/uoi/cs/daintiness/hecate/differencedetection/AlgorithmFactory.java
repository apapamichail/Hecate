package gr.uoi.cs.daintiness.hecate.differencedetection;

public class AlgorithmFactory {

	public DifferencesAlgorithm getAlgorithm(String algorithm){
		if (algorithm.equals("DifferencesAlgorithmSkoulis"))
			return new DifferencesAlgorithmSkoulis();
		return null;
	}
}
