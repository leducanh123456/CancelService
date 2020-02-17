package com.neo.cancelservice.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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
			logger.error("filter data Exception : "
					+ ExtractException.exceptionToString(e));
			return k;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					logger.error(
							"CallableStatement.close filter data Exception=" + ExtractException.exceptionToString(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection.close filter data Exception=" + ExtractException.exceptionToString(e));
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
						logger.error("resultSet.close get list Exception=" + ExtractException.exceptionToString(e));
					}
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						logger.error("preparedStatement.close get list Exception = " + ExtractException.exceptionToString(e));
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						logger.error("connection.close get list Exception = " + ExtractException.exceptionToString(e));
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
			logger.info("Total time distribute old record : {}", Utils.estimateTime(startTime));
		} catch (Exception e) {
			logger.error("distribute old record Exception=" + ExtractException.exceptionToString(e));
			return k;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("CallableStatement.close distribute old record  Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection.close distribute old record Exception="
							+ ExtractException.exceptionToString(e));
				}
			}
		}
		return k;
	}
	

	public void upDateBatchRenewalRetry(String proc,String queryUpdate, LinkedBlockingQueue<Map<String, String>> queueRenewalRetry,
			String batchSize) {
		
		//move sang log
		//delete trong list
		// update trong log
		
		
		//lấy danh sách để thực hiện cập nhật vào DB
		long startTime = System.nanoTime();
		int size = Integer.parseInt(batchSize);
		int tmp = 0;
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		while (!queueRenewalRetry.isEmpty()) {
			try {
				list.add(queueRenewalRetry.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("TakeQueue exception =" + ExtractException.exceptionToString(e));
			}
			tmp++;
			if (tmp == size)
				break;
		}
		

		if (!list.isEmpty()) {

			CallableStatement cs = null;
			Connection connection = null;
			PreparedStatement stmt = null;
			try {
				connection = ds.getConnection();
				connection.setAutoCommit(false);
				cs = connection.prepareCall(proc);
				stmt = connection.prepareStatement(queryUpdate);
				// move sang log và xóa trong list
				StringBuffer listId = new StringBuffer();
				
				for (Map<String, String> map : list) {
					listId.append(map.get("ID"));
					listId.append(",");
				} // end for
				listId.delete(listId.length()-1, listId.length());
				cs.setString(1, listId.toString());
				cs.registerOutParameter(2, OracleTypes.INTEGER);
				cs.execute();
				
				cs.getInt(2);
				
				for (Map<String, String> map : list) {
					stmt.setLong(1, Long.parseLong(map.get("SPEND_TIME")));
					stmt.setNString(2, map.get("SESSION_ID"));
					stmt.setNString(3, map.get("STATUS_MESSAGE"));
					stmt.setNString(4, map.get("STATUS"));
					stmt.setNString(5, map.get("SERVICE_CMD"));
					stmt.setLong(6, Long.parseLong(map.get("ID")));
					stmt.addBatch();
				} // end for

				stmt.executeBatch();
				stmt.clearParameters();
				connection.commit();
				logger.info("Total time update batch extend : {}", Utils.estimateTime(startTime));
			} catch (Exception e) {
				logger.error("update batch extend Exception=" + ExtractException.exceptionToString(e));
				try {
					connection.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					logger.error("connection.rollback update batch extend Exception="
							+ ExtractException.exceptionToString(e1));
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logger.error(
								"prepareStatement.close extend Exception=" + ExtractException.exceptionToString(e));
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logger.error("connection.close extend Exception=" + ExtractException.exceptionToString(e));
					}
				}
			}

		}
	}

	public Map<String, Map<String, String>> getServiceCmds(String proc) {
		long startTime = System.nanoTime();
		Map<String, Map<String, String>> serviceCmds = new HashMap<String, Map<String, String>>();
		Connection connection = null;
		CallableStatement preparedStatement = null;
		try {
			connection = ds.getConnection();
			preparedStatement = connection.prepareCall(proc);
			preparedStatement.registerOutParameter(1, OracleTypes.CURSOR);
			preparedStatement.execute();
			ResultSet resultSet = (ResultSet) preparedStatement.getObject(1);
			ResultSetMetaData metaData = resultSet.getMetaData();
			int rowCount = metaData.getColumnCount();

			List<String> colNames = new ArrayList<>();
			for (int i = 1; i <= rowCount; i++) {
				colNames.add(metaData.getColumnName(i));
			}
			while (resultSet.next()) {
				Map<String, String> map = new HashMap<>();
				for (int i = 1; i <= rowCount; i++) {
					if (resultSet.getString(i) == null) {
						map.put(colNames.get(i - 1), "");
					} else
						map.put(colNames.get(i - 1), resultSet.getString(i));
				}

				StringBuilder s = new StringBuilder("");
				s.append(map.get("SERVICE_ID"));
				s.append("_");
				s.append(map.get("PKG_ID"));
				s.append("_");
				s.append(map.get("ACT_ID"));
				serviceCmds.put(s.toString(), map);
			}
			logger.info("Total time get list service cmd : {}", Utils.estimateTime(startTime));
		} catch (Exception e) {
			logger.error("get list service_cmd=" + ExtractException.exceptionToString(e));
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("preparedStatement.close extend Exception=" + ExtractException.exceptionToString(e));
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("connection.close get service cmd Exception=" + ExtractException.exceptionToString(e));
				}
			}

		}
		return serviceCmds;
	}
}
