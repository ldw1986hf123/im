/**
 *
 */
package com.kuailu.im.server.command;


import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.server.processor.MultiProtocolCmdProcessor;
import com.kuailu.im.server.processor.SingleProtocolCmdProcessor;
import com.kuailu.im.server.service.IChatGroupMemberService;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.util.ApplicationContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class CommandManager {
    /**
     * 通用cmd处理命令
     */
    private static Map<Integer, AbstractCmdHandler> handlerMap = new HashMap<>();
    private static Logger LOG = LoggerFactory.getLogger(CommandManager.class);

    private CommandManager() {
    }

    ;

    static {
        try {
            List<CommandConfiguration> configurations = CommandConfigurationFactory.parseConfiguration();
            init(configurations);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        }
    }

    private static void init(List<CommandConfiguration> configurations) throws Exception {
        for (CommandConfiguration configuration : configurations) {
            AbstractCmdHandler cmdHandler = ((Class<AbstractCmdHandler>) Class.forName(configuration.getCmdHandler())).newInstance();
            List<String> cmdProcessors = configuration.getCmdProcessors();
            if (!cmdProcessors.isEmpty()) {
                for (String cmdProcessor : cmdProcessors) {
                    Object cmdProcessorObj = Class.forName(cmdProcessor).newInstance();
                    if (cmdProcessorObj instanceof MultiProtocolCmdProcessor) {
                        cmdHandler.addMultiProtocolProcessor((MultiProtocolCmdProcessor) cmdProcessorObj);
                    } else if (cmdProcessorObj instanceof SingleProtocolCmdProcessor) {
                        cmdHandler.setSingleProcessor((SingleProtocolCmdProcessor) cmdProcessorObj);
                    }
                }
            }
            registerCommand(cmdHandler);
        }
    }

    public static AbstractCmdHandler registerCommand(AbstractCmdHandler imCommandHandler) throws Exception {
        if (imCommandHandler == null || imCommandHandler.command() == null) {
            return null;
        }
        int cmd_number = imCommandHandler.command().getNumber();
        if (Objects.isNull(Command.forNumber(cmd_number))) {
            throw new ImException("failed to register cmd handler, illegal cmd code:" + cmd_number + ",use Command.addAndGet () to add in the enumerated Command class!");
        }
        if (Objects.isNull(handlerMap.get(cmd_number))) {
            return handlerMap.put(cmd_number, imCommandHandler);
        } else {
            throw new ImException("cmd code:" + cmd_number + ",has been registered, please correct!");
        }
    }


    public static AbstractCmdHandler removeCommand(Command command) {
        if (command == null) {
            return null;
        }
        int cmd_value = command.getNumber();
        if (handlerMap.get(cmd_value) != null) {
            return handlerMap.remove(cmd_value);
        }
        return null;
    }

    public static <T> T getCommand(Command command, Class<T> clazz) {
        AbstractCmdHandler cmdHandler = getCommand(command);
        if (cmdHandler != null) {
            return (T) cmdHandler;
        }
        return null;
    }

    public static AbstractCmdHandler getCommand(Command command) {
        if (command == null) {
            return null;
        }
        return handlerMap.get(command.getNumber());
    }
}
