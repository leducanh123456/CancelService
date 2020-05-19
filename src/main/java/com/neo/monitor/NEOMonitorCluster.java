package com.neo.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.module.bo.ModuleBo;
import com.neo.module.service.ModuleService;

public class NEOMonitorCluster {

	@Autowired
	@Qualifier("listModule")
	private List<ModuleBo> jobs;

	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;

	/**
	 * lưu trữ những modul client đang kết nối đến
	 */
	@Autowired
	@Qualifier("mapJobSocket")
	private ConcurrentHashMap<ModuleBo, SocketChannel> map;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	@Qualifier("schedulerFactory")
	private SchedulerFactoryBean scheduler;
	
	@Autowired
	@Qualifier("filterData")
	private CronTriggerFactoryBean filterData;

	@Autowired
	@Qualifier("jobFilterData")
	private JobDetailFactoryBean jobFilterData;
	
	@Autowired
	@Qualifier("reDistributeOldRecord")
	private CronTriggerFactoryBean reDistributeOldRecord;

	@Autowired
	@Qualifier("jobReDistributeOldRecord")
	private JobDetailFactoryBean jobReDistributeOldRecord;

	@Autowired
	@Qualifier("retry")
	private ConcurrentHashMap<ModuleBo, SocketChannel> retry;

	@Autowired
	@Qualifier("thisModule")
	private ModuleBo moduleBo;// sử dụng một bean để lưu trữ lại thông tin của bản thân job này
	
	@Autowired
	private CancelService service;

	@Autowired
	private ApplicationContext context;// sử dụng để trong trường hợp không tạo được db thì tắt job

	private final Logger logger = LoggerFactory.getLogger(NEOMonitorCluster.class);

