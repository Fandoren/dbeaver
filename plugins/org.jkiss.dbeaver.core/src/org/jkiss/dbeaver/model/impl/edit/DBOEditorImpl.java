/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.edit;

import net.sf.jkiss.utils.CommonUtils;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.model.edit.DBOCommand;
import org.jkiss.dbeaver.model.edit.DBOCommandListener;
import org.jkiss.dbeaver.model.edit.DBOCommandReflector;
import org.jkiss.dbeaver.model.edit.DBOEditor;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.*;

/**
 * DBOEditorImpl
 */
public abstract class DBOEditorImpl<OBJECT_TYPE extends DBSObject> extends DBOManagerImpl<OBJECT_TYPE> implements DBOEditor<OBJECT_TYPE> {

    private static class PersistInfo {
        final IDatabasePersistAction action;
        boolean executed = false;
        Throwable error;

        public PersistInfo(IDatabasePersistAction action)
        {
            this.action = action;
        }
    }

    protected class CommandInfo {
        final DBOCommand<OBJECT_TYPE> command;
        final DBOCommandReflector reflector;
        List<PersistInfo> persistActions;
        CommandInfo mergedBy = null;
        boolean executed = false;

        public CommandInfo(DBOCommand<OBJECT_TYPE> command, DBOCommandReflector reflector)
        {
            this.command = command;
            this.reflector = reflector;
        }

        public DBOCommand<OBJECT_TYPE> getCommand()
        {
            return command;
        }

        public DBOCommandReflector getReflector()
        {
            return reflector;
        }
    }

    private final List<CommandInfo> commands = new ArrayList<CommandInfo>();
    private final List<CommandInfo> undidCommands = new ArrayList<CommandInfo>();
    private List<CommandInfo> mergedCommands = null;

    private final List<DBOCommandListener> listeners = new ArrayList<DBOCommandListener>();

    public boolean isDirty()
    {
        synchronized (commands) {
            return !getObject().isPersisted() || !getMergedCommands().isEmpty();
        }
    }

    public void saveChanges(DBRProgressMonitor monitor) throws DBException {
        synchronized (commands) {
            List<CommandInfo> mergedCommands = getMergedCommands();

            // Validate commands
            for (CommandInfo cmd : mergedCommands) {
                cmd.command.validateCommand(getObject());
            }
            try {
                // Make list of not-executed commands
                for (int i = 0; i < mergedCommands.size(); i++) {
                    CommandInfo cmd = mergedCommands.get(i);
                    if (cmd.mergedBy != null) {
                        cmd = cmd.mergedBy;
                    }
                    if (cmd.executed) {
                        commands.remove(mergedCommands.get(i));
                        continue;
                    }
                    if (monitor.isCanceled()) {
                        break;
                    }
                    // Persist changes
                    if (CommonUtils.isEmpty(cmd.persistActions)) {
                        IDatabasePersistAction[] persistActions = cmd.command.getPersistActions(getObject());
                        if (!CommonUtils.isEmpty(persistActions)) {
                            cmd.persistActions = new ArrayList<PersistInfo>(persistActions.length);
                            for (IDatabasePersistAction action : persistActions) {
                                cmd.persistActions.add(new PersistInfo(action));
                            }
                        }
                    }
                    if (!CommonUtils.isEmpty(cmd.persistActions)) {
                        DBCExecutionContext context = openCommandPersistContext(monitor, cmd.command);
                        try {
                            for (PersistInfo persistInfo : cmd.persistActions) {
                                if (persistInfo.executed) {
                                    continue;
                                }
                                if (monitor.isCanceled()) {
                                    break;
                                }
                                try {
                                    executePersistAction(context, persistInfo.action);
                                    persistInfo.executed = true;
                                } catch (DBException e) {
                                    persistInfo.error = e;
                                    persistInfo.executed = false;
                                    throw e;
                                }
                            }
                        } finally {
                            closePersistContext(context);
                        }
                    }
                    // Update model
                    cmd.command.updateModel(getObject());
                    cmd.executed = true;

                    // Remove original command from stack
                    commands.remove(mergedCommands.get(i));
                }
            }
            finally {
                clearMergedCommands();
                clearUndidCommands();

                for (DBOCommandListener listener : getListeners()) {
                    listener.onSave();
                }
            }
        }
    }

    public void resetChanges()
    {
        synchronized (commands) {
            try {
                while (!commands.isEmpty()) {
                    undoCommand();
                }
                clearUndidCommands();
                clearMergedCommands();
            } finally {
                for (DBOCommandListener listener : getListeners()) {
                    listener.onReset();
                }
            }
        }
    }

    public Collection<? extends DBOCommand<OBJECT_TYPE>> getCommands()
    {
        synchronized (commands) {
            List<DBOCommand<OBJECT_TYPE>> cmdCopy = new ArrayList<DBOCommand<OBJECT_TYPE>>(commands.size());
            for (CommandInfo cmdInfo : getMergedCommands()) {
                if (cmdInfo.mergedBy != null) {
                    cmdInfo = cmdInfo.mergedBy;
                }
                if (!cmdCopy.contains(cmdInfo.command)) {
                    cmdCopy.add(cmdInfo.command);
                }
            }
            return cmdCopy;
        }
    }

    public <COMMAND extends DBOCommand<OBJECT_TYPE>> void addCommand(
        COMMAND command,
        DBOCommandReflector<OBJECT_TYPE, COMMAND> reflector)
    {
        synchronized (commands) {
            commands.add(new CommandInfo(command, reflector));

            clearUndidCommands();
            clearMergedCommands();
        }
    }

