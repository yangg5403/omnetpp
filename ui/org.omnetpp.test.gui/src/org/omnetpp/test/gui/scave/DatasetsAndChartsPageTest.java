package org.omnetpp.test.gui.scave;

import org.eclipse.swt.SWT;
import org.omnetpp.common.ui.GenericTreeNode;
import org.omnetpp.test.gui.access.DatasetsAndChartsPageAccess;

import com.simulcraft.test.gui.access.TreeAccess;

public class DatasetsAndChartsPageTest extends ScaveFileTestCase {
	
	DatasetsAndChartsPageAccess datasetsPage;
	
	@Override
	protected void setUpInternal() throws Exception {
		super.setUpInternal();
		createFiles();
		editor = ScaveEditorUtils.openAnalysisFile(projectName, fileName);
		datasetsPage = editor.ensureDatasetsPageActive();
	}
	
	public void testDatasetsTreeContent() {
		datasetsPage.getDatasetsTree().assertContent(buildDatasetsTreeContent());
	}
	
	public void testOpenPagesFromMenu() {
		testOpenPageFromMenu(".*test-dataset.*", "Dataset.*test-dataset.*");
		testOpenPageFromMenu(".*test-barchart.*", "Chart.*test-barchart.*");
		testOpenPageFromMenu(".*test-linechart.*", "Chart.*test-linechart.*");
		//testOpenPageFromMenu(".*test-histogramchart.*", "Chart.*test-histogramchart.*");
		testOpenPageFromMenu(".*test-scatterchart.*", "Chart.*test-scatterchart.*");
	}
	
	public void testOpenPagesWithDoubleClick() {
		testOpenPageWithDoubleClick(".*test-dataset.*", "Dataset.*test-dataset.*");
		testOpenPageWithDoubleClick(".*test-barchart.*", "Chart.*test-barchart.*");
		testOpenPageWithDoubleClick(".*test-linechart.*", "Chart.*test-linechart.*");
		//testOpenPageWithDoubleClick(".*test-histogramchart.*", "Chart.*test-histogramchart.*");
		testOpenPageWithDoubleClick(".*test-scatterchart.*", "Chart.*test-scatterchart.*");
	}
	
	protected void testOpenPageFromMenu(String nodeLabel, String pageLabel) {
		datasetsPage.getDatasetsTree().findTreeItemByContent(nodeLabel).reveal().chooseFromContextMenu("Open.*");
		editor.assertActivePage(pageLabel);
		editor.closePage(pageLabel);
		editor.assertActivePage("Datasets");
	}
	
	protected void testOpenPageWithDoubleClick(String nodeLabel, String pageLabel) {
		datasetsPage.getDatasetsTree().findTreeItemByContent(nodeLabel).reveal().doubleClick();
		editor.assertActivePage(pageLabel);
		editor.closePage(pageLabel);
		editor.assertActivePage("Datasets");
	}
	
    public void testGroup() {
        TreeAccess tree = datasetsPage.getDatasetsTree();
        tree.findTreeItemByContent("bar chart test-barchart").reveal().click();
        editor.holdDownModifiers(SWT.SHIFT);
        tree.findTreeItemByContent("scatter chart test-scatterchart").reveal().click();
        editor.releaseModifiers(SWT.SHIFT);
        tree.chooseFromContextMenu("Group");
        
        // TODO assert content
        
        editor.executeUndo();
        tree.assertContent(buildDatasetsTreeContent());
    }
	
	
	protected void createFiles() throws Exception {
		createFile(
				fileName,

				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<scave:Analysis xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:scave=\"http://www.omnetpp.org/omnetpp/scave\">\n" +
				"  <inputs>\n" +
				"    <inputs name=\"test-*.vec\"/>\n" +
				"    <inputs name=\"test-*.sca\"/>\n" +
				"  </inputs>\n" +
				"  <datasets>\n" +
				"    <datasets name=\"test-dataset\">\n" +
				"      <items xsi:type=\"scave:Add\" filterPattern=\"\" type=\"VECTOR\"/>\n" +
				"      <items xsi:type=\"scave:Discard\" filterPattern=\"\"/>\n" +
				"      <items xsi:type=\"scave:Group\">\n" +
				"        <items xsi:type=\"scave:Apply\" operation=\"mean\"/>\n" +
				"        <items xsi:type=\"scave:Compute\"/>\n" +
				"        <items xsi:type=\"scave:LineChart\" name=\"test-linechart\" lineNameFormat=\"\"/>\n" +
				"      </items>\n" +
				"      <items xsi:type=\"scave:BarChart\" name=\"test-barchart\"/>\n" +
				"      <items xsi:type=\"scave:HistogramChart\" name=\"test-histogramchart\"/>\n" +
				"      <items xsi:type=\"scave:ScatterChart\" name=\"test-scatterchart\" xDataPattern=\"module(module-1) AND name(&quot;mean(vector-1)&quot;)\"/>\n" +
				"    </datasets>\n" +
				"  </datasets>\n" +
				"  <chartSheets>\n" +
				"    <chartSheets name=\"default\" charts=\"//@datasets/@datasets.0/@items.3 //@datasets/@datasets.0/@items.2/@items.2 //@datasets/@datasets.0/@items.4 //@datasets/@datasets.0/@items.5\"/>\n" +
				"    <chartSheets name=\"test-chartsheet\"/>\n" +
				"  </chartSheets>\n" +
				"</scave:Analysis>\n");
		
		for (int runNumber = 1; runNumber <= 2; ++runNumber) {
			createFile(
					String.format("test-%d.sca", runNumber),
					createScalarFileContent(runNumber));
			createFile(
					String.format("test-%d.vec", runNumber),
					createVectorFileContent(runNumber));
		}
	}
	
	protected GenericTreeNode buildDatasetsTreeContent() {
		return	n("dataset test-dataset",
					n("add vectors: all"),
					n("discard scalars: all"),
					n("group",
						n("apply mean"),
						n("compute <undefined>"),
						n("line chart test-linechart")),
					n("bar chart test-barchart"),
					n("histogram chart test-histogramchart"),
					n("scatter chart test-scatterchart"));
	}
}
