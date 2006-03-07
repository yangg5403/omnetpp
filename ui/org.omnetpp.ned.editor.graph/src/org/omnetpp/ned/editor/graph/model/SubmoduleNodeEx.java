package org.omnetpp.ned.editor.graph.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.omnetpp.ned.editor.graph.properties.SubmoduleDisplayPropertySource;
import org.omnetpp.ned2.model.NEDElement;
import org.omnetpp.ned2.model.pojo.SubmoduleNode;

public class SubmoduleNodeEx extends SubmoduleNode implements INedModule {

	// srcConns contains all connections where the sourcemodule is this module
	protected List<ConnectionNodeEx> srcConns = new ArrayList<ConnectionNodeEx>();
	// destConns contains all connections where the destmodule is this module
	protected List<ConnectionNodeEx> destConns = new ArrayList<ConnectionNodeEx>();
	
	public SubmoduleNodeEx() {
        init();
	}

	public SubmoduleNodeEx(NEDElement parent) {
		super(parent);
        init();
	}

    private void init() {
        // TODO correctly handle the initial naming for new nodes (name most be unique)
        setName("unnamed");
        setType("node");
    }
    
	public String getDisplayString() {
		return NedElementExUtil.getDisplayString(this); 
	}
	
	public void setDisplayString(String dspString) {
		NedElementExUtil.setDisplayString(this, dspString);
	}

	protected CompoundModuleNodeEx getCompoundModule() {
		return (CompoundModuleNodeEx)(getParent().getParent());
	}

	public List<ConnectionNodeEx> getSrcConnections() {
		return srcConns;
	}
	
	public List<ConnectionNodeEx> getDestConnections() {
		return destConns;
	}

	public void addSrcConnection(ConnectionNodeEx conn) {
		assert(!srcConns.contains(conn));
		srcConns.add(conn);
		// TODO add property change event (connection added)
	}

	public void removeSrcConnection(ConnectionNodeEx conn) {
		assert(srcConns.contains(conn));
		srcConns.remove(conn);
		// TODO add property change event (connection removed)
	}

	public void addDestConnection(ConnectionNodeEx conn) {
		assert(!destConns.contains(conn));
		destConns.add(conn);
		// TODO add property change event (connection added)
	}

	public void removeDestConnection(ConnectionNodeEx conn) {
		assert(destConns.contains(conn));
		destConns.remove(conn);
		// TODO add property change event (connection removed)
	}

	public Point getLocation() {
		SubmoduleDisplayPropertySource dps = new SubmoduleDisplayPropertySource(getDisplayString());

		return new Point (dps.getIntPropertyDef(SubmoduleDisplayPropertySource.PROP_X, 0),
						  dps.getIntPropertyDef(SubmoduleDisplayPropertySource.PROP_Y, 0));
	}

	public void setLocation(Point location) {
		SubmoduleDisplayPropertySource dps = new SubmoduleDisplayPropertySource(getDisplayString());
        
        if (location == null) {
            // if location is not specified, remove the constraint from thedisplay string
            dps.resetPropertyValue(SubmoduleDisplayPropertySource.PROP_X);
        } else {
            // if location is specifie set the location (p) constraint in the displaystring 
            dps.setPropertyValue(SubmoduleDisplayPropertySource.PROP_X, location.x);
            dps.setPropertyValue(SubmoduleDisplayPropertySource.PROP_Y, location.y);
        }
		setDisplayString(dps.getValue());
	}

	public Dimension getSize() {
		SubmoduleDisplayPropertySource dps = new SubmoduleDisplayPropertySource(getDisplayString());

		return new Dimension(dps.getIntPropertyDef(SubmoduleDisplayPropertySource.PROP_W, 0),
						     dps.getIntPropertyDef(SubmoduleDisplayPropertySource.PROP_H, 0));
	}

	public void setSize(Dimension size) {
		SubmoduleDisplayPropertySource dps = new SubmoduleDisplayPropertySource(getDisplayString());
        
		if (size == null || size.width <0 || size.height<0) {
            // if the size is unspecified, remove the size constraint from the model
            dps.resetPropertyValue(SubmoduleDisplayPropertySource.PROP_W);
        } else {
            // if the size is specified, add a size constraint to the model
            dps.setPropertyValue(SubmoduleDisplayPropertySource.PROP_W, size.width);
		    dps.setPropertyValue(SubmoduleDisplayPropertySource.PROP_H, size.height);
        }
		setDisplayString(dps.getValue());
	}

}
