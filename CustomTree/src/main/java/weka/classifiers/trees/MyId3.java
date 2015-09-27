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

/**
 <!-- globalinfo-start -->
 * An implementation of ID3 Algorithm using JAVA and WEKA <br/>
 * <br/>
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Quinlan1986,
 *    author = {R. Quinlan},
 *    journal = {Machine Learning},
 *    number = {1},
 *    pages = {81-106},
 *    title = {Induction of decision trees},
 *    volume = {1},
 *    year = {1986}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Gunakan -D untuk debug mode <p/>
 * 
 * 
 <!-- options-end -->
 *
 * @author Andre Susanto (andre@andresusanto.info)
 * @version $Revision: 1 $ 
 */
 
 
public class MyId3 extends Classifier implements TechnicalInformationHandler, Sourcable {

	static final long serialVersionUID = -2693678647096322561L;
	private MyId3[] m_Successors;
	private Attribute m_Attribute;
	private double m_ClassValue;
	private double[] m_Distribution;
	private Attribute m_ClassAttribute;

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

	// Build the classifier
	public void buildClassifier(Instances data) throws Exception {

		// test the data with capabilities
		getCapabilities().testWithFail(data);

		// handle missing classes (remove them)
		data = new Instances(data);
		data.deleteWithMissingClass();

		makeTree(data);
	}

	
	private void makeTree(Instances data) throws Exception {
		// Check if no instances have reached this node.
		if (data.numInstances() == 0) {
			m_Attribute = null;
			m_ClassValue = Instance.missingValue();
			m_Distribution = new double[data.numClasses()];
			return;
		}

		// Compute attribute with maximum information gain.
		double[] infoGains = new double[data.numAttributes()];
		Enumeration attEnum = data.enumerateAttributes();
		
		while (attEnum.hasMoreElements()) {
			Attribute att = (Attribute) attEnum.nextElement();
			infoGains[att.index()] = computeInfoGain(data, att);
		}
		
		m_Attribute = data.attribute(Utils.maxIndex(infoGains));

		// Make leaf if information gain is zero. 
		// Otherwise create successors.
		if (Utils.eq(infoGains[m_Attribute.index()], 0)) {
			m_Attribute = null;
			m_Distribution = new double[data.numClasses()];
			Enumeration instEnum = data.enumerateInstances();
			while (instEnum.hasMoreElements()) {
				Instance inst = (Instance) instEnum.nextElement();
				m_Distribution[(int) inst.classValue()]++;
			}
			Utils.normalize(m_Distribution);
			m_ClassValue = Utils.maxIndex(m_Distribution);
			m_ClassAttribute = data.classAttribute();
		} else {
			Instances[] splitData = splitData(data, m_Attribute);
			m_Successors = new MyId3[m_Attribute.numValues()];
			for (int j = 0; j < m_Attribute.numValues(); j++) {
				m_Successors[j] = new MyId3();
				m_Successors[j].makeTree(splitData[j]);
			}
		}
	}


	public double classifyInstance(Instance instance) throws NoSupportForMissingValuesException {
		if (instance.hasMissingValue()) throw new NoSupportForMissingValuesException("MyId3: this classifier can't handle missing value.");
		
		if (m_Attribute == null) {
			return m_ClassValue;
		} else {
			return m_Successors[(int) instance.value(m_Attribute)].
			classifyInstance(instance);
		}
	}

	public double[] distributionForInstance(Instance instance) throws NoSupportForMissingValuesException {
		if (instance.hasMissingValue()) throw new NoSupportForMissingValuesException("MyId3: this classifier can't handle missing value.");
		if (m_Attribute == null) {
			return m_Distribution;
		} else { 
			return m_Successors[(int) instance.value(m_Attribute)].
			distributionForInstance(instance);
		}
	}

  
	public String toString() {

		if ((m_Distribution == null) && (m_Successors == null)) {
			return "MyId3: No model built yet.";
		}
		
		return "MyId3\n\n" + toString(0);
	}

	private double computeInfoGain(Instances data, Attribute att) throws Exception {
		double infoGain = computeEntropy(data);
		Instances[] splitData = splitData(data, att);
		
		for (int j = 0; j < att.numValues(); j++) {
			if (splitData[j].numInstances() > 0) {
				infoGain -= ((double) splitData[j].numInstances() / (double) data.numInstances()) *
				computeEntropy(splitData[j]);
			}
		}
		return infoGain;
	}

