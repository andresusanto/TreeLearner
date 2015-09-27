// MYID3
// 13512028 - Andre Susanto

package weka.classifiers.trees;

import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NoSupportForMissingValuesException;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.Enumeration;

 
public class MyId3 extends Classifier implements TechnicalInformationHandler, Sourcable {
	
	///////////////////////////////////////// HELPER FUNCTIONS ///////////////////////////////////////////////////////
	
	private double logarithm(double base, double x){
		return Math.log(x) / Math.log(base);
	}
	
	private String printTree(int cur_level) {
		StringBuffer result = new StringBuffer();

		if (attr == null) {
			if (Instance.isMissingValue(classVal)) result.append(" ==> MISSING");
			else result.append(" ==> " + classAtt.value((int) classVal));
		} else {
			for (int j = 0; j < attr.numValues(); j++) {
				result.append("\n");
				for (int i = 0; i < cur_level; i++) result.append("  -> ");
				result.append("if (" + attr.name() + " = )" + attr.value(j));
				result.append(childs[j].printTree(cur_level + 1));
			}
		}
		return result.toString();
	}
	
	private int maxInfoGain(double[] doubles) {
		double cur_max = doubles[0];
		int cur_index = 0;

		for (int i = 1; i < doubles.length; i++) {
			if ((doubles[i] > cur_max)) {
				cur_index = i;
				cur_max = doubles[i];
			}
		}

		return cur_index;
	}
	
	private void normalize(double[] doubles) {
		double sum = 0;
		for (double d : doubles) sum += d;
		normalize(doubles, sum);
	}

	private void normalize(double[] doubles, double sum) {
		if (Double.isNaN(sum)) throw new IllegalArgumentException("Array contains NaN");
		if (sum == 0) throw new IllegalArgumentException("Array empty");
		for (int i = 0; i < doubles.length; i++) doubles[i] /= sum;
	}
	
	private double calcGAIN(Instances data, Attribute att) throws Exception {
		double curGAIN = calcENTRO(data);
		Instances[] removeFromInstance = removeFromInstance(data, att);
		
		for (int j = 0; j < att.numValues(); j++) {
			if (removeFromInstance[j].numInstances() > 0) curGAIN -= ((double) removeFromInstance[j].numInstances() / (double) data.numInstances()) * calcENTRO(removeFromInstance[j]);
			
		}
		return curGAIN;
	}

	private double calcENTRO(Instances data) throws Exception {
		double [] classCounts = new double[data.numClasses()];
		Enumeration instEnum = data.enumerateInstances();
		
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			classCounts[(int) inst.classValue()]++;
		}
		
		double entropy = 0;
		for (int j = 0; j < data.numClasses(); j++) {
			if (classCounts[j] > 0) {
				entropy -= classCounts[j] * logarithm(2, classCounts[j]);
			}
		}
		entropy /= (double) data.numInstances();
		return entropy + logarithm(2, data.numInstances());
	}

	private Instances[] removeFromInstance(Instances data, Attribute att) {
		Instances[] removeFromInstance = new Instances[att.numValues()];
		for (int j = 0; j < att.numValues(); j++) {
			removeFromInstance[j] = new Instances(data, data.numInstances());
		}
		Enumeration instEnum = data.enumerateInstances();
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			removeFromInstance[(int) inst.value(att)].add(inst);
		}
		for (int i = 0; i < removeFromInstance.length; i++) {
			removeFromInstance[i].compactify();
		}
		return removeFromInstance;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	///////////////////////////////////////// RELATED TO WEKA GUI ////////////////////////////////////////////////////
	
	
	// So that the GUI can give description about this classifier
	public String globalInfo() {
		return  "An implementation of ID3 Algorithm using Java. For more information see: \n\n" + getTechnicalInformation().toString();
	}

	// Technical information about this classifier
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "R. Quinlan");
		result.setValue(Field.YEAR, "1986");
		result.setValue(Field.TITLE, "Induction of decision trees");
		result.setValue(Field.JOURNAL, "Machine Learning");
		result.setValue(Field.VOLUME, "1");
		result.setValue(Field.NUMBER, "1");
		result.setValue(Field.PAGES, "81-106");

		return result;
	}

	// Capabillities of this classifier
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// this classifier can handle only nominal attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);

		// this classifier can handle missing class values
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(0);

		return result;
	}

	public String toString() {
		if ((dist == null) && (childs == null)) return "MyId3: No model built yet.";
		return "MyId3\n\n" + printTree(0);
	}
	
	public String toSource(String className) throws Exception {
		return "";
	}
	
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1 $");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/////////////////////////////////// RELATED TO BUILDING MODEL ////////////////////////////////////////////////
	
	// Build the classifier
	public void buildClassifier(Instances data) throws Exception {
		// test the data with capabilities
		getCapabilities().testWithFail(data);

		// handle missing classes (remove them)
		data = new Instances(data);
		data.deleteWithMissingClass();

		constructTree(data);
	}

	
	private void constructTree(Instances data) throws Exception {
		// Check if no instances have reached this node.
		if (data.numInstances() == 0) {
			attr = null;
			classVal = Instance.missingValue();
			dist = new double[data.numClasses()];
			return;
		}

		// Compute attribute with maximum information gain.
		double[] informationGain = new double[data.numAttributes()];
		Enumeration attEnum = data.enumerateAttributes();
		
		while (attEnum.hasMoreElements()) {
			Attribute att = (Attribute) attEnum.nextElement();
			informationGain[att.index()] = calcGAIN(data, att);
		}
		
		attr = data.attribute(maxInfoGain(informationGain));

		// Make leaf if information gain is zero. 
		// Otherwise create successors.
		if (informationGain[attr.index()] == 0) {
			attr = null;
			dist = new double[data.numClasses()];
			Enumeration instEnum = data.enumerateInstances();
			while (instEnum.hasMoreElements()) {
				Instance inst = (Instance) instEnum.nextElement();
				dist[(int) inst.classValue()]++;
			}
			Utils.normalize(dist);
			classVal = maxInfoGain(dist);
			classAtt = data.classAttribute();
		} else {
			Instances[] removeFromInstance = removeFromInstance(data, attr);
			childs = new MyId3[attr.numValues()];
			for (int j = 0; j < attr.numValues(); j++) {
				childs[j] = new MyId3();
				childs[j].constructTree(removeFromInstance[j]);
			}
		}
	}
	
	
	/////////////////////////////////////////// CLASSIFY INSTANCES ////////////////////////////////////////////////////////

	public double classifyInstance(Instance instance) throws NoSupportForMissingValuesException {
		if (instance.hasMissingValue()) throw new NoSupportForMissingValuesException("MyId3: this classifier can't handle missing value.");
		
		if (attr == null) return classVal;
		else
			return childs[(int) instance.value(attr)].classifyInstance(instance);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Attribute classAtt;
	private Attribute attr;
	
	private double classVal;
	private double[] dist;
	
	private MyId3[] childs;

}
