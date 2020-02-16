package com.neo.cancelservice.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			k = cs.getInt(3);
			logger.info("Total time filter data  : {}", Utils.estimateTime(startTime));
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
	
	public List<Map<String,String>> getListCancelService(String proc, String module, int numberRecord){
		long startTime = System.nanoTime();
		List<Map<String, String>> list = new ArrayList<>();
		Connection conn = null;
		CallableStatement ps = null;
		ResultSet rs = null;
		
		try {
			Date date = new Date();
			conn = ds.getConnection();
			ps = conn.prepareCall(proc);
			ps.setString(1, module);
			ps.setLong(2, date.getTime());
			ps.setInt(3, numberRecord);
			ps.registerOutParameter(4, OracleTypes.CURSOR);
			ps.execute();
			rs = (ResultSet) ps.getObject(4);
			ResultSetMetaData metaData = rs.getMetaData();
			int rowCount = metaData.getColumnCount();
			List<String> colNames = new ArrayList<>();
			for (int i = 1; i <= rowCount; i++) {
				colNames.add(metaData.getColumnName(i));
			}
			while (rs.next()) {
				Map<String, String> map = new HashMap<>();
				for (int i = 1; i <= rowCount; i++) {
					if (rs.getObject(i) == null) {
						map.put(colNames.get(i - 1).toUpperCase(), "");
					} else
						map.put(colNames.get(i - 1).toUpperCase(), rs.getObject(i).toString().toUpperCase());
				}
				list.add(map);
			}
			logger.info("Total time get list cancel service: {}", Utils.estimateTime(startTime));
		}catch(Exception e) {
			logger.info("excption {} ExtendDao get list cancel service", ExtractException.exceptionToString(e));
		}
		 finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error("resultSet.close Exception=" + ExtractException.exceptionToString(e));
					}
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						logger.error("preparedStatement.close Exception = " + ExtractException.exceptionToString(e));
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						logger.error("connection.close Exception = " + ExtractException.exceptionToString(e));
					}
				}
			}
			return list;
	}
	
	public int redistributeModuleDisconnect(String proc, String moduleNameactive, String modulenNameNonActive,String table) {
		long startTime = System.nanoTime();
		int k=0;
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = ds.getConnection();
			cs = conn.prepareCall(proc);
			cs.setString(1, moduleNameactive);
			cs.setString(2, modulenNameNonActive);
			cs.setString(3, ",");
			cs.setString(4, table);
			cs.registerOutParameter(5, OracleTypes.INTEGER);
			cs.execute();
			k =  cs.getInt(5);
			logger.info("Total time distribute module disconnect : {}", Utils.estimateTime(startTime));
		} catch (Exception e) {
			logger.error("distribute module disconnect Exception=" + ExtractException.exceptionToString(e));
			return k;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("CallableStatement.close redistributeModuleDisconnect  Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection.close distribute module disconnect Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
		}
		return k;
	}
	
	public int redistributeRecordOld(String proc, String moduleNameactive,String table, Long time) {
		long startTime = System.nanoTime();
		int k=0;
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = ds.getConnection();
			cs = conn.prepareCall(proc);
			cs.setString(1, moduleNameactive);
			cs.setString(2, ",");
			cs.setString(3, table);
			cs.setLong(4, time);
			cs.registerOutParameter(5, OracleTypes.INTEGER);
			cs.execute();
			k =  cs.getInt(5);
			logger.info("Total time distribute module disconnect : {}", Utils.estimateTime(startTime));
		} catch (Exception e) {
			logger.error("distribute module disconnect Exception=" + ExtractException.exceptionToString(e));
			return k;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("CallableStatement.close redistributeModuleDisconnect  Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection.close distribute module disconnect Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
		}
		return k;
	}
}
