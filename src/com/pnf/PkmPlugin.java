package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

public class PkmPlugin extends AbstractUnitIdentifier{
	public static ILogger LOG = GlobalLog.getLogger(PkmPlugin.class);
	private static int[] PKM_MAGIC = {(byte) 0x50, (byte) 0x4B, (byte) 0x4D, (byte) 0x20, (byte) 0x31, (byte) 0x30};
	private static String ID = "pkm_plugin";
	
	public PkmPlugin() {
		super(ID, 0);
	}

	public boolean identify(byte[] stream, IUnit unit) {
		return checkBytes(stream, 0, PKM_MAGIC);
	}
	
	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
        super.initialize(parent, pm);
        /** Add any necessary property definitions here **/
    }

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit unit) {
		return null;
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