	private double computeEntropy(Instances data) throws Exception {
		double [] classCounts = new double[data.numClasses()];
		Enumeration instEnum = data.enumerateInstances();
		
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			classCounts[(int) inst.classValue()]++;
		}
		
		double entropy = 0;
		for (int j = 0; j < data.numClasses(); j++) {
			if (classCounts[j] > 0) {
				entropy -= classCounts[j] * Utils.log2(classCounts[j]);
			}
		}
		entropy /= (double) data.numInstances();
		return entropy + Utils.log2(data.numInstances());
	}

	private Instances[] splitData(Instances data, Attribute att) {
		Instances[] splitData = new Instances[att.numValues()];
		for (int j = 0; j < att.numValues(); j++) {
			splitData[j] = new Instances(data, data.numInstances());
		}
		Enumeration instEnum = data.enumerateInstances();
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			splitData[(int) inst.value(att)].add(inst);
		}
		for (int i = 0; i < splitData.length; i++) {
			splitData[i].compactify();
		}
		return splitData;
	}


	private String toString(int level) {
		StringBuffer text = new StringBuffer();

		if (m_Attribute == null) {
			if (Instance.isMissingValue(m_ClassValue)) {
				text.append(": null");
			} else {
				text.append(": " + m_ClassAttribute.value((int) m_ClassValue));
			} 
		} else {
			for (int j = 0; j < m_Attribute.numValues(); j++) {
				text.append("\n");
				for (int i = 0; i < level; i++) {
					text.append("|  ");
				}
				text.append(m_Attribute.name() + " = " + m_Attribute.value(j));
				text.append(m_Successors[j].toString(level + 1));
			}
		}
		return text.toString();
	}

	protected int toSource(int id, StringBuffer buffer) throws Exception {
		int                 result;
		int                 i;
		int                 newID;
		StringBuffer[]      subBuffers;

		buffer.append("\n");
		buffer.append("  protected static double node" + id + "(Object[] i) {\n");

		// leaf?
		if (m_Attribute == null) {
			result = id;
			if (Double.isNaN(m_ClassValue)) {
				buffer.append("    return Double.NaN;");
			} else {
				buffer.append("    return " + m_ClassValue + ";");
			}
			if (m_ClassAttribute != null) {
				buffer.append(" // " + m_ClassAttribute.value((int) m_ClassValue));
			}
			buffer.append("\n");
			buffer.append("  }\n");
		} else {
			buffer.append("    checkMissing(i, " + m_Attribute.index() + ");\n\n");
			buffer.append("    // " + m_Attribute.name() + "\n");
		  
			// subtree calls
			subBuffers = new StringBuffer[m_Attribute.numValues()];
			newID = id;
			for (i = 0; i < m_Attribute.numValues(); i++) {
				newID++;

				buffer.append("    ");
				if (i > 0) {
					buffer.append("else ");
				}
				
				buffer.append("if (((String) i[" + m_Attribute.index() 
				+ "]).equals(\"" + m_Attribute.value(i) + "\"))\n");
				buffer.append("      return node" + newID + "(i);\n");

				subBuffers[i] = new StringBuffer();
				newID = m_Successors[i].toSource(newID, subBuffers[i]);
			}
			
			buffer.append("    else\n");
			buffer.append("      throw new IllegalArgumentException(\"Value '\" + i["
			  + m_Attribute.index() + "] + \"' is not allowed!\");\n");
			buffer.append("  }\n");

			// output subtree code
			for (i = 0; i < m_Attribute.numValues(); i++) {
				buffer.append(subBuffers[i].toString());
			}
			subBuffers = null;
		  
			result = newID;
		}

		return result;
	}
  
	public String toSource(String className) throws Exception {
		StringBuffer        result;
		int                 id;

		result = new StringBuffer();

		result.append("class " + className + " {\n");
		result.append("  private static void checkMissing(Object[] i, int index) {\n");
		result.append("    if (i[index] == null)\n");
		result.append("      throw new IllegalArgumentException(\"Null values "
		+ "are not allowed!\");\n");
		result.append("  }\n\n");
		result.append("  public static double classify(Object[] i) {\n");
		id = 0;
		result.append("    return node" + id + "(i);\n");
		result.append("  }\n");
		toSource(id, result);
		result.append("}\n");

		return result.toString();
	}
  
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 6404 $");
	}
}
