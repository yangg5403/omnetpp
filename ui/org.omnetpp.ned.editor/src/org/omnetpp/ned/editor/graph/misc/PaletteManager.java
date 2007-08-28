package org.omnetpp.ned.editor.graph.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;

import org.omnetpp.common.displaymodel.IDisplayString;
import org.omnetpp.common.image.ImageFactory;
import org.omnetpp.common.util.DelayedJob;
import org.omnetpp.common.util.StringUtils;
import org.omnetpp.ned.core.NEDResourcesPlugin;
import org.omnetpp.ned.editor.graph.GraphicalNedEditor;
import org.omnetpp.ned.model.INEDElement;
import org.omnetpp.ned.model.ex.ChannelElementEx;
import org.omnetpp.ned.model.ex.ChannelInterfaceElementEx;
import org.omnetpp.ned.model.ex.CompoundModuleElementEx;
import org.omnetpp.ned.model.ex.ConnectionElementEx;
import org.omnetpp.ned.model.ex.ModuleInterfaceElementEx;
import org.omnetpp.ned.model.ex.NEDElementUtilEx;
import org.omnetpp.ned.model.ex.SimpleModuleElementEx;
import org.omnetpp.ned.model.ex.SubmoduleElementEx;
import org.omnetpp.ned.model.interfaces.IHasDisplayString;
import org.omnetpp.ned.model.interfaces.IHasName;
import org.omnetpp.ned.model.interfaces.IModuleTypeElement;
import org.omnetpp.ned.model.interfaces.INEDTypeInfo;
import org.omnetpp.ned.model.interfaces.INedTypeElement;
import org.omnetpp.ned.model.notification.INEDChangeListener;
import org.omnetpp.ned.model.notification.NEDModelEvent;
import org.omnetpp.ned.model.pojo.ChannelInterfaceElement;
import org.omnetpp.ned.model.pojo.ModuleInterfaceElement;
import org.omnetpp.ned.model.pojo.PropertyElement;

/**
 * Responsible for managing palette entries and keeping them in sync with
 * the components in NEDResources plugin
 *
 * @author rhornig
 */
public class PaletteManager implements INEDChangeListener {
    private static final String NBSP = "\u00A0";
    private static final String GROUP_PROPERTY = "group";

    protected GraphicalNedEditor hostingEditor;
    protected PaletteRoot nedPalette;
    private PaletteContainer toolsContainer;
    protected PaletteContainer channelsStack;
    protected PaletteDrawer typesContainer;
    protected PaletteDrawer defaultContainer;

    protected Map<String, ToolEntry> currentEntries = new HashMap<String, ToolEntry>();
    protected Map<String, PaletteDrawer> currentContainers = new HashMap<String, PaletteDrawer>();

    protected DelayedJob paletteUpdaterJob = new DelayedJob(200) {
        public void run() {
            synchronizePalette();
        }
    };

