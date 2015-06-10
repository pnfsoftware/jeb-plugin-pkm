package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.properties.impl.PropertyTypeString;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

public class PkmPlugin extends AbstractUnitIdentifier{
	private static final int[] PKM_MAGIC = {(byte) 0x50, (byte) 0x4B, (byte) 0x4D, (byte) 0x20, (byte) 0x31, (byte) 0x30};
	private static final String ID = "pkm_plugin";
	
	public static final ILogger LOG = GlobalLog.getLogger(PkmPlugin.class);
	public static final String ANDROID_TOOLS_DIR = "AndroidPlatformToolsDirectory";
	public static final String PROP_NAME = ".parsers." + ID + "." + ANDROID_TOOLS_DIR;
	
	public PkmPlugin() {
		super(ID, 0);
	}

	public boolean identify(byte[] stream, IUnit unit) {
		return checkBytes(stream, 0, PKM_MAGIC);
	}
	
	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
        super.initialize(parent, pm);
        
        // We need to use the android tools, so require it as an input before working with PKM files
        PropertyTypeString pts = PropertyTypeString.create();
        
        if(getPropertyDefinitionManager().getDefinition(ANDROID_TOOLS_DIR) == null)
        	getPropertyDefinitionManager().addDefinition(ANDROID_TOOLS_DIR, pts);
    }

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit parent) {
		PkmUnit unit = new PkmUnit(name, data, processor, parent, pdm);
		return unit;
	}

	@Override
	public PluginInformation getPluginInformation() {
		return new PluginInformation("PKM Plugin", "", "1.0", "PNF Software");
	}

	@Override
	public IUnit reload(IBinaryFrames data, IUnitProcessor processor, IUnit unit) {
		return null;
	}
}
