package com.neo.cancelservie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neo.cancelservice.dao.CancelServiceDao;

@Service
public class CancelService {
	
	@Autowired
	private CancelServiceDao dao;
	public int cancelServiceFilter(String proc, String modules, String special) {
		
		return dao.cancelServiceFilter(proc, modules, special);
	}

}