    public PaletteManager(GraphicalNedEditor hostingEditor) {
        super();
        this.hostingEditor = hostingEditor;
        nedPalette = new PaletteRoot();
        channelsStack = new PaletteStack("Connections", "Connect modules using this tool",ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CONNECTION));
        toolsContainer = createTools();
        typesContainer = new PaletteDrawer("Types", ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_FOLDER));
        typesContainer.setInitialState(PaletteDrawer.INITIAL_STATE_PINNED_OPEN);
        defaultContainer = new PaletteDrawer("Submodules", ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_FOLDER));

        synchronizePalette();
    }

    public PaletteRoot getRootPalette() {
        return nedPalette;
    }

    /* (non-Javadoc)
     * @see org.omnetpp.ned.model.notification.INEDChangeListener#modelChanged(org.omnetpp.ned.model.notification.NEDModelEvent)
     * Called when a change occurred in the module which forces palette redraw
     * NOTE: this notification can arrive in any thread (even in a background thread)
     */
    public void modelChanged(NEDModelEvent event) {
        refresh();
    }

    public void refresh() {
        paletteUpdaterJob.restartTimer();
    }

    /**
     * Builds the palette (all drawers)
     */
    public void synchronizePalette() {
        nedPalette.getChildren().clear();
        nedPalette.add(toolsContainer);
        nedPalette.add(typesContainer);
        nedPalette.add(defaultContainer);
        channelsStack.getChildren().clear();
        typesContainer.getChildren().clear();
        defaultContainer.getChildren().clear();
        for(PaletteContainer container : currentContainers.values())
            container.getChildren().clear();

        Map<String, PaletteEntry> newEntries = createPaletteModel();
        for(String id: newEntries.keySet()) {
            getContainerFor(id).add(newEntries.get(id));
        }

        // TODO sort the containers by name
        for(PaletteContainer container : currentContainers.values())
            nedPalette.add(container);
    }

    /**
     * The container belonging to this ID
     */
    public PaletteContainer getContainerFor(String id) {
        if (!id.contains("."))
            return defaultContainer;
        String group = StringUtils.substringBefore(id, ".");
        if ("!mru".equals(group))
            return defaultContainer;
        if ("!connections".equals(group))
            return channelsStack;
        if ("!types".equals(group))
            return typesContainer;

        PaletteDrawer cont = currentContainers.get(group);
        if (cont == null) {
            cont = new PaletteDrawer(group, ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_FOLDER));
            cont.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
            currentContainers.put(group, cont);
        }

        return cont;
    }

    /**
     * Builds a drawer containing basic tools like selection connection etc.
     */
    private PaletteContainer createTools() {
        PaletteGroup controlGroup = new PaletteGroup("Tools");

        ToolEntry tool = new PanningSelectionToolEntry("Selector","Select module(s)");
        tool.setToolClass(NedSelectionTool.class);
        controlGroup.add(tool);
        getRootPalette().setDefaultEntry(tool);

        controlGroup.add(channelsStack);
        return controlGroup;
    }

    private Map<String, PaletteEntry> createPaletteModel() {
        Map<String, PaletteEntry> entries = new LinkedHashMap<String, PaletteEntry>();
        entries.putAll(createChannelsStackEntries());
        entries.putAll(createTypesEntries());
        // and local types
        IFileEditorInput feinput = ((IFileEditorInput)hostingEditor.getEditorInput());
        if (feinput != null) {
            entries.putAll(createInnerTypes(feinput.getFile()));
        }
        entries.put("separator", new PaletteSeparator());
        entries.putAll(createSubmodules());

        return entries;
    }

    /**
     * Iterates over all top level types in a NED file and gathers all NEDTypes from all components.
     * Returns a Container containing all types in this file or NULL if there are no inner types
     * defined in this file's top level modules.
     */
    private static Map<String, ToolEntry> createInnerTypes(IFile file) {
        Map<String, ToolEntry> entries = new LinkedHashMap<String, ToolEntry>();

        for (INEDElement topLevelElement : NEDResourcesPlugin.getNEDResources().getNEDFileModel(file)) {
            // skip non NedType elements
            if (!(topLevelElement instanceof INedTypeElement))
                continue;

            // check all inner types for this element
            for (INedTypeElement typeElement : ((INedTypeElement)topLevelElement).getNEDTypeInfo().getInnerTypes().values()) {
                // skip if it cannot be used to create a submodule (ie. should be Module a module type)
                if (!(typeElement instanceof IModuleTypeElement) && !(typeElement instanceof ModuleInterfaceElement))
                    continue;

                boolean isInterface = typeElement instanceof ModuleInterfaceElement;
                String key = "!mru."+typeElement.getName();

                // set the default images for the palette entry
                ImageDescriptor imageDescNorm = ImageFactory.getDescriptor(ImageFactory.DEFAULT,"vs",null,0);
                ImageDescriptor imageDescLarge = ImageFactory.getDescriptor(ImageFactory.DEFAULT,"s",null,0);
                if (typeElement instanceof IHasDisplayString) {
                    IDisplayString dps = ((IHasDisplayString)typeElement).getDisplayString();
                    String iid = dps.getAsString(IDisplayString.Prop.IMAGE);
                    if (StringUtils.isNotEmpty(iid)) {
                        imageDescNorm = ImageFactory.getDescriptor(iid,"vs",null,0);
                        imageDescLarge = ImageFactory.getDescriptor(iid,"s",null,0);
                        key = key+":"+iid;
                    }
                }

                // create the tool entry (if we are currently dropping an interface, we should use the IF type for the like parameter
                CombinedTemplateCreationEntry toolEntry = new CombinedTemplateCreationEntry(
                        typeElement.getName() + (isInterface ? NBSP+"(interface)" : ""), StringUtils.makeBriefDocu(typeElement.getComment(), 300),
                        new ModelFactory(SubmoduleElementEx.getStaticTagName(),StringUtils.toInstanceName(typeElement.getName()), typeElement.getName(), isInterface),
                        imageDescNorm, imageDescLarge );

                entries.put(key, toolEntry);
            }
        }

        return entries;
    }

    /**
     * Creates several submodule drawers using currently parsed types,
     * and using the GROUP property as the drawer name.
     */
    private static Map<String, ToolEntry> createSubmodules() {
        Map<String, ToolEntry> entries = new LinkedHashMap<String, ToolEntry>();

        // get all the possible type names in alphabetical order
        List<String> typeNames
                = new ArrayList<String>(NEDResourcesPlugin.getNEDResources().getModuleNames());
        typeNames.addAll(NEDResourcesPlugin.getNEDResources().getModuleInterfaceNames());
        Collections.sort(typeNames, StringUtils.dictionaryComparator);

        for (String name : typeNames) {
            INEDTypeInfo comp = NEDResourcesPlugin.getNEDResources().getComponent(name);
            INEDElement typeElement = comp.getNEDElement();
            boolean isInterface = typeElement instanceof ModuleInterfaceElement;
            String key = name;

            // skip this type if it is a top level network
            if (typeElement instanceof CompoundModuleElementEx &&
                    ((CompoundModuleElementEx)typeElement).getIsNetwork()) {
                continue;
            }

            // set the default images for the palette entry
            ImageDescriptor imageDescNorm = ImageFactory.getDescriptor(ImageFactory.DEFAULT,"vs",null,0);
            ImageDescriptor imageDescLarge = ImageFactory.getDescriptor(ImageFactory.DEFAULT,"s",null,0);
            if (typeElement instanceof IHasDisplayString) {
                IDisplayString dps = ((IHasDisplayString)typeElement).getDisplayString();
                String iid = dps.getAsString(IDisplayString.Prop.IMAGE);
                if (StringUtils.isNotEmpty(iid)) {
                    imageDescNorm = ImageFactory.getDescriptor(iid,"vs",null,0);
                    imageDescLarge = ImageFactory.getDescriptor(iid,"s",null,0);
                    key += ":"+iid;
                }
            }

            // determine which palette group it belongs to or put it to the default
            PropertyElement property = comp.getProperties().get(GROUP_PROPERTY);
            String group = property == null ? ""
                            : NEDElementUtilEx.getPropertyValue(property);
            if (StringUtils.isNotEmpty(group))
                key = group+"."+key;

            // create the tool entry (if we are currently dropping an interface, we should use the IF type for the like parameter
            CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
                    name + (isInterface ? NBSP+"(interface)" : ""), StringUtils.makeBriefDocu(typeElement.getComment(), 300),
                    new ModelFactory(SubmoduleElementEx.getStaticTagName(),StringUtils.toInstanceName(name), name, isInterface),
                    imageDescNorm, imageDescLarge );

            entries.put(key, combined);
        }

        return entries;
    }

    private static Map<String, ToolEntry> createChannelsStackEntries() {
        Map<String, ToolEntry> entries = new LinkedHashMap<String, ToolEntry>();

        ConnectionCreationToolEntry defConnTool = new ConnectionCreationToolEntry(
                "Connection",
                "Create connections between submodules, or submodule and parent module",
                new ModelFactory(ConnectionElementEx.getStaticTagName()),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CONNECTION),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CONNECTION)
        );
        // sets the required connection tool
        defConnTool.setToolClass(NedConnectionCreationTool.class);
        entries.put("!connections.connection", defConnTool);

        // connection selection
        MarqueeToolEntry marquee = new MarqueeToolEntry("Connection"+NBSP+"selector","Drag out an area to select connections in it");
        marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
                new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
        entries.put("!connections.marquee", marquee);

        // get all the possible type names in alphabetical order
        List<String> channelNames
                = new ArrayList<String>(NEDResourcesPlugin.getNEDResources().getChannelNames());
        channelNames.addAll(NEDResourcesPlugin.getNEDResources().getChannelInterfaceNames());
        Collections.sort(channelNames, StringUtils.dictionaryComparator);

        for (String name : channelNames) {
            INEDTypeInfo comp = NEDResourcesPlugin.getNEDResources().getComponent(name);
            INEDElement modelElement = comp.getNEDElement();
            boolean isInterface = modelElement instanceof ChannelInterfaceElement;

            ConnectionCreationToolEntry tool
               = new ConnectionCreationToolEntry(
                    name + (isInterface ? NBSP+"(interface)" : ""),
                    StringUtils.makeBriefDocu(comp.getNEDElement().getComment(), 300),
                    new ModelFactory(ConnectionElementEx.getStaticTagName(),name.toLowerCase(), name, isInterface),
                    ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CONNECTION),
                    ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CONNECTION)
            );
            // sets the required connection tool
            tool.setToolClass(NedConnectionCreationTool.class);
            entries.put("!connections."+name, tool);
        }
        return entries;
    }

    /**
     * Builds a tool entry list containing base top level NED components like simple, module, channel etc.
     */
    private static Map<String, ToolEntry> createTypesEntries() {
        Map<String, ToolEntry> entries = new LinkedHashMap<String, ToolEntry>();

        CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
                "Simple"+NBSP+"module",
                "Create a simple module type",
                new ModelFactory(SimpleModuleElementEx.getStaticTagName(), IHasName.DEFAULT_TYPE_NAME),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_SIMPLEMODULE),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_SIMPLEMODULE)
        );
        entries.put("!types.simple", entry);

        entry = new CombinedTemplateCreationEntry(
                "Compound"+NBSP+"Module",
                "Create a compound module type that may contain submodules",
                new ModelFactory(CompoundModuleElementEx.getStaticTagName(), IHasName.DEFAULT_TYPE_NAME),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_COMPOUNDMODULE),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_COMPOUNDMODULE)
        );
        entries.put("!types.compound", entry);

        entry = new CombinedTemplateCreationEntry(
                "Channel",
                "Create a channel type",
                new ModelFactory(ChannelElementEx.getStaticTagName(), IHasName.DEFAULT_TYPE_NAME),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CHANNEL),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CHANNEL)
        );
        entries.put("!types.channel", entry);

        entry = new CombinedTemplateCreationEntry(
        		"Module"+NBSP+"Interface",
        		"Create a module interface type",
        		new ModelFactory(ModuleInterfaceElementEx.getStaticTagName(), IHasName.DEFAULT_TYPE_NAME),
        		ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_INTERFACE),
        		ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_INTERFACE)
        );
        entries.put("!types.moduleinterface", entry);

        entry = new CombinedTemplateCreationEntry(
                "Channel"+NBSP+"Interface",
                "Create a channel interface type",
                new ModelFactory(ChannelInterfaceElementEx.getStaticTagName(), IHasName.DEFAULT_TYPE_NAME),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CHANNELINTERFACE),
                ImageFactory.getDescriptor(ImageFactory.MODEL_IMAGE_CHANNELINTERFACE)
        );
        entries.put("!types.channelinterface", entry);

        return entries;
    }
}
