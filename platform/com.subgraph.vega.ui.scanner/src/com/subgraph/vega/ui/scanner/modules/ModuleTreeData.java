package com.subgraph.vega.ui.scanner.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.subgraph.vega.api.scanner.modules.IEnableableModule;
import com.subgraph.vega.api.scanner.modules.IScannerModule;
import com.subgraph.vega.api.scanner.modules.IScannerModuleRegistry;

public class ModuleTreeData {

	private final Map<String, List<IScannerModule>> categoryToModules;
	
	ModuleTreeData(IScannerModuleRegistry registry) {
		categoryToModules = new HashMap<String, List<IScannerModule>>();
		populateModuleMap(registry.getAllModules(false));
	}

	private void populateModuleMap(List<IScannerModule> modules) {
		for(IScannerModule m : modules) 
			getModuleListForCategory(m.getModuleCategoryName()).add(m);
	}

	public List<IScannerModule> getModuleListForCategory(String categoryName) {
		if(!categoryToModules.containsKey(categoryName))
			categoryToModules.put(categoryName, new ArrayList<IScannerModule>());
		return categoryToModules.get(categoryName);
	}

	void setEnableStateForCategory(String category, boolean state) {
		for(IScannerModule m: getModuleListForCategory(category)) {
			if(m instanceof IEnableableModule)
				((IEnableableModule)m).setEnabled(state);
		}
	}

	boolean someEnabledInCategory(String category) {
		for(IScannerModule m: getModuleListForCategory(category)) {
			if(m instanceof IEnableableModule) 
				if(((IEnableableModule)m).isEnabled())
					return true;
		}
		return false;
	}

	boolean allEnabledInCategory(String category) {
		for(IScannerModule m: getModuleListForCategory(category)) {
			if(m instanceof IEnableableModule) 
				if(!((IEnableableModule)m).isEnabled())
					return false;
		}		
		return true;
	}

	List<String> getAllCategories() {
		return new ArrayList<String>(categoryToModules.keySet());
	}

}