	public void run() throws IOException {
		
		boolean retryModule = true;

		while (retryModule) {
			try {
				jobs = moduleService.getAllModule();
				retryModule = false;
			} catch (Exception e) {
				logger.info("Do not connect DB ======> do not load module");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
		
		String serverName = pro.getString("module.name").trim();

		ModuleBo server = null;
		for (ModuleBo moduleBo : jobs) {
			if (serverName.trim().equals(moduleBo.getModuleName().trim())) {
				server = moduleBo;
				this.moduleBo.setId(moduleBo.getId());
				this.moduleBo.setIp(moduleBo.getIp());
				this.moduleBo.setIsMaster(moduleBo.getIsMaster());
				this.moduleBo.setModuleGroup(moduleBo.getModuleGroup());
				this.moduleBo.setModuleName(moduleBo.getModuleName());
				this.moduleBo.setPort(moduleBo.getPort());
				this.moduleBo.setState(moduleBo.getState());
				this.moduleBo.setStartDate(moduleBo.getStartDate());
				break;
			}
		}
		if (server == null) {
			logger.info("Shut Down module : {} because module do not exist in data base, NEOMonitorCluster 116",
					pro.getString("module.name"));
			int exitValue = SpringApplication.exit(context);
			System.exit(exitValue);
		}
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		try {
			logger.info("Create ServerSocketChannel : {}", pro.getString("module.name"));
			serverSocketChannel.socket().bind(new InetSocketAddress(server.getPort().intValue()));// tạo server cho
		} catch (Exception e) {
			logger.info("Shut Down module : {}  {}", pro.getString("module.name"));
			int exitValue = SpringApplication.exit(context);
			System.exit(exitValue);
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						logger.info("Server waiting connect.....");
						SocketChannel socketChannel = serverSocketChannel.accept();
						logger.info("One module connected ");
						socketChannel.socket().setKeepAlive(true);
						SocketChannel socketChannelTmp = null;
						InputStream in = socketChannel.socket().getInputStream();
						OutputStream out = socketChannel.socket().getOutputStream();
						byte tmp[] = new byte[100];
						String jobNameClient = null;
						try {
							in.read(tmp);
							// server gửi thông tin của nó cho client
							out.write(moduleBo.getModuleName().getBytes());
							jobNameClient = new String(tmp);
						} catch (Exception e) {
							jobNameClient = "";
						}
						logger.info("module connected {}", jobNameClient);
						for (Map.Entry<ModuleBo, SocketChannel> entry : map.entrySet()) {
							if (jobNameClient.trim().indexOf(entry.getKey().getModuleName().trim()) != -1) {
								socketChannelTmp = entry.getValue();
								if (entry.getKey().getState() == 0L && moduleBo.getIsMaster() == 1) {
									ModuleBo tmpm = entry.getKey();
									tmpm.setState(1L);
									moduleService.updateModule(tmpm);
									logger.info("update database");
								}
							}
						}
						if (socketChannelTmp == null) {
							ModuleBo moduleBotmp = moduleService.getModule(jobNameClient.trim());
							if (moduleBotmp != null) {
								logger.info("add module {} in map", moduleBotmp.getModuleName());
								moduleBotmp.setState(1L);
								updateModuleInMap(moduleBotmp, socketChannel);
							} else {
								logger.info("close socket");
								socketChannel.close();
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();

				}

			}
		}).start();

		List<ModuleBo> listJobDisConnect = getAllClient(jobs);

		if (moduleBo.getIsMaster() == 1) {
			moduleService.updateAll(moduleBo, listJobDisConnect);
			addJobMaster();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				ExecutorService executor = Executors.newFixedThreadPool(20);
				List<String> checked = new ArrayList<String>();// lưu trữ thông tin các job đã xác nhận
				while (true) {
					if (!retry.isEmpty()) {// thực hiện thêm những module retry ở lần chạy trước vào map
						updateRetry();
						logger.info("Add Success list module retry in list servers can connect, map size : {}",
								map.size());
					}
					@SuppressWarnings("unused")
					ModuleBo tmp = null;
					List<ModuleBo> jobsTmp = new ArrayList<ModuleBo>();// lưu trữ các modul không retry được
					for (Map.Entry<ModuleBo, SocketChannel> entry : map.entrySet()) {
						ModuleBo jobBoTmp = null;
						SocketChannel obChannel = entry.getValue();

						OutputStream out = null;
						InputStream in = null;
						@SuppressWarnings("unused")
						ModuleBo jobtmp = null;
						try {
							out = obChannel.socket().getOutputStream();
							in = obChannel.socket().getInputStream();
							out.write(moduleBo.getModuleName().getBytes());// gửi thông tin của client cho server
							out.flush();
							// xác nhận thông tin
							if (!isChecked(entry.getKey().getModuleName(), checked)) {
								checked.add(entry.getKey().getModuleName());
								byte serverInfor[] = new byte[100];
								in.read(serverInfor);
								String info = new String(serverInfor);
								logger.info("information respon from server  : {}", info);
								if (info.indexOf(entry.getKey().getModuleName().trim()) != -1) {
									// xác nhận thông tin
									logger.info("successful confirmation  : {}", info);
									// thực hiện chuyển master nếu cần
									if (entry.getKey().getId() < moduleBo.getId() && entry.getKey().getIsMaster() == 1L
											&& moduleBo.getIsMaster() == 1L) {
										// xóa toàn bộ các job thuộc master sau đó cập nhật is master = 0;
										deleteJobOldMaster();
										updateListJob(entry.getKey().getId());
										moduleBo.setIsMaster(0L);
									}
									InetSocketAddress address = new InetSocketAddress(entry.getKey().getIp(),
											entry.getKey().getPort().intValue());
									SocketChannel socketChannel = null;
									try {
										socketChannel = SocketChannel.open(address);
										OutputStream out1 = socketChannel.socket().getOutputStream();
										out1.write(pro.getString("module.name").trim().getBytes());
										out1.flush();
										entry.setValue(socketChannel);
										if (moduleBo.getIsMaster() == 1L) {
											moduleService.updateModule(entry.getKey());
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									jobBoTmp = entry.getKey();
									jobBoTmp.setState(0L);
									jobsTmp.add(jobBoTmp);// thêm vào danh sách các job không còn kết nối được
									logger.info(" information Job {} do not exist", info);
								}
							} else {
								@SuppressWarnings("unused")
								Future<Boolean> future = executor.submit(new SocketReadChannel(obChannel));
							}

						} catch (Exception e) {
							logger.info("Disconnect with module : {}", entry.getKey().getModuleName());
							// e.printStackTrace();
							jobBoTmp = entry.getKey();
							SocketChannel socketChannel = retry(jobBoTmp);// lấy ra kết qủa sau khi retry
							if (socketChannel != null) {// retry thành công
								retry.put(jobBoTmp, socketChannel);
								logger.info("Retry success add one element in retry map, map size : {}", retry.size());

							} else {
								jobsTmp.add(jobBoTmp);
								logger.info("Retry not success add module {} element in list",
										jobBoTmp.getModuleName());

							}
						}
					}
					if (!jobsTmp.isEmpty()) {
						// không loại bỏ các job vẫn còn kết nối với data base
						removeModuleDisconnetDb(jobsTmp, moduleService.getNumberConnectTion(jobsTmp));

						for (ModuleBo moduleBo : jobsTmp) {
							map.remove(moduleBo);
							checked.remove(moduleBo.getModuleName());
							logger.info("Retry not success remove module {} in map", moduleBo.getModuleName());
						}
						// xóa các job ra khỏi list đã kiểm tra

						if (moduleBo.getIsMaster() == 1) {// nếu nó là master nó có quyền được cập nhật

							moduleService.updateAll(moduleBo, jobsTmp);
							String proc = pro.getString("sub.sql.redistribute");
							String table = pro.getString("table.redistribution.module.disconnect");
							logger.info("redistribute module disconnect");
							service.redistributeModuleDisconnect(proc, map, jobsTmp, moduleBo, table);

						} else {
							Long idMaster = checkMasterInListRetryFail(jobsTmp);
							if (idMaster != null) {
								logger.info("Master in list Retry not success");
								if (checkUpdateMaster()) {
									logger.info(" this module update is  Master ({})", moduleBo.getModuleName());
									String proc = pro.getString("sub.sql.redistribute");
									String table = pro.getString("table.redistribution.module.disconnect");
									logger.info("redistribute module disconnect");
									service.redistributeModuleDisconnect(proc, map, jobsTmp, moduleBo, table);
									moduleBo.setIsMaster(1L);
									jobs = moduleService.getAllModule();
									for (ModuleBo m : jobs) {// cần có nó thì proc update disconnect chạy mới chuẩn được
																// không thì nó cập nhật sai
										if (m.getState() == 0) {
											jobsTmp.add(m);
										}
									}
									moduleService.updateAll(moduleBo, jobsTmp);
									addJobMaster();

								} else { // nếu nó không được phép cập nhật làm master thì nó cập nhật lại thằng được
											// chọn làm master
									Long idMaster1 = getMasterCurrent();

									for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
										if (map.getKey().getId() == idMaster1) {
											map.getKey().setIsMaster(1L);
											logger.info("update module {} is master", map.getKey().getModuleName());
										}
									}
									for (ModuleBo m : jobs) {
										if (m.getId() == idMaster1) {
											m.setIsMaster(1L);
										}
									}
								}

							}
						}
					}
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	@SuppressWarnings("unused")
	private void updateModuleInMap(ModuleBo moduleBo, SocketChannel socketChannel) {
		ModuleBo module = null;
		for (Map.Entry<ModuleBo, SocketChannel> maps : map.entrySet()) {
			if (moduleBo.getId() == maps.getKey().getId()) {
				module = maps.getKey();
			}
		}
		if (module != null) {
			try {
				map.get(module).close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map.remove(module);
		}
		map.put(moduleBo, socketChannel);
	}

	/**
	 * create list socket module active and conect servers
	 * 
	 * @param map  map các client sẽ được request
	 * @param jobs danh sách job được lấy ra từ Db
	 * 
	 */
	public List<ModuleBo> getAllClient(List<ModuleBo> jobs) {

		List<ModuleBo> listJob = new ArrayList<>();

		for (ModuleBo moduleBo : jobs) {
			if (pro.getString("module.name").trim().equals(moduleBo.getModuleName()))
				continue;// không call đến chính job đó
			InetSocketAddress address = new InetSocketAddress(moduleBo.getIp(), moduleBo.getPort().intValue());
			SocketChannel socketChannel = null;
			try {
				socketChannel = SocketChannel.open(address);
				OutputStream out = socketChannel.socket().getOutputStream();
				out.write(pro.getString("module.name").trim().getBytes());
				out.flush();
				// kiem tra xem trong map co chua moi push

				updateModuleInMap(moduleBo, socketChannel);
			} catch (IOException e) {
				logger.info("Do not conect module {} ", moduleBo.getModuleName());
				// lưu lại danh sách các job mất kết nối
				moduleBo.setState(0L);
				listJob.add(moduleBo);
			}
		}

		// không loại bỏ các job vẫn còn kết nối với data base
		removeModuleDisconnetDb(listJob, moduleService.getNumberConnectTion(listJob));

		if (map.isEmpty()) {
			moduleBo.setIsMaster(1L);
		} else {
			// không có master nào

			Long id = Long.MAX_VALUE;
			if (!checkExistMaster()) {
				// kiểm tra xem nó có được làm master không nếu được thì cập nhật
				if (isMaster()) {
					id = moduleBo.getId();
					moduleBo.setIsMaster(1L);
				} else {
					for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
						if (map.getKey().getId() < id) {
							id = map.getKey().getId();
						}
					}
				}

			} else {
				if (moduleBo.getIsMaster() == 1) {
					id = moduleBo.getId();
				}
				for (ModuleBo m : jobs) {
					if (m.getIsMaster() == 1 && m.getId() < id) {
						id = m.getId();
					}
				}
				if (id == moduleBo.getId()) {
					moduleBo.setIsMaster(1L);
				} else {
					moduleBo.setIsMaster(0L);
				}
			}
			updateListJob(id);
		}

		return listJob;
	}

	public void updateListJob(Long id) {
		for (ModuleBo moBo : jobs) {
			if (moBo.getId() == id) {
				moBo.setIsMaster(1L);
			} else {
				moBo.setIsMaster(0L);
			}
		}
		for (Map.Entry<ModuleBo, SocketChannel> maps : map.entrySet()) {
			if (maps.getKey().getId() == id) {
				maps.getKey().setIsMaster(1L);
			} else {
				maps.getKey().setIsMaster(0L);
			}
		}
	}

	public boolean checkExistMaster() {
		for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
			if (map.getKey().getIsMaster() == 1L) {
				return true;
			}
		}
		if (moduleBo.getIsMaster() == 1L)
			return true;
		return false;
	}

	public boolean isMaster() {
		for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
			if (map.getKey().getId() < moduleBo.getId()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * retry connection server five times
	 * 
	 * @param job
	 * @return
	 */
	public SocketChannel retry(ModuleBo job) {
		InetSocketAddress address = new InetSocketAddress(job.getIp(), job.getPort().intValue());
		SocketChannel socketChannel = null;
		for (int i = 0; i < 5; i++) {
			try {
				logger.info("retry time {}", i);
				Thread.sleep(150);
				socketChannel = SocketChannel.open(address);
				OutputStream out = socketChannel.socket().getOutputStream();
				out.write(moduleBo.getModuleName().getBytes());
				out.flush();
				InputStream in = socketChannel.socket().getInputStream();
				byte tmp[] = new byte[100];
				in.read(tmp);
				String s = new String(tmp);
				if (!s.trim().equals(job.getModuleName().trim())) {
					socketChannel = null;
				} else {
					break;
				}
			} catch (Exception e) {
				socketChannel = null;
				logger.info("retry not success");
			}
		}
		return socketChannel;

	}

	/**
	 * delete element in map retry exists in map after add map retry in map
	 * 
	 */
	public void updateRetry() {

		List<ModuleBo> jobstmp = new ArrayList<ModuleBo>();
		for (Map.Entry<ModuleBo, SocketChannel> entryRetry : retry.entrySet()) {
			for (Map.Entry<ModuleBo, SocketChannel> entryMap : map.entrySet()) {
				if (entryRetry.getKey().getId() == entryMap.getKey().getId()) {
					jobstmp.add(entryRetry.getKey());
				}
			}
		}
		for (ModuleBo moduleBo : jobstmp) {
			map.remove(moduleBo);
		}

		map.putAll(retry);
		retry.clear();

	}

	/**
	 * check exists module master in list retry fail
	 * 
	 * @param jobsTmp list module retry fail
	 * @return
	 */
	public Long checkMasterInListRetryFail(List<ModuleBo> jobsTmp) {

		for (ModuleBo moduleBo : jobsTmp) {
			if (moduleBo.getIsMaster() == 1) {
				moduleBo.setIsMaster(0L);
				return 1L;
			}
		}
		return null;
	}

	/**
	 * check this module update master
	 * 
	 * @return
	 */
	public Boolean checkUpdateMaster() {
		for (Map.Entry<ModuleBo, SocketChannel> entryMap : map.entrySet()) {
			if (entryMap.getKey().getId() < moduleBo.getId()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * remove modules disconnect database
	 * 
	 * @param moduleBos
	 * @param objects
	 */
	public void removeModuleDisconnetDb(List<ModuleBo> moduleBos, List<Map<String, String>> map) {
		List<ModuleBo> list = new ArrayList<ModuleBo>();
		if (!map.isEmpty()) {
			for (ModuleBo moduleBo : moduleBos) {
				for (Map<String, String> object : map) {
					if (object.get(moduleBo.getModuleName()) != null) {
						list.add(moduleBo);
					}
				}
			}
			for (ModuleBo moduleBo : list) {
				moduleBos.remove(moduleBo);
			}
		}
	}

	public Boolean isChecked(String moduleName, List<String> list) {
		for (String s : list) {
			if (s.trim().equals(moduleName.trim())) {
				return true;
			}
		}
		return false;
	}

	public void deleteJobOldMaster() {
		Scheduler sc1 = scheduler.getScheduler();
		try {
			sc1.start();
			logger.info("Job filter data already exist, delete old job filter data");
			sc1.deleteJob(jobFilterData.getObject().getKey());
			logger.info("jobReDistributeOldRecord already exist, delete old jobReDistributeOldRecord");
			sc1.deleteJob(jobReDistributeOldRecord.getObject().getKey());

		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addJobMaster() {
		Scheduler sc1 = scheduler.getScheduler();
		try {
			sc1.start();
			
			if (sc1.checkExists(jobFilterData.getObject().getKey())) {
				logger.info("Job filter data already exist, delete old job filter data");
				sc1.deleteJob(jobFilterData.getObject().getKey());
			}
			if (sc1.checkExists(jobReDistributeOldRecord.getObject().getKey())) {
				logger.info("jobReDistributeOldRecord already exist, delete old jobReDistributeOldRecord");
				sc1.deleteJob(jobReDistributeOldRecord.getObject().getKey());
			}

			logger.info("Add job filter data cancel service in master");
			sc1.scheduleJob(jobFilterData.getObject(), filterData.getObject());
			logger.info("Add job jobReDistributeOldRecord in master");
			sc1.scheduleJob(jobReDistributeOldRecord.getObject(), reDistributeOldRecord.getObject());
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public Long getMasterCurrent() {
		Long id = moduleBo.getId();
		for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
			if (map.getKey().getId() < id) {
				id = map.getKey().getId();
			}
		}
		return id;
	}
}
