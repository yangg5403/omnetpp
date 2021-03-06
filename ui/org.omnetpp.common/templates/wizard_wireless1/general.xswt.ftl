<?xml version="1.0" encoding="UTF-8"?>
<xswt xmlns:x="http://sweet_swt.sf.net/xswt">

  <import xmlns="http://sweet_swt.sf.net/xswt">
    <package name="java.lang"/>
    <package name="org.eclipse.swt.widgets" />
    <package name="org.eclipse.swt.graphics" />
    <package name="org.eclipse.swt.layout" />
    <package name="org.omnetpp.common.wizard.support" />
    <package name="org.omnetpp.ned.editor.wizards.support" />
    <package name="org.omnetpp.cdt.wizard.support" />
  </import>
  <layout x:class="GridLayout" numColumns="2"/>
  <x:children>
    <label text="Number of nodes:"/>
    <spinner x:id="numNodes" x:style="BORDER" maximum="10000" increment="10"/>
    <label text="Node placement:"/>
    <combo x:id="placement" x:style="BORDER|READ_ONLY">
        <add x:p0="grid"/>
        <add x:p0="random"/>
        <add x:p0="sinewave"/>
        <add x:p0="unpinned"/>
    </combo>
    <label text="Routing protocol:"/>
    <combo x:id="routingProtocol" x:style="BORDER">
        <add x:p0="AODV"/>
        <add x:p0="DSDV"/>
        <add x:p0="DYMO"/>
    </combo>
  </x:children>
</xswt>