    public <COMMAND extends DBOCommand<OBJECT_TYPE>> void removeCommand(COMMAND command)
    {
        synchronized (commands) {
            for (CommandInfo cmd : commands) {
                if (cmd.command == command) {
                    commands.remove(cmd);
                    break;
                }
            }
            clearUndidCommands();
            clearMergedCommands();
        }
    }

    public <COMMAND extends DBOCommand<OBJECT_TYPE>> void updateCommand(COMMAND command)
    {
        synchronized (commands) {
            clearUndidCommands();
            clearMergedCommands();
        }
    }

    public void addCommandListener(DBOCommandListener listener)
    {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeCommandListener(DBOCommandListener listener)
    {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    DBOCommandListener[] getListeners()
    {
        synchronized (listeners) {
            return listeners.toArray(new DBOCommandListener[listeners.size()]);
        }
    }

    public boolean canUndoCommand()
    {
        synchronized (commands) {
            return !commands.isEmpty() && commands.get(commands.size() - 1).command.isUndoable();
        }
    }

    public boolean canRedoCommand()
    {
        synchronized (commands) {
            return !undidCommands.isEmpty();
        }
    }

    public void undoCommand()
    {
        if (!canUndoCommand()) {
            throw new IllegalStateException("Can't undo command");
        }
        synchronized (commands) {
            CommandInfo lastCommand = commands.remove(commands.size() - 1);
            if (!lastCommand.command.isUndoable()) {
                throw new IllegalStateException("Last executed command is not undoable");
            }
            // Undo UI changes and put command in undid command stack
            if (lastCommand.reflector != null) {
                lastCommand.reflector.undoCommand(lastCommand.command);
            }
            undidCommands.add(lastCommand);
            clearMergedCommands();
        }
    }

    public void redoCommand()
    {
        if (!canRedoCommand()) {
            throw new IllegalStateException("Can't redo command");
        }
        synchronized (commands) {
            // Just redo UI changes and put command on the top of stack
            CommandInfo commandInfo = undidCommands.remove(undidCommands.size() - 1);
            if (commandInfo.reflector != null) {
                commandInfo.reflector.redoCommand(commandInfo.command);
            }
            commands.add(commandInfo);
            clearMergedCommands();
        }
    }

    private void clearUndidCommands()
    {
        undidCommands.clear();
    }

    private List<CommandInfo> getMergedCommands()
    {
        if (mergedCommands != null) {
            return mergedCommands;
        }
        mergedCommands = new ArrayList<CommandInfo>();

        final Map<DBOCommand, CommandInfo> mergedByMap = new IdentityHashMap<DBOCommand, CommandInfo>();
        final Map<String, Object> userParams = new HashMap<String, Object>();
        for (int i = 0; i < commands.size(); i++) {
            CommandInfo lastCommand = commands.get(i);
            lastCommand.mergedBy = null;
            CommandInfo firstCommand = null;
            DBOCommand<OBJECT_TYPE> result = lastCommand.command;
            if (mergedCommands.isEmpty()) {
                result = lastCommand.command.merge(null, userParams);
            } else {
                for (int k = mergedCommands.size(); k > 0; k--) {
                    firstCommand = mergedCommands.get(k - 1);
                    result = lastCommand.command.merge(firstCommand.command, userParams);
                    if (result != lastCommand.command) {
                        break;
                    }
                }
            }
            if (result == null) {
                // Remove first and skip last command
                mergedCommands.remove(firstCommand);
                continue;
            }

            mergedCommands.add(lastCommand);
            if (result == lastCommand.command) {
                // No changes
                //firstCommand.mergedBy = lastCommand;
            } else if (firstCommand != null && result == firstCommand.command) {
                // Remove last command from queue
                lastCommand.mergedBy = firstCommand;
            } else {
                // Some other command
                // May be it is some earlier command from queue or some new command (e.g. composite)
                CommandInfo mergedBy = mergedByMap.get(result);
                if (mergedBy == null) {
                    // Try to find in command stack
                    for (int k = i; k >= 0; k--) {
                        if (commands.get(k).command == result) {
                            mergedBy = commands.get(k);
                            break;
                        }
                    }
                    if (mergedBy == null) {
                        // Create new command info
                        mergedBy = new CommandInfo(result, null);
                    }
                    mergedByMap.put(result, mergedBy);
                }
                lastCommand.mergedBy = mergedBy;
                if (!mergedCommands.contains(mergedBy)) {
                    mergedCommands.add(mergedBy);
                }
            }
        }
        filterCommands(mergedCommands);
        return mergedCommands;
    }

    private void clearMergedCommands()
    {
        mergedCommands = null;
    }

    protected DBCExecutionContext openCommandPersistContext(
        DBRProgressMonitor monitor,
        DBOCommand<OBJECT_TYPE> command)
        throws DBException
    {
        return getDataSource().openContext(
            monitor,
            DBCExecutionPurpose.USER_SCRIPT,
            "Execute " + command.getTitle());
    }

    protected void closePersistContext(DBCExecutionContext context)
    {
        context.close();
    }

    protected void filterCommands(List<CommandInfo> commands)
    {
        // do nothing by default
    }


    protected void executePersistAction(
        DBCExecutionContext context,
        IDatabasePersistAction action)
        throws DBException
    {
        throw new DBException("Object persistence is not implemented");
    }

}