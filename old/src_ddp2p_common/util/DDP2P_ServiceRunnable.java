package util;

import config.Application_GUI;

abstract public class DDP2P_ServiceRunnable implements Runnable {
	private static final boolean DEBUG = true;
	public Object ctx;
	String name;
	boolean daemon;
	boolean accounting = true;
//	/**
//	 * The daemon value is set only if it is "true"
//	 * 
//	 * @param name
//	 * @param daemon
//	 */
//	public DDP2P_ServiceRunnable (String name, boolean daemon) {
//		this.name = name;
//		this.daemon = daemon;
//	}
//	/**
//	 * The daemon value is set only if it is "true"
//	 * 
//	 * @param name
//	 * @param daemon
//	 * @param ctx
//	 */
//	public DDP2P_ServiceRunnable (String name, boolean daemon, Object ctx) {
//		this.name = name;
//		this.daemon = daemon;
//		this.ctx = ctx;
//	}
	/**
	 * The daemon value is set only if it is "true"
	 * 
	 * @param name
	 * @param daemon
	 * @param accounting
	 * @param ctx
	 */
	public DDP2P_ServiceRunnable (String name, boolean daemon, boolean accounting, Object ctx) {
		this.name = name;
		this.daemon = daemon;
		this.ctx = ctx;
		this.accounting = accounting;
		if (accounting)
			if (DEBUG) Util.printCallPath("Sure? Do not do this on Swing!");
	}
//	/**
//	 * 
//	 * @param ctx
//	 */
//	public DDP2P_ServiceRunnable (Object ctx) {
//		this.ctx = ctx;
//	}
	public Thread start() {
		Thread th = new Thread(this);
		if (daemon) th.setDaemon(daemon);
		if (name != null) th.setName(name);
		th.start();
		return th;
	}
	public static void ping(String msg) {
		Application_GUI.ThreadsAccounting_ping(msg);
	}
	public Object getContext () {
		return ctx;
	}
	public void run () {
		if (accounting) Application_GUI.ThreadsAccounting_registerThread();//ThreadsAccounting.registerThread();
		try {
			_run();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if (accounting) Application_GUI.ThreadsAccounting_unregisterThread();//ThreadsAccounting.unregisterThread();
	}
	abstract public void _run();
}