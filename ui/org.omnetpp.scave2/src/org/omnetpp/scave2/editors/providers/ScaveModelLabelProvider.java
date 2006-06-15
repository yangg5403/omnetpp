package org.omnetpp.scave2.editors.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.omnetpp.scave.model.Add;
import org.omnetpp.scave.model.Analysis;
import org.omnetpp.scave.model.Apply;
import org.omnetpp.scave.model.Chart;
import org.omnetpp.scave.model.ChartSheet;
import org.omnetpp.scave.model.ChartSheets;
import org.omnetpp.scave.model.Dataset;
import org.omnetpp.scave.model.Datasets;
import org.omnetpp.scave.model.Deselect;
import org.omnetpp.scave.model.Discard;
import org.omnetpp.scave.model.Except;
import org.omnetpp.scave.model.Group;
import org.omnetpp.scave.model.InputFile;
import org.omnetpp.scave.model.Inputs;
import org.omnetpp.scave.model.Param;
import org.omnetpp.scave.model.Property;
import org.omnetpp.scave.model.Select;
import org.omnetpp.scave.model.SetOperation;

/**
 * Label provider for model objects. We use this instead 
 * of the EMF-generated default label provider.
 *  
 * @author andras
 */
//XXX changing the "based on" property of "Dataset" in the properties view somehow doesn't refresh the label in the editor's tree
public class ScaveModelLabelProvider extends LabelProvider {
	public Image getImage(Object element) {
		return null;  //XXX fall back to EMF-generated code?
	}

	public String getText(Object element) {
		if (element instanceof Analysis) {
			return "Analysis";
		} 
		else if (element instanceof Inputs) {
			return "Inputs";
		} 
		else if (element instanceof InputFile) {
			InputFile o = (InputFile) element;
			return "file "+fallback(o.getName(),"<nothing>");
		} 
		else if (element instanceof ChartSheets) {
			return "Chart Sheets";
		} 
		else if (element instanceof ChartSheet) {
			ChartSheet o = (ChartSheet) element;
			return "chart sheet "+fallback(o.getName(),"<unnamed>");
		} 
		else if (element instanceof Datasets) {
			return "Datasets";
		} 
		else if (element instanceof Dataset) {
			Dataset o = (Dataset) element;
			String res = fallback(o.getType(),"")+" dataset "+fallback(o.getName(),"<unnamed>");
			if (o.getBasedOn()!=null)
				res += " based on dataset "+fallback(o.getBasedOn().getName(),"<unnamed>");
			return res;
		} 
		else if (element instanceof SetOperation) {
			
			// Add, Discard, Select, Deselect, Except
			SetOperation o = (SetOperation) element;
			
			// "select..."
			String res = "";
			if (element instanceof Add)
				res = "add";
			else if (element instanceof Discard)
				res = "discard";
			else if (element instanceof Select)
				res = "select";
			else if (element instanceof Deselect)
				res = "deselect";
			else if (element instanceof Except)
				res = "except";
			else 
				res = "???";

			// "end-to-end delay"
			if (!isEmpty(o.getNamePattern()))
				res += " \""+o.getNamePattern()+"\"";
			else 
				res += " <nothing>";
			if (!isEmpty(o.getModuleNamePattern()))
				res += " of module(s) \""+o.getModuleNamePattern()+"\"";
			if (o.getSourceDataset()!=null)
				res += " from dataset "+fallback(o.getSourceDataset().getName(),"<unnamed>");

			// "from runs where ..."
			String clause = "";
			if (!isEmpty(o.getFilenamePattern()))
				clause += " resultfile ~ \""+o.getFilenamePattern()+"\"";
			if (!isEmpty(o.getExperimentNamePattern()))
				clause += comma(clause)+" experiment ~ \""+o.getExperimentNamePattern()+"\"";
			if (!isEmpty(o.getMeasurementNamePattern()))
				clause += comma(clause)+" measurement ~ \""+o.getMeasurementNamePattern()+"\"";
			if (!isEmpty(o.getReplicationNamePattern()))
				clause += comma(clause)+" replication ~ \""+o.getReplicationNamePattern()+"\"";
			if (!isEmpty(o.getRunNamePattern()))
				clause += comma(clause)+" run ~ \""+o.getRunNamePattern()+"\"";

			// "and..."
			if (!isEmpty(o.getFromRunsWhere()))
				clause += (clause.equals("") ? " " : ", and ")+o.getFromRunsWhere();

			if (!clause.equals(""))
				res += " from runs where"+clause;
			
			return res;
		} 
		else if (element instanceof Apply) {
			Apply o = (Apply) element;
			return "apply "+fallback(o.getOperation(),"<undefined>");
		}
		else if (element instanceof Group) {
			return "group";
		}
		else if (element instanceof Chart) {
			Chart o = (Chart) element;
			String res = "chart "+fallback(o.getName(),"<unnamed>");
			if (o.getContainingSheet()!=null)
				res += " on sheet "+fallback(o.getContainingSheet().getName(),"<unnamed>");
			return res;
		}
		else if (element instanceof Param) {
			Param o = (Param) element;
			return fallback(o.getName(),"<undefined>")+" = "+fallback(o.getValue(),"");
		}
		else if (element instanceof Property) {
			Property o = (Property) element;
			return fallback(o.getName(),"<undefined>")+" = "+fallback(o.getValue(),"");
		}
		return element.toString(); // fallback
	}

	private String fallback(String string, String defaultString) {
		return (string!=null && !string.equals("")) ? string : defaultString; 
	}

	private String comma(String before) {
		return (before!=null && !before.equals("")) ? "," : ""; 
	}

	private boolean isEmpty(String string) {
		return string==null || string.equals(""); 
	}
}
