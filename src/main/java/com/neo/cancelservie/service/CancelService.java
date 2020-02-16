package com.neo.cancelservie.service;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neo.cancelservice.dao.CancelServiceDao;
import com.neo.module.bo.ModuleBo;

@Service
public class CancelService {
	
	@Autowired
	private CancelServiceDao dao;
	public int cancelServiceFilter(String proc, String modules) {
		
		return dao.cancelServiceFilter(proc, modules, ",");
	}
	
	public List<Map<String, String>> getListCancelService(String proc, String module, int numberRecord){
		
		return dao.getListCancelService(proc, module, numberRecord);
	}
	public  int redistributeModuleDisconnect(String proc,ConcurrentHashMap<ModuleBo, SocketChannel> map,List<ModuleBo> jobsTmp,ModuleBo module, String table) {
		StringBuilder moduleNameactive = new StringBuilder();
		moduleNameactive.append(module.getModuleName());
		moduleNameactive.append(",");
		for (Map.Entry<ModuleBo, SocketChannel> tmp : map.entrySet()) {
			moduleNameactive.append(tmp.getKey().getModuleName());
			moduleNameactive.append(",");
			
		}
		moduleNameactive.delete(moduleNameactive.length()-1, moduleNameactive.length());
		StringBuilder modulenNameNonActive = new StringBuilder();
		for( ModuleBo moduleBo : jobsTmp) {
			modulenNameNonActive.append(moduleBo.getModuleName());
			modulenNameNonActive.append(",");
		}
		modulenNameNonActive.delete(modulenNameNonActive.length()-1, modulenNameNonActive.length());
		return dao.redistributeModuleDisconnect(proc, moduleNameactive.toString(), modulenNameNonActive.toString(), table);
	}

}
