package com.kuailu.im.server.command;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 */
public class CommandConfiguration {
	private  int cmd ;
	private  String cmdHandler ;
	private  List<String> cmdProcessors = new ArrayList<String>();
	
	public CommandConfiguration(){}
	
	public CommandConfiguration(String cmd,Properties prop){
		this.cmd = Integer.parseInt(cmd);
		String[] values = prop.getProperty(cmd).split(",");
		if(values.length > 0){
			cmdHandler = values[0];
			if(values.length >1){
				for(int i = 0 ; i < values.length ; i++){
					if(i > 0) {
						cmdProcessors.add(values[i]);
					}
				}
			}
		}
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public String getCmdHandler() {
		return cmdHandler;
	}

	public void setCmdHandler(String cmdHandler) {
		this.cmdHandler = cmdHandler;
	}

	public List<String> getCmdProcessors() {
		return cmdProcessors;
	}

	public void setCmdProcessors(List<String> cmdProcessors) {
		this.cmdProcessors = cmdProcessors;
	}
}
