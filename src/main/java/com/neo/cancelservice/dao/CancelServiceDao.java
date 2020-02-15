package com.neo.cancelservice.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.neo.utils.ExtractException;
import com.neo.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.internal.OracleTypes;

@Repository
@Transactional
public class CancelServiceDao {
	
	@Autowired
	private HikariDataSource ds;

	private final Logger logger = LoggerFactory.getLogger(CancelServiceDao.class);
	
	public CancelServiceDao() {
		
	}
	
	public int cancelServiceFilter(String proc, String modules, String special) {
		int k=0;
		long startTime = System.nanoTime();
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = ds.getConnection();
			cs = conn.prepareCall(proc);
			cs.setString(1, modules);
			cs.setString(2, special);
			cs.registerOutParameter(3, OracleTypes.INTEGER);
			cs.execute();
			k = cs.getInt(1);
			logger.info("Total time move extend_data_indat => extend_data_queue : {}", Utils.estimateTime(startTime));
		} catch (Exception e) {
			logger.error("move data Extend_data_inday => exten_data_queue Exception = "
					+ ExtractException.exceptionToString(e));
			return k;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					logger.error(
							"CallableStatement.close move data Exception=" + ExtractException.exceptionToString(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection.close move data Exception=" + ExtractException.exceptionToString(e));
				}
			}
		}
		return k;
	}
}
